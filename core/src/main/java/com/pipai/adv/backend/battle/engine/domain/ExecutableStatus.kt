package com.pipai.adv.backend.battle.engine.domain

data class ExecutableStatus(val executable: Boolean, val reason: String?) {
    companion object {
        @JvmStatic
        val COMMAND_OK = ExecutableStatus(true, null)
    }
}
