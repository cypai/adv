package com.pipai.adv.utils

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer

fun ShapeRenderer.drawHealthbar(x: Float, y: Float, width: Float, height: Float,
                                borderColor: Color, leftColor: Color, rightColor: Color, bgColor: Color,
                                percentage: Float) {

    val innerWidth = width - 2
    this.rect(x, y, width, height, borderColor, borderColor, borderColor, borderColor)
    this.rect(x + 1, y + 1, innerWidth, height - 2, leftColor, rightColor, rightColor, leftColor)
    this.rect(x + 1 + innerWidth, y + 1, -(innerWidth - innerWidth * percentage), height - 2, bgColor, bgColor, bgColor, bgColor)
}

fun SpriteBatch.drawRect(pixel: TextureRegion, x: Float, y: Float, width: Float, height: Float, thickness: Float) {
    this.draw(pixel, x, y, width, thickness)
    this.draw(pixel, x, y, thickness, height)
    this.draw(pixel, x, y + height - thickness, width, thickness)
    this.draw(pixel, x + width - thickness, y, thickness, height)
}

fun SpriteBatch.drawLine(pixel: TextureRegion, x1: Float, y1: Float, x2: Float, y2: Float, thickness: Float) {
    val dx = x2 - x1
    val dy = y2 - y1
    val dist = Math.sqrt((dx * dx + dy * dy).toDouble())
    val rad = Math.atan2(dy.toDouble(), dx.toDouble())
    this.draw(pixel, x1, y1, 0f, 0f, dist.toFloat(), thickness, 1f, 1f, rad.toFloat())
}
