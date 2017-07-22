package com.pipai.adv.artemis.screens

import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import com.artemis.managers.GroupManager
import com.artemis.managers.TagManager
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL20
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.system.init.BattleMapScreenInit
import com.pipai.adv.artemis.system.input.ExitInputProcessor
import com.pipai.adv.artemis.system.rendering.BattleMapRenderingSystem
import com.pipai.adv.artemis.system.rendering.FpsRenderingSystem
import com.pipai.adv.gui.BatchHelper
import com.pipai.adv.map.TestMapGenerator
import com.pipai.adv.screen.SwitchableScreen
import com.pipai.adv.tiles.GrassyTileset
import net.mostlyoriginal.api.event.common.EventSystem

class BattleMapScreen(game: AdvGame) : SwitchableScreen(game) {

    private val batch: BatchHelper = game.batchHelper
    private val multiplexer: InputMultiplexer = InputMultiplexer()

    private val world: World

    init {
        val ftile = Gdx.files.internal("assets/binassets/graphics/tilesets/outside_tileset.png")
        val mapTileset = GrassyTileset(ftile)

        val map = TestMapGenerator()
                .generate(20, 20, mapTileset)

        val config = WorldConfigurationBuilder()
                .with(
                        // Managers
                        TagManager(),
                        GroupManager(),
                        EventSystem())
                .withPassive(-1,
                        BattleMapRenderingSystem(game.batchHelper, mapTileset))
                .withPassive(-3,
                        FpsRenderingSystem(game.batchHelper))
                .build()
        world = World(config)
        multiplexer.addProcessor(ExitInputProcessor())
        Gdx.input.inputProcessor = multiplexer

        BattleMapScreenInit(world, map).initialize()
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        world.setDelta(delta)
        world.process()
    }

    override fun resize(width: Int, height: Int) {
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun show() {
    }

    override fun hide() {
    }

    override fun dispose() {
        world.dispose()
    }
}
