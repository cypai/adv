package com.pipai.adv.artemis.system.ui.menu

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.pipai.adv.backend.battle.engine.commands.ActionCommandFactory
import com.pipai.adv.backend.battle.engine.commands.TargetCommand

class TargetMenuCommandItem(override val text: String,
                            override val image: TextureRegion?,
                            override val factory: ActionCommandFactory<TargetCommand>) : MenuCommandItem<TargetCommand> {

    override fun rightText(): String = ""
}
