package com.badlogic.gdx.scenes.scene2d.utils

import com.badlogic.gdx.graphics.g2d.Batch

class MultiDrawable(val drawables : Array<Drawable>) : BaseDrawable() {

    override fun draw(batch: Batch?, x: Float, y: Float, width: Float, height: Float) {
        drawables.forEach { it.draw(batch, x, y, width, height) }
    }

}
