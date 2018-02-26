package com.pipai.adv.artemis.system.ui

import com.artemis.BaseSystem
import com.artemis.managers.TagManager
import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.components.*
import com.pipai.adv.artemis.events.MouseDownEvent
import com.pipai.adv.artemis.events.MovementTileUpdateEvent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.system.animation.BattleAnimationSystem
import com.pipai.adv.artemis.system.input.SelectedUnitSystem
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.MapGraph
import com.pipai.adv.backend.battle.engine.MoveCommand
import com.pipai.adv.utils.GridUtils
import com.pipai.adv.utils.getLogger
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.EventSystem
import net.mostlyoriginal.api.event.common.Subscribe

class BattleUiSystem(private val game: AdvGame) : BaseSystem() {

    private val logger = getLogger()

    private val mCamera by mapper<OrthographicCameraComponent>()
    private val mInterpolation by mapper<InterpolationComponent>()

    private val mBackend by mapper<BattleBackendComponent>()
    private val mNpcId by mapper<NpcIdComponent>()

    private val sTags by system<TagManager>()
    private val sEvent by system<EventSystem>()

    private val sSelectedUnit by system<SelectedUnitSystem>()
    private val sBattleAnimation by system<BattleAnimationSystem>()

    private var mapGraph: MapGraph? = null

    val stage = Stage(ScreenViewport())

    @Subscribe
    fun movementTileUpdateListener(event: MovementTileUpdateEvent) {
        mapGraph = event.mapGraph
    }

    @Subscribe
    fun mouseDownListener(event: MouseDownEvent) {
        val theMapGraph = mapGraph
        val selectedUnit = sSelectedUnit.selectedUnit
        if (event.button != Input.Buttons.RIGHT || theMapGraph == null || selectedUnit == null) return

        val destination = GridUtils.localToGridPosition(event.x, event.y, game.advConfig.resolution.tileSize.toFloat())
        if (theMapGraph.canMoveTo(destination)) {
            val backend = getBackend()
            val npcId = mNpcId.get(selectedUnit).npcId

            val moveCommand = MoveCommand(npcId, theMapGraph.getPath(destination))

            val executionStatus = backend.canBeExecuted(moveCommand)
            if (executionStatus.executable) {
                val events = backend.execute(moveCommand)
                sBattleAnimation.processBattleEvents(events)
            } else {
                logger.debug("Unable to move: ${executionStatus.reason}")
            }
        }
    }

    private fun getBackend(): BattleBackend {
        return mBackend.get(sTags.getEntityId(Tags.BACKEND.toString())).backend
    }

    override fun processSystem() {
        stage.act()
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
    }

}
