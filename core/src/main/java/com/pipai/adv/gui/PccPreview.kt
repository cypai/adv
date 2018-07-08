package com.pipai.adv.gui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.tiles.PccManager
import com.pipai.adv.tiles.PccMetadata
import com.pipai.adv.tiles.UnitAnimationFrame

class PccPreview(pcc: List<PccMetadata>,
                 var direction: Direction,
                 private val pccManager: PccManager,
                 skin: Skin) : Image() {

    private val pccParts: MutableList<PccMetadata> = pcc.toMutableList()

    private val bg = skin.newDrawable("white", Color.DARK_GRAY)
    var frame = 0

    init {
        width = PccManager.PCC_WIDTH + 2f
        height = PccManager.PCC_HEIGHT + 2f
    }

    override fun getPrefHeight(): Float {
        return PccManager.PCC_HEIGHT + 2f
    }

    override fun getPrefWidth(): Float {
        return PccManager.PCC_WIDTH + 2f
    }

    fun getPcc() = pccParts.toList()

    fun setPcc(pcc: List<PccMetadata>) {
        pccParts.clear()
        pccParts.addAll(pcc)
    }

    fun incrementFrame() {
        frame++
        if (frame >= 4) {
            frame = 0
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.flush()
        bg.draw(batch, x, y, width, height)
        for (pcc in pccParts) {
            val pccTexture = pccManager.getPccFrame(pcc, UnitAnimationFrame(direction, frame))

            if (pcc.color1 != null) {
                batch.shader.setAttributef("a_color_inter1", pcc.color1.r, pcc.color1.g, pcc.color1.b, pcc.color1.a)
            }
            if (pcc.color2 != null) {
                batch.shader.setAttributef("a_color_inter2", pcc.color2.r, pcc.color2.g, pcc.color2.b, pcc.color2.a)
            }

            batch.draw(pccTexture, x + 1, y + 1, PccManager.PCC_WIDTH.toFloat(), PccManager.PCC_HEIGHT.toFloat())
            batch.flush()
            batch.shader.setAttributef("a_color_inter1", 0f, 0f, 0f, 0f)
            batch.shader.setAttributef("a_color_inter2", 0f, 0f, 0f, 0f)
        }
    }
}
