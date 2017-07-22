package com.pipai.adv.artemis.system.rendering

import com.artemis.BaseSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.pipai.adv.gui.BatchHelper

class FpsRenderingSystem(private val batch: BatchHelper) : BaseSystem() {

    override fun processSystem() {
        val spr = batch.spr
        val font = batch.font
        spr.begin()
        font.color = Color.WHITE
        font.draw(spr, Gdx.graphics.framesPerSecond.toString(),
                Gdx.graphics.width.toFloat() - 24,
                Gdx.graphics.height.toFloat() - font.lineHeight / 2)
        spr.end()
    }

}
