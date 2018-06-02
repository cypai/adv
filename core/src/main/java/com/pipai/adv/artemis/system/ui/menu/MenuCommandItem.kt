package com.pipai.adv.artemis.system.ui.menu

import com.pipai.adv.backend.battle.engine.commands.ActionCommand
import com.pipai.adv.backend.battle.engine.commands.ActionCommandFactory

interface MenuCommandItem<out T : ActionCommand> : MenuItem {
    val factory: ActionCommandFactory<T>
}
