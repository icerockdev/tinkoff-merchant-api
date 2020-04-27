/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.sample

import com.fasterxml.jackson.databind.ObjectMapper
import com.icerockdev.api.AbstractResponse
import com.icerockdev.api.Request
import com.icerockdev.service.tinkoff.TinkoffClient
import com.icerockdev.service.tinkoff.TinkoffCredential
import com.icerockdev.service.tinkoff.TinkoffUtils
import com.icerockdev.util.QueryParser
import com.icerockdev.webserver.*
import com.icerockdev.webserver.log.JsonDataLogger
import com.icerockdev.webserver.tools.receiveRequest
import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
fun Application.main() {
    install(StatusPages, getStatusConfiguration())
    install(CORS) {
        applyDefaultCORS()
        anyHost() // TODO: Don't do this in production if possible. Try to limit it.
    }
    install(DefaultHeaders)
    install(CallLogging) {
        applyDefaultLogging()
    }
    install(JsonDataLogger) {
        mapperConfiguration = getObjectMapper()
    }
    install(CallId, getCallConfiguration())
    install(ContentNegotiation) {
        jackson(block = getObjectMapper())
    }
    install(QueryParser) {
        mapperConfiguration = getObjectMapper()
    }

    /**
     * @see <a href="https://ktor.io/advanced/pipeline.html#ktor-pipelines">Ktor Documentation</a>
     */
    intercept(ApplicationCallPipeline.Monitoring, getMonitoringPipeline())

    val credential = TinkoffCredential(
        terminalKey = "1234567890123DEMO",
        secretKey = "n6sd22449gugk8mb"
    )
    val utils = TinkoffUtils(credential)
    val mapper = ObjectMapper()
    val tinkoffClient = TinkoffClient(credential = credential, utils = utils)

    routing {
        post("/init") {
            val request = call.receiveRequest<InitRequest>()
            val response = tinkoffClient.init(request.amount, request.orderId)

            call.respond(
                InitResponse(
                    amount = response.amount ?: 0,
                    paymentId = response.paymentId?.toString() ?: ""
                )
            )
        }

        post("/confirm") {
            val request = call.receiveRequest<ConfirmRequest>()
            tinkoffClient.confirm(request.paymentId.toInt())

            call.respond(ConfirmResponse())
        }
    }
}

class InitRequest(val amount: Int, val orderId: String) : Request()
class ConfirmRequest(val paymentId: String) : Request()

class InitResponse(val amount: Int, val paymentId: String) :
    AbstractResponse(200, "Init payment successful", success = true)

class ConfirmResponse :
    AbstractResponse(200, "Confirm payment successful", success = true)
