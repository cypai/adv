package com.pipai.adv

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.pipai.adv.artemis.screens.BattleMapScreen
import com.pipai.adv.gui.BatchHelper
import com.pipai.utils.getLogger
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle

class AdvGame(val advConfig: AdvConfig) : Game() {

    private val logger = getLogger()

    lateinit var globals: AdvGameGlobals
        private set

    lateinit var spriteBatch: SpriteBatch
        private set

    lateinit var shapeRenderer: ShapeRenderer
        private set

    lateinit var font: BitmapFont
        private set

    lateinit var batchHelper: BatchHelper
        private set

    override fun create() {
        logger.info("Starting AdvGame with the following config settings:")
        logger.info(advConfig.resolution.toDebugString())

        spriteBatch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        font = BitmapFont()
        batchHelper = BatchHelper(spriteBatch, shapeRenderer, font)
        ToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE)

        logger.info("Loading schematics...")
        globals = AdvGameInitializer().initializeGlobals()
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
