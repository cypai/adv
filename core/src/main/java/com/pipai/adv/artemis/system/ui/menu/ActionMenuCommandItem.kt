package com.pipai.adv.artemis.system.ui.menu

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.pipai.adv.backend.battle.engine.commands.ActionCommand
import com.pipai.adv.backend.battle.engine.commands.ActionCommandFactory

class ActionMenuCommandItem(override val text: String,
                            override val image: TextureRegion?,
                            override val rightText: String,
                            override val factory: ActionCommandFactory<ActionCommand>) : MenuCommandItem<ActionCommand> {

    constructor(text: String, image: TextureRegion?, factory: ActionCommandFactory<ActionCommand>)
            : this(text, image, "", factory)

}
