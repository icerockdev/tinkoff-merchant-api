/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.tinkoff

import com.fasterxml.jackson.annotation.JsonInclude
import com.icerockdev.service.tinkoff.exception.TinkoffValidationException
import com.icerockdev.service.tinkoff.response.CancelResponse
import com.icerockdev.service.tinkoff.response.CardListResponse
import com.icerockdev.service.tinkoff.response.CardResponse
import com.icerockdev.service.tinkoff.response.ChargeResponse
import com.icerockdev.service.tinkoff.response.ConfirmResponse
import com.icerockdev.service.tinkoff.response.CustomerResponse
import com.icerockdev.service.tinkoff.response.EmptyResponse
import com.icerockdev.service.tinkoff.response.GetStateResponse
import com.icerockdev.service.tinkoff.response.InitResponse
import com.icerockdev.service.tinkoff.response.ResendResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.jackson.jackson
import kotlin.math.abs
import kotlin.math.log10

class TinkoffClient(
    client: HttpClient,
    credential: TinkoffCredential,
    utils: TinkoffUtils
) {

    constructor(credential: TinkoffCredential, utils: TinkoffUtils) : this(
        client = HttpClient(Apache) {
            install(ContentNegotiation) {
                jackson {
                    setSerializationInclusion(JsonInclude.Include.NON_NULL)
                }
            }
            install(Logging) {
                logger = Logger.DEFAULT
                this.level = LogLevel.INFO
            }
        },
        credential = credential,
        utils = utils
    )

    private val api: TinkoffMerchantAPI = TinkoffMerchantAPI(client = client, credential = credential, utils = utils)

    /**
     * @see <a href="https://oplata.tinkoff.ru/develop/api/payments/init-description/">Init</a>
     */
    suspend fun init(
        amount: Int,
        orderId: String,
        payType: String = TinkoffPayType.ONE_STAGE.value,
        language: String = TinkoffFormLanguage.RU.value, // Russian is default
        customerKey: String? = null,
        recurrent: Boolean = false,
        ip: String? = null,
        description: String? = null,
        notificationURL: String? = null,
        successURL: String? = null,
        failURL: String? = null
    ): InitResponse {
        validateIntLength("Amount", amount, 1, 10)
        validateStringLength("OrderId", orderId, 1, 20)
        validateInEnum("PayType", payType, TinkoffPayType.valuesList())
        validateInEnum("Language", language, TinkoffFormLanguage.valuesList())

        val payload = TinkoffPayload()
        payload.data["Amount"] = amount
        payload.data["OrderId"] = orderId
        payload.data["PayType"] = payType
        payload.data["Language"] = language

        if (recurrent && customerKey == null) {
            throw TinkoffValidationException("CustomerKey is required for recurrent payment.")
        }

        if (recurrent) {
            payload.data["Recurrent"] = TINKOFF_RECURRENT_VALUE
        }

        if (customerKey != null) {
            validateStringLength("CustomerKey", customerKey, 1, 36)
            payload.data["CustomerKey"] = customerKey
        }

        if (ip != null) {
            validateStringLength("IP", ip, 7, 40)
            payload.data["IP"] = ip
        }

        if (description != null) {
            validateStringLength("IP", description, 1, 250)
            payload.data["Description"] = description
        }

        if (notificationURL != null) {
            payload.data["NotificationURL"] = notificationURL
        }

        if (successURL != null) {
            payload.data["SuccessURL"] = successURL
        }

        if (failURL != null) {
            payload.data["FailURL"] = failURL
        }

        return api.buildQuery("Init", payload)
    }

    /**
     * @see <a href="https://oplata.tinkoff.ru/develop/api/payments/confirm-description/">Confirm</a>
     */
    suspend fun confirm(paymentId: Int, amount: Int? = null, ip: String? = null): ConfirmResponse {
        val payload = getPaymentPayload(paymentId, amount, ip)

        return api.buildQuery("Confirm", payload)
    }

    /**
     * @see <a href="https://oplata.tinkoff.ru/develop/api/payments/cancel-description/">Cancel</a>
     */
    suspend fun cancel(paymentId: Int, amount: Int? = null, ip: String? = null): CancelResponse {
        val payload = getPaymentPayload(paymentId, amount, ip)

        return api.buildQuery("Cancel", payload)
    }

    /**
     * @see <a href="https://oplata.tinkoff.ru/develop/api/payments/getstate-description/">GetState</a>
     */
    suspend fun getState(paymentId: Int, ip: String? = null): GetStateResponse {
        validateIntLength("PaymentId", paymentId, 1, 20)

        val payload = TinkoffPayload()
        payload.data["PaymentId"] = paymentId

        if (ip != null) {
            validateStringLength("IP", ip, 7, 40)
            payload.data["IP"] = ip
        }

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
     * @see <a href="https://oplata.tinkoff.ru/develop/api/payments/charge-description/">Charge</a>
     */
    suspend fun charge(paymentId: Int, rebillId: Int, ip: String? = null): ChargeResponse {
        validateIntLength("PaymentId", paymentId, 1, 20)
        validateIntLength("RebillId", rebillId, 1, 20)

        val payload = TinkoffPayload()
        payload.data["PaymentId"] = paymentId
        payload.data["RebillId"] = rebillId

        if (ip != null) {
            validateStringLength("IP", ip, 7, 40)
            payload.data["IP"] = ip
        }

        return api.buildQuery("Charge", payload)
    }

    /**
     * @see <a href="https://oplata.tinkoff.ru/develop/api/autopayments/getcustomer-description/">GetCustomer</a>
     */
    suspend fun getCustomer(customerKey: String, ip: String? = null): CustomerResponse {
        val payload = getCustomerPayload(customerKey, ip)

        return api.buildQuery("GetCustomer", payload)
    }

    /**
     * @see <a href="https://oplata.tinkoff.ru/develop/api/autopayments/addcustomer-description/">AddCustomer</a>
     */
    suspend fun addCustomer(
        customerKey: String,
        phone: String? = null,
        email: String? = null,
        ip: String? = null
    ): EmptyResponse {
        val payload = getCustomerPayload(customerKey, ip)

        if (phone != null) {
            validateStringLength("Phone", phone, 11, 15)
            payload.data["Phone"] = phone
        }
        if (email != null) {
            validateStringLength("Email", email, 6, 100)
            payload.data["Email"] = email
        }

        return api.buildQuery("AddCustomer", payload)
    }

    /**
     * @see <a href="https://oplata.tinkoff.ru/develop/api/autopayments/removecustomer-description/">RemoveCustomer</a>
     */
    suspend fun removeCustomer(customerKey: String, ip: String? = null): EmptyResponse {
        val payload = getCustomerPayload(customerKey, ip)

        return api.buildQuery("RemoveCustomer", payload)
    }

    /**
     * @see <a href="https://oplata.tinkoff.ru/develop/api/autopayments/getcardlist-description/">GetCardList</a>
     */
    suspend fun getCardList(customerKey: String, ip: String? = null): CardListResponse {
        val payload = getCustomerPayload(customerKey, ip)

        return api.buildQuery("GetCardList", payload)
    }

    /**
     * @see <a href="https://oplata.tinkoff.ru/develop/api/autopayments/removecard-description/">RemoveCard</a>
     */
    suspend fun removeCard(customerKey: String, cardId: String, ip: String? = null): CardResponse {
        val payload = getCustomerPayload(customerKey, ip)

        validateStringLength("CardId", cardId, 1, 40)
        payload.data["CardId"] = cardId

        return api.buildQuery("RemoveCard", payload)
    }

    private fun getPaymentPayload(paymentId: Int, amount: Int? = null, ip: String? = null): TinkoffPayload {
        validateIntLength("PaymentId", paymentId, 1, 20)

        val payload = TinkoffPayload()
        payload.data["PaymentId"] = paymentId

        if (amount != null) {
            validateIntLength("Amount", amount, 1, 10)
            payload.data["Amount"] = amount
        }

        if (ip != null) {
            validateStringLength("IP", ip, 7, 40)
            payload.data["IP"] = ip
        }

        return payload
    }

    private fun getCustomerPayload(customerKey: String, ip: String? = null): TinkoffPayload {
        val payload = TinkoffPayload()
        payload.data["CustomerKey"] = customerKey

        if (ip != null) {
            validateStringLength("IP", ip, 7, 40)
            payload.data["IP"] = ip
        }

        return payload
    }

    private fun validateStringLength(name: String, value: String, min: Int, max: Int) {
        if (value.length < min || value.length > max) {
            throw TinkoffValidationException("$name length should be between $min anf $max.")
        }
    }

    @Suppress("SameParameterValue")
    private fun validateIntLength(name: String, value: Int, min: Int, max: Int) {
        val length = when (value) {
            0 -> 1
            else -> log10(abs(value.toDouble())).toInt() + 1
        }
        if (length < min || length > max) {
            throw TinkoffValidationException("$name length should be between $min anf $max.")
        }
    }

    private fun validateInEnum(name: String, value: String, list: List<String>) {
        if (!list.contains(value)) {
            throw TinkoffValidationException("$name should be in list (${list.joinToString(", ")}).")
        }
    }
}
