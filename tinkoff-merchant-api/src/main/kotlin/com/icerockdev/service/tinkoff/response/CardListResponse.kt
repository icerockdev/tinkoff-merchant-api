/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.tinkoff.response

class CardListResponse(
    override val success: Boolean,
    override val errorCode: String
) : ArrayList<CardResponse>(), Response
