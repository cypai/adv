package com.pipai.adv.artemis.system.rendering

import com.artemis.managers.TagManager
import com.artemis.systems.IteratingSystem
import com.pipai.adv.artemis.components.BattleBackendComponent
import com.pipai.adv.artemis.components.OrthographicCameraComponent
import com.pipai.adv.artemis.screens.BattleMapScreenTags
import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.gui.BatchHelper
import com.pipai.adv.tiles.MapTilesetInfo
import com.pipai.adv.tiles.TilePosition
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.require
import com.pipai.adv.utils.system

class BattleMapRenderingSystem(private val batch: BatchHelper, private val mapTileset: MapTilesetInfo) : IteratingSystem(allOf()) {

    private val mBackend by require<BattleBackendComponent>()

    private val mCamera by mapper<OrthographicCameraComponent>()

    private val sTags by system<TagManager>()

    override protected fun process(entityId: Int) {
        val cBackend = mBackend.get(entityId)
        val mapState = cBackend.backend.getBattleMapState()

        renderBackgroundTiles(mapState)
        renderMapObjects()
    }

    private fun renderBackgroundTiles(mapState: BattleMap) {
        val cameraId = sTags.getEntityId(BattleMapScreenTags.CAMERA.toString())
        val cCamera = mCamera.get(cameraId)

        batch.spr.begin()
        for (x in 0 until mapState.width) {
            for (y in 0 until mapState.height) {
                val cell = mapState.cells[x][y]
                for (bgTileInfo in cell.backgroundTiles) {
                    val tile = mapTileset.tiles(bgTileInfo.tileType)[bgTileInfo.index]
                    batch.spr.draw(tile, x.toFloat() * 32f, y.toFloat() * 32f)
                }
            }
        }
        batch.spr.end()
    }

    private fun renderMapObjects() {

    }
}
