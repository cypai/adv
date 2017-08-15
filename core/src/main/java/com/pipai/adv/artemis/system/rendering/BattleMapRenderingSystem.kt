package com.pipai.adv.artemis.system.rendering

import com.artemis.managers.TagManager
import com.artemis.systems.IteratingSystem
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.components.AnimationFramesComponent
import com.pipai.adv.artemis.components.BattleBackendComponent
import com.pipai.adv.artemis.components.EnvObjTileComponent
import com.pipai.adv.artemis.components.OrthographicCameraComponent
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata.MapTilesetMetadata
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata.PccTilesetMetadata
import com.pipai.adv.gui.BatchHelper
import com.pipai.adv.tiles.MapTileset
import com.pipai.adv.tiles.PccFrame
import com.pipai.adv.tiles.PccManager
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.require
import com.pipai.adv.utils.system

class BattleMapRenderingSystem(private val batch: BatchHelper,
                               private val mapTileset: MapTileset,
                               private val advConfig: AdvConfig,
                               private val pccManager: PccManager) : IteratingSystem(allOf()) {

    private val mBackend by require<BattleBackendComponent>()

    private val mCamera by mapper<OrthographicCameraComponent>()
    private val mEnvObjTile by mapper<EnvObjTileComponent>()
    private val mXy by mapper<XYComponent>()
    private val mAnimationFrames by mapper<AnimationFramesComponent>()

    private val sTags by system<TagManager>()

    override protected fun process(entityId: Int) {
        val cBackend = mBackend.get(entityId)
        val mapState = cBackend.backend.getBattleMapState()

        val cameraId = sTags.getEntityId(Tags.CAMERA.toString())
        val camera = mCamera.get(cameraId).camera

        batch.spr.setProjectionMatrix(camera.combined)
        batch.spr.begin()
        renderBackgroundTiles(mapState)
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

    private fun renderMapObjects() {
        val tileSize = advConfig.resolution.tileSize.toFloat()

        val envObjEntityBag = world.aspectSubscriptionManager.get(allOf(
                EnvObjTileComponent::class, XYComponent::class, AnimationFramesComponent::class)).entities
        val entities = envObjEntityBag.data.slice(0 until envObjEntityBag.size())

        val sortedEntities = entities.map { it -> Pair(-mXy.get(it).y, it) }.sortedBy { it.first }

        for (envObjTilePair in sortedEntities) {
            renderEnvObjTile(envObjTilePair.second, tileSize)
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
}
