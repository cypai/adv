package com.pipai.adv.artemis.system.ui

import com.artemis.BaseSystem
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.pipai.adv.AdvGame
import com.pipai.adv.gui.UiConstants
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.EventSystem

class MainTextboxUiSystem(private val game: AdvGame) : BaseSystem() {

    private val sEvent by system<EventSystem>()

    private val batch = game.batchHelper
    private val config = game.advConfig

    private val stage = Stage(ScreenViewport())
    private val table = Table()
    private lateinit var bg: Image
    private lateinit var frame: Image
    private lateinit var textLabel: Label

    private var text: String = ""
    private var fullText: String = ""

    init {
        createMainTextbox()
    }

    private fun createMainTextbox() {
        val skin = game.skin

        val width = game.advConfig.resolution.width.toFloat()
        val height = game.advConfig.resolution.height.toFloat()

        val x = (width / 10).toInt()
        val y = (height / 10).toInt()

        val textboxWidth = width * 0.8f
        val textboxHeight = height / 5f

        val bgRegion = skin.getRegion("bg")
        bgRegion.setRegion(0, 0, textboxWidth.toInt(), textboxHeight.toInt())
        bg = Image(bgRegion)
        bg.width = textboxWidth
        bg.height = textboxHeight
        bg.x = x.toFloat()
        bg.y = y.toFloat()

        val framePatch = skin.getPatch("frame")
        framePatch.topHeight = 2f
        frame = Image(framePatch)
        frame.width = textboxWidth + UiConstants.FRAME_LEFT_PADDING + UiConstants.FRAME_RIGHT_PADDING
        frame.height = textboxHeight + UiConstants.FRAME_TOP_PADDING + UiConstants.FRAME_BOTTOM_PADDING
        frame.x = (x - UiConstants.FRAME_LEFT_PADDING).toFloat()
        frame.y = (y - UiConstants.FRAME_BOTTOM_PADDING).toFloat()

        val tablePadding = textboxHeight / 8

        table.x = x.toFloat() + tablePadding
        table.y = y.toFloat() + tablePadding
        table.width = textboxWidth - 2 * tablePadding
        table.height = textboxHeight - 2 * tablePadding

        textLabel = Label("", skin)
        textLabel.setWrap(true)
        table.add(textLabel).expand().top().fillX()

        stage.addActor(bg)
        stage.addActor(frame)
        stage.addActor(table)
    }

    fun setToText(text: String) {
        this.text = ""
        fullText = text
    }

    fun setToText(speaker: String, text: String) {
        if (speaker.isBlank()) {
            setToText(text)
        } else {
            this.text = "$speaker: "
            fullText = "$speaker: $text"
        }
    }

    fun showFullText() {
        text = fullText
    }

    fun isDone(): Boolean {
        return text.length == fullText.length
    }

    override fun processSystem() {
        if (fullText.length == 0) {
            isEnabled = false
            return
        }
        if (text.length < fullText.length) {
            val substringIndex = Math.min(text.length + 1, fullText.length)
            text = fullText.substring(0, substringIndex)
        }
        textLabel.setText(text)
        stage.act()
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
    }
}
