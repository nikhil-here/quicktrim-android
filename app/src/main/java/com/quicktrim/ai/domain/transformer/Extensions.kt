package com.quicktrim.ai.domain.transformer

import kotlin.math.roundToLong

fun Double.toMs(): Long {
    return (this * 1_000L).roundToLong()
}