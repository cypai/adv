package com.pipai.adv.artemis.system.rendering

import com.artemis.managers.TagManager
import com.artemis.systems.IteratingSystem
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.components.BattleBackendComponent
import com.pipai.adv.artemis.components.OrthographicCameraComponent
import com.pipai.adv.artemis.components.PccComponent
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.artemis.screens.BattleMapScreenTags
import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.gui.BatchHelper
import com.pipai.adv.tiles.MapTileset
import com.pipai.adv.tiles.PccFrame
import com.pipai.adv.tiles.PccManager
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.require
import com.pipai.adv.utils.system
import com.pipai.adv.artemis.components.AnimationFramesComponent

class BattleMapRenderingSystem(private val batch: BatchHelper,
                               private val mapTileset: MapTileset,
                               private val advConfig: AdvConfig,
                               private val pccManager: PccManager) : IteratingSystem(allOf()) {

    private val mBackend by require<BattleBackendComponent>()

    private val mCamera by mapper<OrthographicCameraComponent>()
    private val mPccs by mapper<PccComponent>()
    private val mXy by mapper<XYComponent>()
    private val mAnimationFrames by mapper<AnimationFramesComponent>()

    private val sTags by system<TagManager>()

    override protected fun process(entityId: Int) {
        val cBackend = mBackend.get(entityId)
        val mapState = cBackend.backend.getBattleMapState()

        val cameraId = sTags.getEntityId(BattleMapScreenTags.CAMERA.toString())
        val camera = mCamera.get(cameraId).camera

        batch.spr.setProjectionMatrix(camera.combined)
        batch.spr.begin()
        renderBackgroundTiles(mapState)
        renderMapObjects()
        batch.spr.end()
    }

    private fun renderBackgroundTiles(mapState: BattleMap) {

        val tileSize = advConfig.resolution.tileSize.toFloat()

        for (x in 0 until mapState.width) {
            for (y in 0 until mapState.height) {
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

        val pccEntityBag = world.aspectSubscriptionManager.get(allOf(
                PccComponent::class, XYComponent::class, AnimationFramesComponent::class)).entities
        val pccEntities = pccEntityBag.data.slice(0 until pccEntityBag.size())
        for (pccEntity in pccEntities) {
            renderPcc(pccEntity, tileSize)
        }
    }

    private fun renderPcc(id: Int, tileSize: Float) {
        val cPcc = mPccs.get(id)
        val cXy = mXy.get(id)
        val cAnimationFrames = mAnimationFrames.get(id)
        for (pcc in cPcc.pccs) {
            val pccTexture = pccManager.getPccFrame(pcc, PccFrame(cPcc.direction, cAnimationFrames.frame))
            val scaleFactor = tileSize / pccTexture.regionWidth
            batch.spr.draw(pccTexture, cXy.x, cXy.y, tileSize, pccTexture.regionHeight * scaleFactor)
        }
    }
}
