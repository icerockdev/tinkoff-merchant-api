/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.tinkoff

data class TinkoffPayload (
    val data: MutableMap<String, Any> = mutableMapOf()
)
