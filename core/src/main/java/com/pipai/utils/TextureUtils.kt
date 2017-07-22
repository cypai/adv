package com.pipai.utils

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion

fun Texture.split(width: Int, height: Int,
                  xSpace: Int, ySpace: Int): Array<Array<TextureRegion>> {

    val tiles = TextureRegion.split(this, width + xSpace, height + ySpace)

    for (row in tiles) {
        for (tile in row) {
            tile.regionX = tile.regionX + xSpace
            tile.regionY = tile.regionY + ySpace
            tile.regionWidth = width
            tile.regionHeight = height
        }
    }

    return tiles
}
