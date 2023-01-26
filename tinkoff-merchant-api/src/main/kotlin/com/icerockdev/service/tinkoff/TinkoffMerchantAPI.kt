/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.tinkoff

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.icerockdev.service.tinkoff.exception.TinkoffErrorException
import com.icerockdev.service.tinkoff.response.ErrorResponse
import com.icerockdev.service.tinkoff.response.Response
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

internal class TinkoffMerchantAPI(
    val client: HttpClient,
    val credential: TinkoffCredential,
    val utils: TinkoffUtils
) {
    val mapper = jacksonObjectMapper()

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
        val response: String = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(payload.data)
        }.body()

        return try {
            val result = mapper.readValue<T>(response)
            when {
                !result.success -> throw Exception()
                else -> result
            }
        } catch (cause: Throwable) {
            try {
                val error = mapper.readValue<ErrorResponse>(response)
                throw TinkoffErrorException(
                    error.message.toString(),
                    error.errorCode.toInt(),
                    error.details
                )
            } catch (cause: JsonParseException) {
                throw TinkoffErrorException(cause.message ?: "", TinkoffErrorCode.INTERNAL.value)
            }
        }
    }
}
