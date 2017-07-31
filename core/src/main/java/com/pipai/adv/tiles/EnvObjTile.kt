package com.pipai.adv.tiles

import com.badlogic.gdx.graphics.g2d.TextureRegion

interface EnvObjTile<in T> {
    fun tile(params: T): List<TextureRegion>
}


class MapTile(private val tileset: MapTileset, private val tilePosition: TilePosition) : EnvObjTile<Unit> {
    override fun tile(params: Unit): List<TextureRegion> {
        return listOf(tileset.tile(tilePosition))
    }
}

class TilesetTile(private val tileset: FileTileset, private val tilePosition: TilePosition) : EnvObjTile<Unit> {
    override fun tile(params: Unit): List<TextureRegion> {
        return listOf(tileset.tile(tilePosition))
    }
}

class PccTile(private val pccManager: PccManager, private var metadata: List<PccMetadata>) : EnvObjTile<PccFrame> {

    init {
        pccManager.loadPccTextures(metadata)
    }

    override fun tile(params: PccFrame): List<TextureRegion> {
        return metadata.map { pccManager.getPccFrame(it, params) }
    }
}
