/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.tinkoff

import org.junit.Test

import org.junit.Assert.*

class TinkoffUtilsTest {

    private val credential: TinkoffCredential = TinkoffCredential(
        terminalKey = "1234567890123DEMO",
        secretKey = "n6sd22449gugk8mb"
    )
    private val utils: TinkoffUtils = TinkoffUtils(credential)

    @Test
    fun generateTokenTest() {
        val payload: TinkoffPayload = TinkoffPayload().apply {
            data["Success"] = true
            data["ErrorCode"] = "0"
            data["TerminalKey"] = "1234567890123DEMO"
            data["OrderId"] = "201709"
            data["Status"] = "CONFIRMED"
            data["Amount"] = 100000
            data["PaymentId"] = "8742591"
            data["CardId"] = "322264"
            data["Pan"] = "430000******0777"
            data["ExpDate"] = "1122"
            data["RebillId"] = "101709"
            data["Token"] = "7c9b1cbe164a0286e393b28493429a77d037862ad5a03ae4bd96491f31f55d64"
        }

        // Will skip Receipt, DATA and Token parameters
        val token = utils.generateToken(payload)

        assertEquals(token, payload.data["Token"])
    }
}
