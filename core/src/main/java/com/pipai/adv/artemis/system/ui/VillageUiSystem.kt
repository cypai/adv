package com.pipai.adv.artemis.system.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageList
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.events.PauseEvent
import com.pipai.adv.artemis.screens.*
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.artemis.system.ui.menu.StringMenuItem
import com.pipai.adv.domain.CutsceneUtils
import com.pipai.adv.gui.StandardImageListItemView
import net.mostlyoriginal.api.event.common.Subscribe

class VillageUiSystem(private val game: AdvGame,
                      private val stage: Stage) : NoProcessingSystem() {

    private val skin = game.skin
    private val save = game.globals.save!!

    private val mainTable = Table()
    private val mainMenuList = ImageList(game.skin, "smallMenuList", StandardImageListItemView<StringMenuItem>())

    private val openingCutscene = CutsceneUtils.loadCutscene(Gdx.files.local("assets/data/cutscenes/opening.txt"))

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
                StringMenuItem(save.playerGuild, null, ""),
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
        mainMenuList.disabledFontColor = Color.GRAY
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

        mainMenuList.setDisabledPredicate { it.text in save.variables["disabledVillageOptions"]!!.split('|') }
    }

    private fun handleMainMenuConfirm(menuItem: StringMenuItem) {
        when (menuItem.text) {
            save.playerGuild -> {
                game.screen = GuildScreen(game)
            }
            "Hospital" -> {
                // go
            }
            "Market" -> {
                game.screen = MarketScreen(game)
            }
            "Pub" -> {
                if ("Orphanage" in save.variables["disabledVillageOptions"]!!) {
                    game.screen = CutsceneScreen(game, openingCutscene, "pub")
                } else {
                    game.screen = CutsceneScreen(game, openingCutscene, "pubBartChat")
                }
            }
            "Orphanage" -> {
                game.screen = OrphanageScreen(game)
            }
            "Guild Hall" -> {
                if (save.questTaken("Guild Exam: D")) {
                    game.screen = CutsceneScreen(game, openingCutscene, "guildHallNotFinishedChat")
                } else {
                    game.screen = CutsceneScreen(game, openingCutscene, "guildHall2")
                }
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
