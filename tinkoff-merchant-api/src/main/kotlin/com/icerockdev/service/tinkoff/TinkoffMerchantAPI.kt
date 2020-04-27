/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.tinkoff

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.icerockdev.service.tinkoff.exception.TinkoffErrorException
import com.icerockdev.service.tinkoff.response.ErrorResponse
import com.icerockdev.service.tinkoff.response.Response
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class TinkoffMerchantAPI(
    val client: HttpClient,
    val credential: TinkoffCredential,
    val utils: TinkoffUtils
) {
    val mapper = ObjectMapper()

    suspend inline fun <reified T : Response> buildQuery(
        path: String,
        payload: TinkoffPayload
    ): T {
        val url = StringBuilder(credential.getApiUrl())
        url.append(path)
        payload.data["TerminalKey"] = credential.getTerminalKey()
        payload.data["Token"] = utils.generateToken(payload)

        return send(url.toString(), payload)
    }

    suspend inline fun <reified T : Response> send(url: String, payload: TinkoffPayload): T {
        val response = client.post<String>(url) {
            contentType(ContentType.Application.Json)
            body = payload.data
        }

        return try {
            val result = mapper.readValue<T>(response)
            when {
                !result.success -> throw Exception(result.errorCode)
                else -> result
            }
        } catch (cause: Throwable) {
            val error = mapper.readValue<ErrorResponse>(response)
            throw TinkoffErrorException(
                error.message.toString(),
                error.errorCode.toInt()
            )
        }
    }
}
