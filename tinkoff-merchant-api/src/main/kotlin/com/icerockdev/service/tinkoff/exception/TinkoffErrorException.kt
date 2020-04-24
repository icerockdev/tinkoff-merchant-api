/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.tinkoff.exception

class TinkoffErrorException(val code: Int, override val message: String): Throwable(message)
