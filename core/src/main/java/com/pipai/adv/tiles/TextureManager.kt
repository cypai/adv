package com.pipai.adv.tiles

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureRegion

class TextureManager {

    private val tilesets: MutableMap<String, FileTileset> = mutableMapOf()

    fun loadAllTextures() {
        tilesets.put("signs", FileTileset(Gdx.files.internal("assets/binassets/graphics/tilesets/signs.png"), 32, 32))
        tilesets.put("trees", FileTileset(Gdx.files.internal("assets/binassets/graphics/tilesets/tree.png"), 48, 96))
        tilesets.put("chest", FileTileset(Gdx.files.internal("assets/binassets/graphics/tilesets/chest.png"), 32, 32))
    }

    fun getTile(descriptor: TileDescriptor): TextureRegion {
        return tilesets[descriptor.tileset]!!.tile(descriptor.tilePosition)
    }

    fun dispose() {
        for (kv in tilesets) {
            kv.value.dispose()
        }
    }
}
