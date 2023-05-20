package com.ltd_tech.readsdk.utils

import android.app.Activity
import android.content.ContentResolver
import android.net.Uri
import com.artifex.mupdf.fitz.SeekableInputStream
import com.artifex.mupdf.viewer.ContentInputStream
import com.artifex.mupdf.viewer.MuPDFCore
import java.io.IOException


class DocumentParser {

    fun openBuffer(buffer: ByteArray, magic: String): MuPDFCore? =
        try {
            MuPDFCore(buffer, magic)
        } catch (e: Exception) {
            null
        }

    fun openStream(stm: SeekableInputStream, magic: String): MuPDFCore? =
        try {
            MuPDFCore(stm, magic)
        } catch (e: Exception) {
            null
        }

    @Throws(IOException::class)
    fun openCore(activity: Activity, uri: Uri, size: Long, mimetype: String): MuPDFCore? {
        val cr: ContentResolver = activity.contentResolver
        val input = cr.openInputStream(uri) ?: return null
        var buf: ByteArray? = null
        var used = -1
        try {
            val limit = 8 * 1024 * 1024
            if (size < 0) { // size is unknown
                buf = ByteArray(limit)
                used = input.read(buf)
                val atEOF = input.read() == -1
                if (used < 0 || used == limit && !atEOF) // no or partial data
                    buf = null
            } else if (size <= limit) { // size is known and below limit
                buf = ByteArray(size.toInt())
                used = input.read(buf)
                if (used < 0 || used < size) // no or partial data
                    buf = null
            }
            if (buf != null && buf.size != used) {
                val newbuf = ByteArray(used)
                System.arraycopy(buf, 0, newbuf, 0, used)
                buf = newbuf
            }
        } catch (e: OutOfMemoryError) {
            buf = null
        } finally {
            input.close()
        }
        return if (buf != null) {
            openBuffer(buf, mimetype)
        } else {
            openStream(ContentInputStream(cr, uri, size), mimetype)
        }
    }


}