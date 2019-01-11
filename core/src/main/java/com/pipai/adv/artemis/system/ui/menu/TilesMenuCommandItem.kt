package com.pipai.adv.artemis.system.ui.menu

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.pipai.adv.backend.battle.engine.commands.ActionCommandFactory
import com.pipai.adv.backend.battle.engine.commands.PositionCommand

class TilesMenuCommandItem(override val text: String,
                           override val image: TextureRegion?,
                           override val rightText: String,
                           override val factory: ActionCommandFactory<PositionCommand>) : MenuCommandItem<PositionCommand> {

    constructor(text: String, image: TextureRegion?, factory: ActionCommandFactory<PositionCommand>)
            : this(text, image, "", factory)

}
