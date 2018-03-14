package com.pipai.adv.artemis.system.ui

import com.artemis.BaseSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.ImageList
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.events.PauseEvent
import com.pipai.adv.artemis.system.ui.menu.StringMenuItem
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.EventSystem

class PauseUiSystem(private val game: AdvGame) : BaseSystem(), InputProcessor {

    private val sEvent by system<EventSystem>()

    val stage = Stage(ScreenViewport(), game.spriteBatch)

    init {
        createMainForm()
    }

    override fun initialize() {
        isEnabled = false
    }

    private fun createMainForm() {
        val skin = game.skin

        val mainMenuWidth = game.advConfig.resolution.width / 3f
        val mainMenuHeight = game.advConfig.resolution.height / 2f

        val table = Table()
        table.x = (game.advConfig.resolution.width - mainMenuWidth) / 2
        table.y = (game.advConfig.resolution.height - mainMenuHeight) / 2
        table.width = mainMenuWidth
        table.height = mainMenuHeight
        table.background = skin.getDrawable("frameDrawable")

        table.top().pad(10f)
        table.add(Label("Pause", skin))
        table.row()

        val mainMenuList = ImageList(game.skin, "smallMenuList", object : ImageList.ImageListItemView<StringMenuItem> {
            override fun getItemImage(item: StringMenuItem): TextureRegion? = null
            override fun getItemText(item: StringMenuItem): String = item.text
            override fun getItemRightText(item: StringMenuItem): String = item.rightText
            override fun getSpacing(): Float = 10f
        })
        mainMenuList.setItems(listOf(
                StringMenuItem("Save Game", null, ""),
                StringMenuItem("Load Game", null, ""),
                StringMenuItem("Options", null, ""),
                StringMenuItem("Quit Game", null, "")))
        mainMenuList.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                handleMainMenuConfirm(mainMenuList.getSelected())
            }
        })
        table.add(mainMenuList)
                .width(mainMenuWidth - 20f)
                .left()
        table.validate()

        stage.addActor(table)
    }

    private fun handleMainMenuConfirm(menuItem: StringMenuItem) {
        when (menuItem.text) {
            "Quit Game" -> {
                showDialog("Are you sure you want to quit?", { Gdx.app.exit() }, {})
            }
        }
    }

    private fun showDialog(text: String, yesCallback: () -> Unit, noCallback: () -> Unit) {
        val dialog = object : Dialog("", game.skin) {
            override fun result(item: Any?) {
                when (item) {
                    true -> yesCallback.invoke()
                    false -> noCallback.invoke()
                }
            }
        }
        dialog.text(text)

        dialog.button("  Yes  ", true)
        dialog.button("  No  ", false)
        dialog.key(Keys.ENTER, true)
        dialog.key(Keys.Z, true)
        dialog.key(Keys.ESCAPE, false)
        dialog.key(Keys.X, false)
        dialog.contentTable.pad(16f)
        dialog.buttonTable.pad(16f)
        dialog.show(stage)
    }

    override fun processSystem() {
        if (isEnabled) {
            stage.act()
            stage.draw()
        }
    }

    override fun dispose() {
        stage.dispose()
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Keys.ESCAPE -> {
                isEnabled = !isEnabled
                sEvent.dispatch(PauseEvent(isEnabled))
                return true
            }
            else -> stage.keyDown(keycode)
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean = false

    override fun keyTyped(character: Char) = false

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        stage.keyboardFocus = null
        stage.touchDown(screenX, screenY, pointer, button)
        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) = false

    override fun mouseMoved(screenX: Int, screenY: Int) = false

    override fun scrolled(amount: Int): Boolean = false

}
