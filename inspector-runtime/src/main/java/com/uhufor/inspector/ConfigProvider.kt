package com.uhufor.inspector

import android.content.Context

interface ConfigProvider {
    fun getConfig(): Config
}

fun Context.configProvider(): ConfigProvider {
    return object : ConfigProvider {
        override fun getConfig(): Config = Inspector.config
    }
}
