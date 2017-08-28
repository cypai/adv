package com.pipai.adv.artemis.system.ui

import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.components.MainTextboxFlagComponent
import com.pipai.adv.artemis.components.MultipleTextComponent
import com.pipai.adv.artemis.components.PartialTextComponent
import com.pipai.adv.artemis.system.input.InteractionInputSystem
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.getSystemSafe
import com.pipai.adv.utils.require
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.EventSystem
import com.pipai.adv.artemis.system.input.CharacterMovementInputSystem

class MainTextboxUiSystem(private val game: AdvGame) : IteratingSystem(allOf()), InputProcessor {

    private val mPartialText by require<PartialTextComponent>()
    private val mMultipleText by require<MultipleTextComponent>()
    private val mMainTextboxFlag by require<MainTextboxFlagComponent>()

    private val sEvent by system<EventSystem>()

    private val batch = game.batchHelper
    private val config = game.advConfig

    private val stage = Stage(ScreenViewport())
    private val table = Table()
    private lateinit var bg: Image
    private lateinit var frame: Image
    private lateinit var textLabel: Label

    private var isShowing = false
    private var speedup = false
    private var nextText = false

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
        frame.width = textboxWidth + 4
        frame.height = textboxHeight + 4
        frame.x = (x - 1).toFloat()
        frame.y = (y - 3).toFloat()

        val tablePadding = textboxHeight / 8

        table.x = x.toFloat() + tablePadding
        table.y = y.toFloat() + tablePadding
        table.width = textboxWidth - 2 * tablePadding
        table.height = textboxHeight - 2 * tablePadding

        textLabel = Label("", skin.get("defaultLabelStyle", LabelStyle::class.java))
        textLabel.setWrap(true)
        table.add(textLabel).expand().top().fillX()

        stage.addActor(bg)
        stage.addActor(frame)
        stage.addActor(table)
    }

    override fun process(entityId: Int) {
        if (!isShowing) {
            isShowing = true
            disableSystems()
        }

        val cPartialText = mPartialText.get(entityId)

        if (nextText) {
            if (cPartialText.currentText.length < cPartialText.fullText.length) {
                cPartialText.currentText = cPartialText.fullText
            } else {
                val cMultipleText = mMultipleText.get(entityId)
                val textList = cMultipleText.textList
                if (textList.size > 0) {
                    cPartialText.setToText(textList.first())
                    textList.removeAt(0)
                } else {
                    world.delete(entityId)
                    isShowing = false
                    enableSystems()
                }
            }
            nextText = false
        }

        textLabel.setText(cPartialText.currentText)
        stage.act()
        stage.draw()
    }

    private fun disableSystems() {
        world.getSystemSafe(InteractionInputSystem::class.java)?.isEnabled = false
        world.getSystemSafe(CharacterMovementInputSystem::class.java)?.isEnabled = false
    }

    private fun enableSystems() {
        world.getSystemSafe(InteractionInputSystem::class.java)?.isEnabled = true
        world.getSystemSafe(CharacterMovementInputSystem::class.java)?.isEnabled = true
    }

    override fun dispose() {
        stage.dispose()
    }

    override fun keyDown(keycode: Int): Boolean {
        if (isShowing) {
            when (keycode) {
                Keys.Z -> {
                    nextText = true
                }
                Keys.X -> {
                    speedup = true
                }
            }
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        when (keycode) {
            Keys.X -> {
                speedup = false
            }
        }
        return false
    }

    override fun keyTyped(character: Char) = false

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) = false

    override fun mouseMoved(screenX: Int, screenY: Int) = false

    override fun scrolled(amount: Int): Boolean = false
}
