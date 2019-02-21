package com.pipai.adv.backend.progression

import java.time.Duration
import java.time.LocalDateTime

class TimeBackend(var time: LocalDateTime) {

    private val updateTimespan: Duration = Duration.ofHours(2)

    private val callbacks: MutableList<() -> Unit> = mutableListOf()

    fun addCallback(callback: () -> Unit) {
        callbacks.add(callback)
    }

    fun update() {
        time = time.plus(updateTimespan)
        callbacks.forEach { it() }
    }
}
