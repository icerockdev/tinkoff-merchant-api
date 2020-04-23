/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.tinkoff

import com.fasterxml.jackson.annotation.JsonInclude
import com.icerockdev.service.tinkoff.response.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging

class TinkoffClient(
    client: HttpClient = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = JacksonSerializer {
                setSerializationInclusion(JsonInclude.Include.NON_NULL);
            }
        }
        install(Logging) {
            logger = Logger.DEFAULT
            this.level = LogLevel.INFO
        }
    },
    credential: TinkoffCredential,
    utils: TinkoffUtils
) {
    private val api: TinkoffMerchantAPI = TinkoffMerchantAPI(client = client, credential = credential, utils = utils);

    /**
     * @see <a href="https://oplata.tinkoff.ru/develop/api/payments/init-description/">Init</a>
     */
    suspend fun init(
        amount: Int,
        orderId: String,
        payType: String = TinkoffPayType.ONE_STAGE.value,
        customerKey: String? = null
    ): InitResponse {
        val payload = TinkoffPayload()
        payload.data["Amount"] = amount
        payload.data["OrderId"] = orderId
        payload.data["PayType"] = payType

        // Attach card
        if (customerKey != null) {
            payload.data["CustomerKey"] = customerKey
        }

        return api.buildQuery("Init", payload)
    }

    /**
     * @see <a href="https://oplata.tinkoff.ru/develop/api/payments/confirm-description/">Confirm</a>
     */
    suspend fun confirm(paymentId: Int): ConfirmResponse {
        val payload = TinkoffPayload()
        payload.data["PaymentId"] = paymentId

        return api.buildQuery("Confirm", payload)
    }

    /**
     * @see <a href="https://oplata.tinkoff.ru/develop/api/payments/cancel-description/">Cancel</a>
     */
    suspend fun cancel(paymentId: Int): CancelResponse {
        val payload = TinkoffPayload()
        payload.data["PaymentId"] = paymentId

        return api.buildQuery("Cancel", payload)
    }

    /**
     * @see <a href="https://oplata.tinkoff.ru/develop/api/payments/getstate-description/">GetState</a>
     */
    suspend fun getState(paymentId: Int): GetStateResponse {
        val payload = TinkoffPayload()
        payload.data["PaymentId"] = paymentId

        return api.buildQuery("GetState", payload)
    }

    /**
     * @see <a href="https://oplata.tinkoff.ru/develop/api/payments/resend-description/">Resend</a>
     */
    suspend fun resend(): ResendResponse {
        val payload = TinkoffPayload()

        return api.buildQuery("Resend", payload)
    }

    /**
     * @see <a href="https://oplata.tinkoff.ru/develop/api/autopayments/getcustomer-description/">GetCustomer</a>
     */
    suspend fun getCustomer() {
        // TODO: Implement this method
    }

    /**
     * @see <a href="https://oplata.tinkoff.ru/develop/api/autopayments/addcustomer-description/">AddCustomer</a>
     */
    suspend fun addCustomer(customerKey: String, phone: String? = null, email: String? = null): EmptyResponse {
        val payload = TinkoffPayload()
        payload.data["CustomerKey"] = customerKey
        if (phone != null) {
            payload.data["Phone"] = phone
        }
        if (email != null) {
            payload.data["Email"] = email
        }

        return api.buildQuery("AddCustomer", payload)
    }

    /**
     * @see <a href="https://oplata.tinkoff.ru/develop/api/autopayments/removecustomer-description/">RemoveCustomer</a>
     */
    suspend fun removeCustomer() {
        // TODO: Implement this method
    }

    /**
     * @see <a href="https://oplata.tinkoff.ru/develop/api/autopayments/getcardlist-description/">GetCardList</a>
     */
    suspend fun getCardList(customerKey: String): CardListResponse {
        val payload = TinkoffPayload()
        payload.data["CustomerKey"] = customerKey

        return api.buildQuery("GetCardList", payload)
    }
}
