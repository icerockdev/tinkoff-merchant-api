/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.tinkoff

import org.apache.commons.codec.digest.DigestUtils

class TinkoffUtils(private val credential: TinkoffCredential) {

    fun generateToken(payload: TinkoffPayload): String {
        val tokenBuilder = StringBuilder()
        val data = payload.data

        data["Password"] = credential.getSecretKey()
        data.filter { entry -> entry.key != "DATA" && entry.key != "Receipt" && entry.key != "Token" }
            .toList()
            .sortedBy { (key) -> key }
            .forEach { (_, value) -> tokenBuilder.append(value.toString()) }

        return DigestUtils.sha256Hex(tokenBuilder.toString())
    }
}
