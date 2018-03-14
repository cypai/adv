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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.MultiDrawable
import com.badlogic.gdx.scenes.scene2d.utils.OffsetDrawable
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

        logger.info("Loading schematics...")
        globals = AdvGameInitializer().initializeGlobals()

        spriteBatch = SpriteBatch(1000, globals.shaderProgram)
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
        val frameDrawable = MultiDrawable(arrayOf(
                OffsetDrawable(skin.getTiledDrawable("bg"), 1f, 3f, -4f, -4f),
                skin.getDrawable("frame")))
        skin.add("frameDrawable", frameDrawable, Drawable::class.java)

        val flatFrameTexture = Texture(Gdx.files.local("assets/binassets/graphics/textures/flatframe.png"))
        val flatFramePatch = NinePatch(flatFrameTexture, 5, 5, 5, 5)
        skin.add("flatFrameTexture", flatFrameTexture)
        skin.add("flatFrame", flatFramePatch)
        val flatFrameDrawable = MultiDrawable(arrayOf(
                OffsetDrawable(skin.getTiledDrawable("bg"), 1f, 3f, -4f, -4f),
                skin.getDrawable("flatFrame")))
        skin.add("flatFrameDrawable", flatFrameDrawable, Drawable::class.java)

        val transparencyBgTexture = Texture(Gdx.files.local("assets/binassets/graphics/textures/transparencyBg.png"))
        skin.add("transparencyBg", transparencyBgTexture)

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

        val textButtonStyle = TextButton.TextButtonStyle(frameDrawable, flatFrameDrawable, frameDrawable, smallFont)
        textButtonStyle.fontColor = Color.BLACK
        skin.add("default", textButtonStyle)

        val clearGrayDrawable = skin.newDrawable("white", Color(0.5f, 0.5f, 0.5f, 0.5f))
        val clearDarkGrayDrawable = skin.newDrawable("white", Color(0.3f, 0.3f, 0.3f, 0.5f))

        val listStyle = List.ListStyle(smallFont, Color.BLACK, Color.BLACK, grayDrawable)
        listStyle.background = whiteDrawable
        skin.add("default", listStyle)
        val scrollPaneStyle = ScrollPane.ScrollPaneStyle()
        scrollPaneStyle.vScroll = OffsetDrawable(clearGrayDrawable, -8f, 0f, 6f, 0f)
        scrollPaneStyle.vScrollKnob = OffsetDrawable(clearDarkGrayDrawable, -8f, 0f, 6f, 0f)
        skin.add("default", scrollPaneStyle)
        val splitPaneStyle = SplitPane.SplitPaneStyle(grayDrawable)
        skin.add("default-horizontal", splitPaneStyle)
        val selectBoxStyle = SelectBox.SelectBoxStyle(smallFont, Color.BLACK, whiteDrawable, scrollPaneStyle, listStyle)
        skin.add("default", selectBoxStyle)

        val menuListStyle = List.ListStyle(font, Color.BLACK, Color.BLACK,
                OffsetDrawable(clearGrayDrawable, 4f, 4f, -8f, -8f))
        menuListStyle.background = frameDrawable
        skin.add("menuList", menuListStyle)
        val smallMenuListStyle = List.ListStyle(smallFont, Color.BLACK, Color.BLACK,
                OffsetDrawable(skin.newDrawable("white", Color(0.5f, 0.5f, 0.5f, 0.5f)), 4f, 1f, -16f, -2f))
        skin.add("smallMenuList", smallMenuListStyle)

        val windowStyle = Window.WindowStyle(smallFont, Color.BLACK, frameDrawable)
        skin.add("default", windowStyle)
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
