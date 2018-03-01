package com.pipai.adv.backend.battle.engine.log

class BattleLog {
    private val log: MutableList<BattleLogEvent> = mutableListOf()

    private var executionIndex = 0

    fun addEvent(event: BattleLogEvent) {
        log.add(event)
    }

    fun beginExecution() {
        executionIndex = log.size
    }

    fun getEventsDuringExecution(): List<BattleLogEvent> {
        return log.takeLast(log.size - executionIndex)
    }
}
