/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.tinkoff

const val TINKOFF_RECURRENT_VALUE = "Y"

enum class TinkoffFormLanguage(val value: String) {
    RU("ru"),
    EN("en");

    companion object {
        fun valuesList(): List<String> {
            return values().map { it.value }
        }
    }
}

enum class TinkoffStatus(val value: String) {
    NEW("NEW"),
    CANCELED("CANCELED"),
    FORMSHOWED("FORMSHOWED"),
    DEADLINE_EXPIRED("DEADLINE_EXPIRED"),
    AUTHORIZING("AUTHORIZING"),
    THREE_DS_CHECKING("3DS_CHECKING"),
    THREE_DS_CHECKED("3DS_CHECKED"),
    AUTH_FAIL("AUTH_FAIL"),
    AUTHORIZED("AUTHORIZED"),
    REVERSING("REVERSING"),
    REVERSED("REVERSED"),
    CONFIRMING("CONFIRMING"),
    CONFIRMED("CONFIRMED"),
    REFUNDING("REFUNDING"),
    PARTIAL_REFUNDED("PARTIAL_REFUNDED"),
    REFUNDED("REFUNDED"),
    REJECTED("REJECTED");

    companion object {
        fun valuesList(): List<String> {
            return values().map { it.value }
        }
    }
}

enum class TinkoffPayType(val value: String) {
    ONE_STAGE("О"),
    TWO_STAGE("T");

    companion object {
        fun valuesList(): List<String> {
            return values().map { it.value }
        }
    }
}

// TODO: Describe all error codes
enum class TinkoffErrorCode(val value: Int) {
    NONE(0),
    INTERNAL(9999);

    companion object {
        fun valuesList(): List<Int> {
            return values().map { it.value }
        }
    }
}
