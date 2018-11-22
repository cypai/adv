package com.pipai.adv.artemis.system.cutscene

import com.artemis.managers.TagManager
import com.pipai.adv.artemis.components.BattleBackendComponent
import com.pipai.adv.artemis.events.BattleEventAnimationEndEvent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.artemis.system.cutscene.directors.BattleCutsceneDirector
import com.pipai.adv.artemis.system.input.CutsceneInputSystem
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.Subscribe

class BattleCutsceneSystem(private val directors: List<BattleCutsceneDirector>) : NoProcessingSystem() {

    private val mBackend by mapper<BattleBackendComponent>()

    private val sCutscene by system<CutsceneInputSystem>()
    private val sTags by system<TagManager>()

    @Subscribe
    fun handleBattleEventAnimationEnd(@Suppress("UNUSED_PARAMETER") event: BattleEventAnimationEndEvent) {
        checkCutsceneDirectors()
    }

    private fun getBackend(): BattleBackend = mBackend.get(sTags.getEntity(Tags.BACKEND.toString())).backend

    fun checkCutsceneDirectors() {
        val backend = getBackend()
        directors.forEach { it.check(backend) }
    }

}
