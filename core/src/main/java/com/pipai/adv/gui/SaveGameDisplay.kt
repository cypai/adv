package com.pipai.adv.gui

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.pipai.adv.AdvGame
import com.pipai.adv.save.AdvSaveSlot

class SaveGameDisplay(private val game: AdvGame) : ScrollPane(Table(), game.skin) {

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
        saveList.addConfirmCallback {
            if (it.name == "Empty Slot") {
                writeSave(it.slot)
            } else {
                openOverwriteConfirmationDialog(it)
            }
        }
        table.add(Label("Save Game", game.skin))
        table.row()
        table.add(saveList)
                .minWidth(480f)
                .minHeight(480f)
        table.width = table.prefWidth
        table.height = table.prefHeight
    }

    fun show(stage: Stage) {
        val saves = game.globals.saveManager.getAllSaves()
        val slots: MutableList<AdvSaveSlot> = mutableListOf()
        slots.addAll(saves)
        slots.add(AdvSaveSlot("Empty Slot", (saves.lastOrNull()?.slot ?: 0) + 1))
        saveList.setItems(slots)
        x = (game.advConfig.resolution.width - table.width) / 2f
        y = (game.advConfig.resolution.height - table.height) / 2f
        setSize(table.width, table.height)
        stage.addActor(this)
    }

    private fun openOverwriteConfirmationDialog(slot: AdvSaveSlot) {
        val dialog = object : Dialog("", game.skin) {
            override fun result(item: Any?) {
                when (item) {
                    true -> {
                        writeSave(slot.slot)
                    }
                }
            }
        }
        dialog.text("Overwrite this save?")

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

    private fun writeSave(slot: Int) {
        game.globals.writeSave(slot)
        remove()
    }

}
