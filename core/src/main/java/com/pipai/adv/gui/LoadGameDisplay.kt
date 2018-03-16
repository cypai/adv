package com.pipai.adv.gui

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.screens.GuildScreen
import com.pipai.adv.save.AdvSaveSlot

class LoadGameDisplay(private val game: AdvGame) : ScrollPane(Table(), game.skin) {

    private val table = this.actor as Table
    private val saveList = ImageList(game.skin, "smallMenuList", object : ImageList.ImageListItemView<AdvSaveSlot> {
        override fun getItemImage(item: AdvSaveSlot): TextureRegion? = null
        override fun getItemText(item: AdvSaveSlot): String = item.name
        override fun getItemRightText(item: AdvSaveSlot): String = if (item.slot == 0) "Autosave" else item.slot.toString()
        override fun getSpacing(): Float = 10f
        override fun getMinCenterSpacing(): Float = 64f
        override fun getRightSpacing(): Float = 14f
    })

    init {
        table.background = game.skin.getDrawable("frameDrawable")
        saveList.hoverSelect = true
        saveList.keySelection = true
        saveList.addConfirmCallback {
            openConfirmationDialog(it)
        }
        table.add(Label("Load Game", game.skin))
        table.row()
        table.add(saveList)
                .minWidth(480f)
                .minHeight(480f)
        table.width = table.prefWidth
        table.height = table.prefHeight
    }

    fun show(stage: Stage) {
        val saves = game.globals.saveManager.getAllSaves()
        saveList.setItems(saves)
        x = (game.advConfig.resolution.width - table.width) / 2f
        y = (game.advConfig.resolution.height - table.height) / 2f
        setSize(table.width, table.height)
        stage.addActor(this)
        stage.keyboardFocus = saveList
    }

    private fun openConfirmationDialog(slot: AdvSaveSlot) {
        val dialog = object : Dialog("", game.skin) {
            override fun result(item: Any?) {
                when (item) {
                    true -> {
                        loadSave(slot.slot)
                    }
                }
            }
        }
        dialog.text("Load this save?")

        dialog.button("  Yes  ", true)
        dialog.button("  No  ", false)
        dialog.key(Input.Keys.ENTER, true)
        dialog.key(Input.Keys.Z, true)
        dialog.key(Input.Keys.ESCAPE, false)
        dialog.key(Input.Keys.X, false)
        dialog.contentTable.pad(16f)
        dialog.buttonTable.pad(16f)
        dialog.show(stage)
    }

    private fun loadSave(slot: Int) {
        game.globals.loadSave(slot)
        val currentScreen = game.screen
        game.screen = GuildScreen(game)
        currentScreen.dispose()
    }

}
