package com.pipai.adv.artemis.system.cutscene

import com.artemis.managers.TagManager
import com.pipai.adv.artemis.components.BattleBackendComponent
import com.pipai.adv.artemis.events.BattleEventAnimationEndEvent
import com.pipai.adv.artemis.events.CutsceneEvent
import com.pipai.adv.artemis.events.DirectorEndEvent
import com.pipai.adv.artemis.events.DirectorsFinishedEvent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.artemis.system.cutscene.directors.BattleCutsceneDirector
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.EventSystem
import net.mostlyoriginal.api.event.common.Subscribe

class BattleCutsceneSystem(private val directors: List<BattleCutsceneDirector>) : NoProcessingSystem() {

    private val mBackend by mapper<BattleBackendComponent>()

    private val sTags by system<TagManager>()
    private val sEvent by system<EventSystem>()

    private val directorsQueue: MutableList<BattleCutsceneDirector> = directors.toMutableList()

    @Subscribe
    fun handleBattleEventAnimationEnd(@Suppress("UNUSED_PARAMETER") event: BattleEventAnimationEndEvent) {
        checkAllCutsceneDirectors()
    }

    @Subscribe
    fun handleCutsceneEnd(event: CutsceneEvent) {
        if (!event.start) {
            checkCutsceneDirectors()
        }
    }

    @Subscribe
    fun handleDirectorEnd(@Suppress("UNUSED_PARAMETER") event: DirectorEndEvent) {
        checkCutsceneDirectors()
    }

    private fun getBackend(): BattleBackend = mBackend.get(sTags.getEntity(Tags.BACKEND.toString())).backend

    fun checkAllCutsceneDirectors() {
        directorsQueue.addAll(directors)
        checkCutsceneDirectors()
    }

    fun checkCutsceneDirectors() {
        if (directorsQueue.isEmpty()) {
            sEvent.dispatch(DirectorsFinishedEvent())
        } else {
            val backend = getBackend()
            val director = directorsQueue.removeAt(0)
            val pause = director.check(backend)
            if (!pause) {
                checkCutsceneDirectors()
            }
        }
    }

}
