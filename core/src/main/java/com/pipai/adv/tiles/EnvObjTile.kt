package com.pipai.adv.tiles

import com.badlogic.gdx.graphics.g2d.TextureRegion

interface EnvObjTile<in T> {
    val tilesetType: EnvObjTilesetType

    fun tile(params: T): List<TextureRegion>
}

enum class EnvObjTilesetType {
    NONE, MAP, ATLAS, PCC
}

class MapTile(private val tileset: MapTileset, private val tilePosition: TilePosition) : EnvObjTile<Unit> {
    override val tilesetType: EnvObjTilesetType = EnvObjTilesetType.MAP

    override fun tile(params: Unit): List<TextureRegion> {
        return listOf(tileset.tile(tilePosition))
    }
}

class TilesetTile(private val tileset: FileTileset, private val tilePosition: TilePosition) : EnvObjTile<Unit> {
    override val tilesetType: EnvObjTilesetType = EnvObjTilesetType.ATLAS

    override fun tile(params: Unit): List<TextureRegion> {
        return listOf(tileset.tile(tilePosition))
    }
}

class PccTile(private val pccManager: PccManager, private var metadata: List<PccMetadata>) : EnvObjTile<PccFrame> {

    init {
        pccManager.loadPccTextures(metadata)
    }

    override val tilesetType: EnvObjTilesetType = EnvObjTilesetType.PCC

    override fun tile(params: PccFrame): List<TextureRegion> {
        return metadata.map { pccManager.getPccFrame(it, params) }
    }
}
