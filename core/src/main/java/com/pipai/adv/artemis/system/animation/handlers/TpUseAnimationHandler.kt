package com.pipai.adv.artemis.system.animation.handlers

import com.artemis.ComponentMapper
import com.artemis.World
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.components.SideUiBoxComponent
import com.pipai.adv.artemis.events.BattleEventAnimationEndEvent
import com.pipai.adv.backend.battle.engine.log.TpChangeEvent
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.fetch
import net.mostlyoriginal.api.event.common.EventSystem

class TpUseAnimationHandler(val config: AdvConfig, private val world: World) {

    private lateinit var mSideUiBox: ComponentMapper<SideUiBoxComponent>

    private lateinit var sEvent: EventSystem

    init {
        world.inject(this)
    }

    fun animate(event: TpChangeEvent) {
        updateSideUiTp(event)
        sEvent.dispatch(BattleEventAnimationEndEvent(event))
    }

    private fun updateSideUiTp(event: TpChangeEvent) {
        world.fetch(allOf(SideUiBoxComponent::class))
                .firstOrNull { mSideUiBox.get(it).npcId == event.npcId }
                ?.let {
                    val cSideUiBox = mSideUiBox.get(it)
                    cSideUiBox.tp = event.newTpAmount
                }
    }

}
