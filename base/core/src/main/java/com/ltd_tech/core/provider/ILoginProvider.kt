package com.ltd_tech.core.provider

import com.alibaba.android.arouter.facade.template.IProvider
import java.io.Serializable

/**
 * 登录协议类
 */
interface ILoginProvider : IProvider {

    /**
     * 登录
     */
    fun login(name: String, password: String)

    /**
     * 退出
     */
    fun logout()

    /**
     * 根据key查询用户的信息
     */
    fun <T : Serializable> getUserInfo(key: String, value: T): T

    /**
     * 获取用户的token
     */
    fun getUserToken(): String?
}