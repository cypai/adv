package com.pipai.adv.artemis.system.cutscene.directors

import com.artemis.World
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.events.DirectorEndEvent
import com.pipai.adv.artemis.system.misc.CameraInterpolationSystem
import com.pipai.adv.artemis.system.rendering.BattleMapRenderingSystem
import com.pipai.adv.backend.battle.domain.Team
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.rules.ending.ItemRetrievalEndingRule
import com.pipai.adv.backend.battle.engine.rules.ending.MapClearEndingRule
import com.pipai.adv.map.TileVisibility
import com.pipai.adv.utils.GridUtils
import net.mostlyoriginal.api.event.common.EventSystem

class OpeningDirector(private val config: AdvConfig, world: World) : BattleCutsceneDirector {

    private lateinit var sCameraInterpolation: CameraInterpolationSystem
    private lateinit var sBattleMapRenderer: BattleMapRenderingSystem

    private lateinit var sEvent: EventSystem

    private var shown = false

    init {
        world.inject(this)
    }

    override fun check(backend: BattleBackend): Boolean {
        if (!shown) {
            shown = true
            val goal = backend.objective
            when (goal) {
                is MapClearEndingRule -> {
                    println("Objective: Clear the map")
                }
                is ItemRetrievalEndingRule -> {
                    sBattleMapRenderer.fogOfWar.setPlayerTileVisibility(goal.itemPosition, TileVisibility.SEEN)
                    sCameraInterpolation.sendCameraToPosition(
                            GridUtils.gridPositionToLocal(goal.itemPosition, config.resolution.tileSize.toFloat()),
                            { handleItemRetrievalCutscene(backend) })
                    return true
                }
            }
        }
        return false
    }

    private fun handleItemRetrievalCutscene(backend: BattleBackend) {
        val playerPosition = backend.getNpcPosition(backend.getTeam(Team.PLAYER).first())!!
        sCameraInterpolation.sendCameraToPosition(
                GridUtils.gridPositionToLocal(playerPosition, config.resolution.tileSize.toFloat()),
                { sEvent.dispatch(DirectorEndEvent()) })
    }

}
