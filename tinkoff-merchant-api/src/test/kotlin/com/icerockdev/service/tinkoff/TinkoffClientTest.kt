/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.tinkoff

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.icerockdev.service.tinkoff.exception.TinkoffValidationException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals

@Suppress("BlockingMethodInNonBlockingContext")
class TinkoffClientTest {

    private val mapper = ObjectMapper()
    private val credential: TinkoffCredential = TinkoffCredential(
        terminalKey = "1234567890123DEMO",
        secretKey = "n6sd22449gugk8mb"
    )
    private val utils: TinkoffUtils = TinkoffUtils(credential)
    private val mockHttpClient: HttpClient = HttpClient(MockEngine) {
        install(JsonFeature) {
            serializer = JacksonSerializer {
                setSerializationInclusion(JsonInclude.Include.NON_NULL);
            }
        }
        engine {
            addHandler { request ->
                val headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString()))
                val json = (request.body as TextContent).text
                val payload: Map<String, Any> = mapper.readValue(json, object : TypeReference<Map<String, Any>>(){})

                val orderId = payload["OrderId"]
                val amount = payload["Amount"]
                val paymentId = payload["PaymentId"]

                when (request.url.fullPath) {
                    "/v2/Init" -> {
                        respond("{\n" +
                                "\"Success\" : true,\n" +
                                "\"ErrorCode\" : \"0\",\n" +
                                "\"TerminalKey\" : \"1234567890123DEMO\",\n" +
                                "\"Status\" : \"NEW\",\n" +
                                "\"PaymentId \": \"13660\",\n" +
                                "\"OrderId\" : \"${orderId}\",\n" +
                                "\"Amount\" : ${amount},\n" +
                                "\"PaymentURL\" : \"https://securepay.tinkoff.ru/rest/Authorize/1B63Y1\"\n" +
                                "}", headers = headers)
                    }
                    "/v2/Confirm" -> {
                        respond("{\n" +
                                " \"Success\" :  true,\n" +
                                " \"ErrorCode\" :  \"0\",\n" +
                                " \"TerminalKey\" : \"1234567890123DEMO\",\n" +
                                " \"Status\" : \"CONFIRMED\",\n" +
                                " \"PaymentId\" : \"${paymentId}\",\n" +
                                " \"OrderId\" : \"${orderId}\"\n" +
                                "}", headers = headers)
                    }
                    else -> respondError(status = HttpStatusCode.InternalServerError, headers = headers)
                }
            }
        }
    }
    private val tinkoffClient: TinkoffClient = TinkoffClient(client = mockHttpClient, credential = credential, utils = utils)

    @Test
    fun initTest() = runBlocking {
        val amount = 10000
        val orderId = randomOrderId()
        val response = tinkoffClient.init(amount, orderId)

        assertEquals(response.amount, amount)
        assertEquals(response.orderId, orderId)
        assertEquals(response.status, TinkoffStatus.NEW.value)
    }

    @Test
    fun initFailTest() = runBlocking {
        val amount = 10000
        val orderId = "LONG_${randomOrderId()}"

        val result = try {
            tinkoffClient.init(amount, orderId)
        } catch (e: TinkoffValidationException) {
            assertEquals(e.message, "OrderId length should be between 1 anf 20.")
        }
    }

    @Test
    fun confirmTest() = runBlocking {
        val paymentId = randomPaymentId()
        val response = tinkoffClient.confirm(paymentId)

        assertEquals(response.paymentId, paymentId)
        assertEquals(response.status, TinkoffStatus.CONFIRMED.value)
    }

    private fun randomOrderId(): String {
        return "TEST_ORDER_ID_${randomString()}"
    }

    private fun randomPaymentId(): Int {
        return (1000000..9999999).random()
    }

    private fun randomString(length: Int = 6): String {
        return (1..length)
            .map { (('a'..'z') + ('A'..'Z') + ('0'..'9')).random() }
            .joinToString("")
    }
}
