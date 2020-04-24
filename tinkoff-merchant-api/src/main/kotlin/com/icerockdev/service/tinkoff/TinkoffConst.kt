/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.tinkoff

const val TINKOFF_RECURRENT_VALUE = "Y"

enum class TinkoffFormLanguage(val value: String) {
    RU("ru"),
    EN("en");

    companion object {
        fun valuesList(): ArrayList<String> = arrayListOf(
            RU.value,
            EN.value
        )
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
        fun valuesList(): ArrayList<String> = arrayListOf(
            NEW.value,
            CANCELED.value,
            FORMSHOWED.value,
            DEADLINE_EXPIRED.value,
            AUTHORIZING.value,
            THREE_DS_CHECKING.value,
            THREE_DS_CHECKED.value,
            AUTH_FAIL.value,
            AUTHORIZED.value,
            REVERSING.value,
            REVERSED.value,
            CONFIRMING.value,
            CONFIRMED.value,
            REFUNDING.value,
            PARTIAL_REFUNDED.value,
            REFUNDED.value,
            REJECTED.value
        )
    }
}

enum class TinkoffPayType(val value: String) {
    ONE_STAGE("О"),
    TWO_STAGE("T");

    companion object {
        fun valuesList(): ArrayList<String> = arrayListOf(
            ONE_STAGE.value,
            TWO_STAGE.value
        )
    }
}

