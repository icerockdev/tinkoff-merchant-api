/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.tinkoff.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class GetStateResponse(
    @JsonProperty("Success")
    override val success: Boolean,
    @JsonProperty("ErrorCode")
    override val errorCode: String,
    @JsonProperty("PaymentId")
    val paymentId: Int?,
    @JsonProperty("NewAmount")
    val newAmount: Int?,
    @JsonProperty("Status")
    val status: String?
) : Response
