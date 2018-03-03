package com.pipai.adv.artemis.system.ui.menu

import com.pipai.adv.backend.battle.engine.commands.ActionCommand
import com.pipai.adv.backend.battle.engine.commands.ActionCommandFactory

interface MenuCommandItem<out T : ActionCommand> : MenuItem {

    override val rightText: String
        get() = rightText()

    val factory: ActionCommandFactory<T>

    fun rightText(): String
}
