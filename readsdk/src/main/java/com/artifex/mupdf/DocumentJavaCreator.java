package com.artifex.mupdf;

import android.app.Activity;
import android.content.ContentResolver;
import android.net.Uri;

import com.artifex.mupdf.fitz.SeekableInputStream;
import com.artifex.mupdf.viewer.ContentInputStream;
import com.artifex.mupdf.viewer.MuPDFCore;

import java.io.IOException;
import java.io.InputStream;

public class DocumentJavaCreator {

    public DocumentJavaCreator(){}


    private MuPDFCore openBuffer(byte buffer[], String magic) {
        try {
            return new MuPDFCore(buffer, magic);
        } catch (Exception e) {
            return null;
        }
    }

    private MuPDFCore openStream(SeekableInputStream stm, String magic) {
        try {
            return new MuPDFCore(stm, magic);
        } catch (Exception e) {
            return null;
        }
    }

    public MuPDFCore openCore(Activity activity, Uri uri, long size, String mimetype) throws IOException {
        ContentResolver cr = activity.getContentResolver();

        InputStream is = cr.openInputStream(uri);
        byte[] buf = null;
        int used = -1;
        try {
            final int limit = 8 * 1024 * 1024;
            if (size < 0) { // size is unknown
                buf = new byte[limit];
                used = is.read(buf);
                boolean atEOF = is.read() == -1;
                if (used < 0 || (used == limit && !atEOF)) // no or partial data
                    buf = null;
            } else if (size <= limit) { // size is known and below limit
                buf = new byte[(int) size];
                used = is.read(buf);
                if (used < 0 || used < size) // no or partial data
                    buf = null;
            }
            if (buf != null && buf.length != used) {
                byte[] newbuf = new byte[used];
                System.arraycopy(buf, 0, newbuf, 0, used);
                buf = newbuf;
            }
        } catch (OutOfMemoryError e) {
            buf = null;
        } finally {
            is.close();
        }

        if (buf != null) {
            return openBuffer(buf, mimetype);
        } else {
            return openStream(new ContentInputStream(cr, uri, size), mimetype);
        }
    }
}
