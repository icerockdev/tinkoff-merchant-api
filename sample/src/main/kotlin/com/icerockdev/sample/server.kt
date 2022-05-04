/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.sample

import com.icerockdev.api.AbstractResponse
import com.icerockdev.api.Request
import com.icerockdev.api.request.QueryParser
import com.icerockdev.service.tinkoff.TinkoffClient
import com.icerockdev.service.tinkoff.TinkoffCredential
import com.icerockdev.service.tinkoff.TinkoffUtils
import com.icerockdev.webserver.applyCallConfiguration
import com.icerockdev.webserver.applyDefaultCORS
import com.icerockdev.webserver.applyDefaultConfiguration
import com.icerockdev.webserver.applyDefaultLogging
import com.icerockdev.webserver.applyDefaultStatusConfiguration
import com.icerockdev.webserver.log.ApplicationCallLogging
import com.icerockdev.webserver.tools.receiveRequest
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.CallId
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import org.slf4j.LoggerFactory

fun Application.main() {
    install(StatusPages) {
        applyDefaultStatusConfiguration(
            logger = LoggerFactory.getLogger(Application::class.java)
        )
    }
    install(CORS) {
        applyDefaultCORS()
        anyHost() // TODO: Don't do this in production if possible. Try to limit it.
    }
    install(DefaultHeaders)
    install(ApplicationCallLogging) {
        applyDefaultLogging()
    }

    install(CallId) {
        applyCallConfiguration()
    }
    install(ContentNegotiation) {
        jackson {
            applyDefaultConfiguration()
        }
    }
    install(QueryParser) {
        mapperConfiguration = {
            applyDefaultConfiguration()
        }
    }

    val credential = TinkoffCredential(
        terminalKey = "", // set terminal key from personal account
        secretKey = "" // set terminal password from personal account
    )
    val utils = TinkoffUtils(credential)
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
