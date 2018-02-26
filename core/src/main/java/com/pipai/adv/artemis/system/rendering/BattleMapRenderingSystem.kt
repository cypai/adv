package com.pipai.adv.artemis.system.rendering

import com.artemis.managers.TagManager
import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.components.*
import com.pipai.adv.artemis.events.MovementTileUpdateEvent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata.MapTilesetMetadata
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata.PccTilesetMetadata
import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.backend.battle.engine.MapGraph
import com.pipai.adv.gui.BatchHelper
import com.pipai.adv.tiles.MapTileset
import com.pipai.adv.tiles.PccFrame
import com.pipai.adv.tiles.PccManager
import com.pipai.adv.tiles.TextureManager
import com.pipai.adv.utils.*
import net.mostlyoriginal.api.event.common.Subscribe

class BattleMapRenderingSystem(private val skin: Skin,
                               private val batch: BatchHelper,
                               private val mapTileset: MapTileset,
                               private val advConfig: AdvConfig,
                               private val pccManager: PccManager,
                               private val textureManager: TextureManager) : IteratingSystem(allOf()) {

    private val mBackend by require<BattleBackendComponent>()

    private val mCamera by mapper<OrthographicCameraComponent>()
    private val mEnvObjTile by mapper<EnvObjTileComponent>()
    private val mXy by mapper<XYComponent>()
    private val mDrawable by mapper<DrawableComponent>()
    private val mAnimationFrames by mapper<AnimationFramesComponent>()
    private val mTileDescriptor by mapper<TileDescriptorComponent>()

    private val sTags by system<TagManager>()

    companion object {
        private val BLUE_MOVE = Color(0.3f, 0.3f, 0.8f, 0.4f)
        private val GREEN_MOVE = Color(0.3f, 0.6f, 0f, 0.4f)
        private val YELLOW_MOVE = Color(0.8f, 0.6f, 0f, 0.4f)
    }

    private var mapGraph: MapGraph? = null
    private val blueMoveDrawable = skin.newDrawable("white", BLUE_MOVE)
    private val greenMoveDrawable = skin.newDrawable("white", GREEN_MOVE)
    private val yellowMoveDrawable = skin.newDrawable("white", YELLOW_MOVE)

    @Subscribe
    fun movementTileUpdateListener(event: MovementTileUpdateEvent) {
        mapGraph = event.mapGraph
    }

    override fun process(entityId: Int) {
        val cBackend = mBackend.get(entityId)
        val mapState = cBackend.backend.getBattleMapState()

        val cameraId = sTags.getEntityId(Tags.CAMERA.toString())
        val camera = mCamera.get(cameraId).camera

        batch.spr.setProjectionMatrix(camera.combined)
        batch.spr.begin()
        renderBackgroundTiles(mapState)

        val theMapGraph = mapGraph
        if (theMapGraph != null) {
            renderMovementTiles(theMapGraph)
        }
        renderMapObjects()
        batch.spr.end()
    }

    private fun renderBackgroundTiles(mapState: BattleMap) {
        val tileSize = advConfig.resolution.tileSize.toFloat()

        for (y in 0 until mapState.height) {
            for (x in 0 until mapState.width) {
                val cell = mapState.cells[x][y]
                for (bgTileInfo in cell.backgroundTiles) {
                    val tile = mapTileset.tiles(bgTileInfo.tileType)[bgTileInfo.index]
                    batch.spr.draw(tile, x.toFloat() * tileSize, y.toFloat() * tileSize, tileSize, tileSize)
                }
            }
        }
    }

    private fun renderMovementTiles(mapGraph: MapGraph) {
        val ap = mapGraph.ap
        val apMax = mapGraph.apMax

        if (ap == 1) {
            renderTileHighlight(mapGraph.getMovableCellPositions(1), yellowMoveDrawable)
            return
        }
        if (apMax == 2) {
            renderTileHighlight(mapGraph.getMovableCellPositions(1), blueMoveDrawable)
            renderTileHighlight(mapGraph.getMovableCellPositions(2), yellowMoveDrawable)
        } else if (apMax == 3) {
            if (ap == 2) {
                renderTileHighlight(mapGraph.getMovableCellPositions(1), greenMoveDrawable)
                renderTileHighlight(mapGraph.getMovableCellPositions(2), yellowMoveDrawable)
            } else {
                renderTileHighlight(mapGraph.getMovableCellPositions(1), blueMoveDrawable)
                renderTileHighlight(mapGraph.getMovableCellPositions(2), greenMoveDrawable)
                renderTileHighlight(mapGraph.getMovableCellPositions(3), yellowMoveDrawable)
            }
        }
    }

    private fun renderTileHighlight(tiles: List<GridPosition>, drawable: Drawable) {
        val tileSize = advConfig.resolution.tileSize.toFloat()
        for (tilePosition in tiles) {
            drawable.draw(batch.spr, tileSize * tilePosition.x, tileSize * tilePosition.y, tileSize, tileSize)
        }
    }

    private fun renderMapObjects() {
        val envObjEntities = world.fetch(allOf(EnvObjTileComponent::class, XYComponent::class, AnimationFramesComponent::class))
                .map { Pair(it, RenderType.ENV_OBJ) }

        val tileDescriptorEntities = world.fetch(allOf(TileDescriptorComponent::class, XYComponent::class))
                .map { Pair(it, RenderType.TILE) }

        val drawableEntities = world.fetch(allOf(DrawableComponent::class, XYComponent::class))
                .map { Pair(it, RenderType.DRAWABLE) }

        val entities: MutableList<Pair<Int, RenderType>> = mutableListOf()
        entities.addAll(envObjEntities)
        entities.addAll(tileDescriptorEntities)
        entities.addAll(drawableEntities)

        val sortedEntities = entities.map { Pair(-mXy.get(it.first).y, it) }
                .sortedBy { it.first }
                .map { it.second }

        val tileSize = advConfig.resolution.tileSize.toFloat()
        for (entityPair in sortedEntities) {
            when (entityPair.second) {
                RenderType.ENV_OBJ -> renderEnvObjTile(entityPair.first, tileSize)
                RenderType.TILE -> renderTileDescriptor(entityPair.first)
                RenderType.DRAWABLE -> renderDrawable(entityPair.first)
            }
        }
    }

    private fun renderEnvObjTile(id: Int, tileSize: Float) {
        val cEnvObjTile = mEnvObjTile.get(id)
        val cXy = mXy.get(id)
        val cAnimationFrames = mAnimationFrames.get(id)

        val tilesetMetadata = cEnvObjTile.tilesetMetadata
        when (tilesetMetadata) {
            is PccTilesetMetadata -> {
                for (pcc in tilesetMetadata.pccMetadata) {
                    val pccTexture = pccManager.getPccFrame(pcc, PccFrame(cEnvObjTile.direction, cAnimationFrames.frame))
                    val scaleFactor = tileSize / pccTexture.regionWidth
                    batch.spr.draw(pccTexture, cXy.x, cXy.y, tileSize, pccTexture.regionHeight * scaleFactor)
                }
            }
            is MapTilesetMetadata -> {
                batch.spr.draw(mapTileset.tiles(tilesetMetadata.mapTileType)[0], cXy.x, cXy.y, tileSize, tileSize)
            }
        }
    }

    private fun renderTileDescriptor(id: Int) {
        val cTileDescriptor = mTileDescriptor.get(id)
        val cXy = mXy.get(id)
        batch.spr.draw(textureManager.getTile(cTileDescriptor.descriptor), cXy.x, cXy.y)
    }

    private fun renderDrawable(id: Int) {
        val cDrawable = mDrawable.get(id)
        val cXy = mXy.get(id)
        cDrawable.drawable.draw(batch.spr, cXy.x, cXy.y, cDrawable.width, cDrawable.height)
    }

    private enum class RenderType {
        ENV_OBJ, TILE, DRAWABLE
    }
}
