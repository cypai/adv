package com.pipai.adv.utils

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

fun ShapeRenderer.drawHealthbar(x: Float, y: Float, width: Float, height: Float,
                                borderColor: Color, leftColor: Color, rightColor: Color, bgColor: Color,
                                percentage: Float) {

    val innerWidth = width - 2
    this.rect(x, y, width, height, borderColor, borderColor, borderColor, borderColor)
    this.rect(x + 1, y + 1, innerWidth, height - 2, leftColor, rightColor, rightColor, leftColor)
    this.rect(x + 1 + innerWidth, y + 1, -(innerWidth - innerWidth * percentage), height - 2, bgColor, bgColor, bgColor, bgColor)
}
