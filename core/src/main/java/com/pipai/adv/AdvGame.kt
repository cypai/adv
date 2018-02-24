package com.pipai.adv

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Pixmap.Format
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.List
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle
import com.kotcrab.vis.ui.VisUI
import com.pipai.adv.artemis.screens.MainMenuScreen
import com.pipai.adv.gui.BatchHelper
import com.pipai.adv.utils.getLogger
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
        fontParameter.size = 28
        font = fontGenerator.generateFont(fontParameter)

        fontParameter.size = 20
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
        VisUI.load()

        skin = Skin()
        val bgTexture = Texture(Gdx.files.local("assets/binassets/graphics/textures/paper.jpg"))
        bgTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
        skin.add("bg", bgTexture)

        val mainMenuBgTexture = Texture(Gdx.files.local("assets/binassets/graphics/textures/mainmenu.jpg"))
        mainMenuBgTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
        skin.add("mainMenuBg", mainMenuBgTexture)

        val frameTexture = Texture(Gdx.files.local("assets/binassets/graphics/textures/frame.png"))
        val framePatch = NinePatch(frameTexture, 5, 5, 5, 5)
        skin.add("frameTexture", frameTexture)
        skin.add("frame", framePatch)

        val pixmap = Pixmap(1, 1, Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        skin.add("white", Texture(pixmap));

        skin.add("default", LabelStyle(font, Color.BLACK))
        skin.add("small", LabelStyle(smallFont, Color.BLACK))

        val whiteDrawable = skin.newDrawable("white", Color.WHITE)
        val grayDrawable = skin.newDrawable("white", Color.LIGHT_GRAY)
        val blackDrawable = skin.newDrawable("white", Color.BLACK)
        val textFieldStyle = TextFieldStyle(font, Color.BLACK,
                blackDrawable, grayDrawable, whiteDrawable)
        skin.add("default", textFieldStyle)

        val smallTextFieldStyle = TextFieldStyle(smallFont, Color.BLACK,
                blackDrawable, grayDrawable, whiteDrawable)
        skin.add("small", smallTextFieldStyle)

        val clearLightDrawable = skin.newDrawable("white", Color(0.5f, 0.5f, 0.5f, 0.2f))
        val clearGrayDrawable = skin.newDrawable("white", Color(0.5f, 0.5f, 0.5f, 0.4f))
        val textButtonStyle = TextButton.TextButtonStyle(clearLightDrawable, clearGrayDrawable, clearLightDrawable, smallFont)
        textButtonStyle.fontColor = Color.BLACK
        skin.add("default", textButtonStyle)

        val listStyle = List.ListStyle(smallFont, Color.BLACK, Color.BLACK, grayDrawable)
        listStyle.background = whiteDrawable
        skin.add("default", listStyle)
        val scrollPaneStyle = ScrollPane.ScrollPaneStyle(whiteDrawable, grayDrawable, blackDrawable, grayDrawable, blackDrawable)
        skin.add("default", scrollPaneStyle)
        val selectBoxStyle = SelectBox.SelectBoxStyle(smallFont, Color.BLACK, whiteDrawable, scrollPaneStyle, listStyle)
        skin.add("default", selectBoxStyle)
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
