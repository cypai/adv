package com.badlogic.gdx.scenes.scene2d.utils

import com.badlogic.gdx.graphics.g2d.Batch

class OffsetDrawable(val drawable: Drawable,
                     val xOffset: Float, val yOffset: Float,
                     val widthOffset: Float, val heightOffset: Float) : BaseDrawable() {

    override fun draw(batch: Batch?, x: Float, y: Float, width: Float, height: Float) {
        drawable.draw(batch, x + xOffset, y + yOffset, width + widthOffset, height + heightOffset)
    }

}
