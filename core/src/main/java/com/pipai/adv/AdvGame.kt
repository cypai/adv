package com.pipai.adv

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.pipai.adv.artemis.screens.BattleMapScreen
import com.pipai.adv.gui.BatchHelper
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle

class AdvGame : Game() {
    lateinit var spriteBatch: SpriteBatch
    lateinit var shapeRenderer: ShapeRenderer
    lateinit var font: BitmapFont
    lateinit var batchHelper: BatchHelper

    override fun create() {
        spriteBatch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        font = BitmapFont()
        batchHelper = BatchHelper(spriteBatch, shapeRenderer, font)
        ToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE)
        setScreen(BattleMapScreen(this))
    }

    override fun render() {
        super.render()
    }

    override fun dispose() {
        spriteBatch.dispose()
        shapeRenderer.dispose()
        font.dispose()
    }
}
