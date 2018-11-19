package com.pipai.adv.artemis.system.ui

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageList
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.events.PauseEvent
import com.pipai.adv.artemis.screens.GuildScreen
import com.pipai.adv.artemis.screens.MarketScreen
import com.pipai.adv.artemis.screens.OrphanageScreen
import com.pipai.adv.artemis.screens.WorldMapScreen
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.artemis.system.ui.menu.StringMenuItem
import com.pipai.adv.gui.StandardImageListItemView
import net.mostlyoriginal.api.event.common.Subscribe

class VillageUiSystem(private val game: AdvGame,
                      private val stage: Stage) : NoProcessingSystem() {

    private val skin = game.skin

    private val mainTable = Table()
    private val mainMenuList = ImageList(game.skin, "smallMenuList", StandardImageListItemView<StringMenuItem>())

    init {
        createTables()
    }

    @Subscribe
    fun handlePause(event: PauseEvent) {
        if (!event.isPaused) {
            stage.keyboardFocus = mainMenuList
        }
    }

    override fun initialize() {
        isEnabled = false
    }

    private fun createTables() {
        val menuItems = mutableListOf(
                StringMenuItem(game.globals.save!!.playerGuild, null, ""),
                StringMenuItem("Hospital", null, ""),
                StringMenuItem("Market", null, ""),
                StringMenuItem("Pub", null, ""),
                StringMenuItem("Orphanage", null, ""),
                StringMenuItem("Guild Hall", null, ""),
                StringMenuItem("Town Hall", null, ""),
                StringMenuItem("Gate", null, ""))

        mainMenuList.setItems(menuItems)
        mainMenuList.hoverSelect = true
        mainMenuList.keySelection = true
        mainMenuList.addConfirmCallback { handleMainMenuConfirm(it) }
        mainTable.add(mainMenuList)
                .pad(16f)
                .left()
                .top()

        mainTable.x = 64f
        mainTable.y = 64f
        mainTable.width = mainTable.prefWidth
        mainTable.height = mainTable.prefHeight
        mainTable.background = skin.getDrawable("frameDrawable")

        stage.addActor(mainTable)

        mainMenuList.setSelectedIndex(0)
        stage.keyboardFocus = mainMenuList
    }

    private fun handleMainMenuConfirm(menuItem: StringMenuItem) {
        when (menuItem.text) {
            game.globals.save!!.playerGuild -> {
                game.screen = GuildScreen(game)
            }
            "Hospital" -> {
                // go
            }
            "Market" -> {
                game.screen = MarketScreen(game)
            }
            "Pub" -> {
                // go
            }
            "Orphanage" -> {
                game.screen = OrphanageScreen(game)
            }
            "Guild Hall" -> {
                // go
            }
            "Town Hall" -> {
                // go
            }
            "Gate" -> {
                game.screen = WorldMapScreen(game)
            }
        }
    }

}
