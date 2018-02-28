package com.pipai.adv.artemis.system.ui

import com.artemis.BaseSystem
import com.artemis.managers.TagManager
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.components.*
import com.pipai.adv.artemis.events.MouseDownEvent
import com.pipai.adv.artemis.events.MouseHoverEvent
import com.pipai.adv.artemis.events.MovementTileUpdateEvent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.system.animation.BattleAnimationSystem
import com.pipai.adv.artemis.system.input.SelectedUnitSystem
import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.MapGraph
import com.pipai.adv.backend.battle.engine.MoveCommand
import com.pipai.adv.utils.GridUtils
import com.pipai.adv.utils.getLogger
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.Subscribe

class BattleFieldUiSystem(private val game: AdvGame) : BaseSystem() {

    private val logger = getLogger()

    private val mDrawable by mapper<DrawableComponent>()
    private val mXy by mapper<XYComponent>()
    private val mPath by mapper<PathInterpolationComponent>()

    private val mBackend by mapper<BattleBackendComponent>()
    private val mNpcId by mapper<NpcIdComponent>()

    private val sTags by system<TagManager>()

    private val sSelectedUnit by system<SelectedUnitSystem>()
    private val sBattleAnimation by system<BattleAnimationSystem>()

    private val previewDrawable = game.skin.newDrawable("white", Color(0.3f, 0.3f, 0.8f, 0.7f))
    private val previewDrawableSize = 8f

    private var mapGraph: MapGraph? = null
    private var hoverDestination: GridPosition? = null
    private var movePreviewEntityId: Int? = null

    val stage = Stage(ScreenViewport())

    @Subscribe
    fun movementTileUpdateListener(event: MovementTileUpdateEvent) {
        mapGraph = event.mapGraph

        if (event.mapGraph == null) {
            movePreviewEntityId?.let { world.delete(it) }
        }
    }

    @Subscribe
    fun mouseHoverListener(event: MouseHoverEvent) {
        val theMapGraph = mapGraph

        if (theMapGraph != null) {
            val tileSize = game.advConfig.resolution.tileSize.toFloat()

            val destination = GridUtils.localToGridPosition(event.x, event.y, tileSize)
            if (destination != hoverDestination && theMapGraph.canMoveTo(destination)) {
                hoverDestination = destination

                val path = theMapGraph.getPath(destination).map {
                    GridUtils.gridPositionToLocalOffset(it, tileSize,
                            tileSize / 2f - previewDrawableSize / 2,
                            tileSize / 2f - previewDrawableSize / 2)
                }
                val start = GridUtils.gridPositionToLocalOffset(theMapGraph.start, tileSize,
                        tileSize / 2f - previewDrawableSize / 2,
                        tileSize / 2f - previewDrawableSize / 2)

                val previewPath = path.toMutableList()
                previewPath.add(0, start)
                createMovePreview(previewPath.toList())
            }
        }
    }

    private fun createMovePreview(path: List<Vector2>) {
        movePreviewEntityId?.let { world.delete(it) }

        val previewId = world.create()

        val cDrawable = mDrawable.create(previewId)
        cDrawable.drawable = previewDrawable
        cDrawable.width = previewDrawableSize
        cDrawable.height = previewDrawableSize
        val cXy = mXy.create(previewId)
        cXy.setXy(path.first())
        val cPath = mPath.create(previewId)
        cPath.onEnd = PathInterpolationEndStrategy.RESTART
        cPath.interpolation = Interpolation.linear
        cPath.maxT = 5
        cPath.endpoints.addAll(path)

        movePreviewEntityId = previewId
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
