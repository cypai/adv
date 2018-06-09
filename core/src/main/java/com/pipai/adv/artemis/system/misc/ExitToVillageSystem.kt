package com.pipai.adv.artemis.system.misc

import com.artemis.BaseSystem
import com.artemis.managers.TagManager
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.screens.VillageScreen
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system

class ExitToVillageSystem(private val game: AdvGame) : BaseSystem() {

    private val mXy by mapper<XYComponent>()

    private val sTags by system<TagManager>()

    override fun processSystem() {
        val playerEntityId = sTags.getEntityId(Tags.CONTROLLABLE_CHARACTER.toString())
        val cXy = mXy.get(playerEntityId)
        if (cXy.y < -16) {
            game.screen = VillageScreen(game)
        }
    }
}
