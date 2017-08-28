package com.pipai.adv

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.pipai.adv.artemis.screens.MainMenuScreen
import com.pipai.adv.gui.BatchHelper
import com.pipai.utils.getLogger
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle

class AdvGame(val advConfig: AdvConfig) : Game() {

    private val logger = getLogger()

    private val FONT_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.;,{}\"´`'<>";

    lateinit var globals: AdvGameGlobals
        private set

    lateinit var spriteBatch: SpriteBatch
        private set

    lateinit var shapeRenderer: ShapeRenderer
        private set

    lateinit var font: BitmapFont
        private set

    lateinit var smallFont: BitmapFont
        private set

    lateinit var batchHelper: BatchHelper
        private set

    lateinit var skin: Skin
        private set

    override fun create() {
        logger.info("Starting AdvGame with the following config settings:")
        logger.info(advConfig.resolution.toDebugString())

        spriteBatch = SpriteBatch()
        shapeRenderer = ShapeRenderer()

        val fontGenerator = FreeTypeFontGenerator(Gdx.files.internal("assets/binassets/graphics/fonts/SourceSansPro-Regular.ttf"))
        val fontParameter = FreeTypeFontParameter()
        fontParameter.size = 32
        font = fontGenerator.generateFont(fontParameter)

        fontParameter.size = 16
        smallFont = fontGenerator.generateFont(fontParameter)
        fontGenerator.dispose()

        batchHelper = BatchHelper(spriteBatch, shapeRenderer, font, smallFont)
        ToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE)

        initSkin()

        logger.info("Loading schematics...")
        globals = AdvGameInitializer().initializeGlobals()
        setScreen(MainMenuScreen(this))
    }

    private fun initSkin() {
        skin = Skin()
        val bgTexture = Texture(Gdx.files.internal("assets/binassets/graphics/textures/paper.jpg"))
        bgTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
        skin.add("bg", bgTexture)

        val frameTexture = Texture(Gdx.files.internal("assets/binassets/graphics/textures/frame.png"))
        val framePatch = NinePatch(frameTexture, 5, 5, 5, 5)
        skin.add("frameTexture", frameTexture)
        skin.add("frame", framePatch)

        skin.add("defaultLabelStyle", LabelStyle(font, Color.BLACK))
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
