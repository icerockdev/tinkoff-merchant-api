/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.tinkoff;

class TinkoffCredential(
    private val apiUrl: String = "https://securepay.tinkoff.ru/v2/",
    private val terminalKey: String,
    private val secretKey: String
) {
    fun getApiUrl() = apiUrl
    fun getTerminalKey() = terminalKey
    fun getSecretKey() = secretKey
}
