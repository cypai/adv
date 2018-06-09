package com.pipai.adv.artemis.system.ui

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.screens.GuildScreen
import com.pipai.adv.artemis.system.NoProcessingSystem

class VillageUiSystem(private val game: AdvGame,
                      private val stage: Stage) : NoProcessingSystem() {

    init {
        createMainForm()
    }

    override fun initialize() {
        isEnabled = false
    }

    private fun createMainForm() {
        val skin = game.skin
        val config = game.advConfig

        val townHallButton = TextButton("  Town Hall  ", skin)
        townHallButton.x = config.resolution.width * 0.2f
        townHallButton.y = config.resolution.height * 0.7f
        stage.addActor(townHallButton)

        val guildHallButton = TextButton("  Guild Hall  ", skin)
        guildHallButton.x = config.resolution.width * 0.5f
        guildHallButton.y = config.resolution.height * 0.7f
        stage.addActor(guildHallButton)

        val marketButton = TextButton("  Market  ", skin)
        marketButton.x = config.resolution.width * 0.8f
        marketButton.y = config.resolution.height * 0.7f
        stage.addActor(marketButton)

        val hospitalButton = TextButton("  Hospital  ", skin)
        hospitalButton.x = config.resolution.width * 0.2f
        hospitalButton.y = config.resolution.height * 0.4f
        stage.addActor(hospitalButton)

        val pubButton = TextButton("  Pub  ", skin)
        pubButton.x = config.resolution.width * 0.5f
        pubButton.y = config.resolution.height * 0.4f
        pubButton.pad(10f)
        stage.addActor(pubButton)

        val orphanageButton = TextButton("  Orphanage  ", skin)
        orphanageButton.x = config.resolution.width * 0.8f
        orphanageButton.y = config.resolution.height * 0.4f
        stage.addActor(orphanageButton)

        val guildButton = TextButton("  ${game.globals.save!!.playerGuild}  ", skin)
        guildButton.x = config.resolution.width * 0.5f
        guildButton.y = config.resolution.height * 0.1f
        guildButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.screen = GuildScreen(game)
            }
        })
        stage.addActor(guildButton)

        val gateButton = TextButton("  Gate  ", skin)
        gateButton.x = config.resolution.width * 0.5f
        gateButton.y = config.resolution.height * 0.9f
        stage.addActor(gateButton)
    }
}
