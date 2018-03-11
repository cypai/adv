package com.pipai.adv.artemis.screens

import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import com.artemis.managers.GroupManager
import com.artemis.managers.TagManager
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.system.animation.AnimationFrameIncrementSystem
import com.pipai.adv.artemis.system.animation.BattleAnimationSystem
import com.pipai.adv.artemis.system.input.CameraMovementInputSystem
import com.pipai.adv.artemis.system.input.ExitInputProcessor
import com.pipai.adv.artemis.system.input.InputProcessingSystem
import com.pipai.adv.artemis.system.input.ZoomInputSystem
import com.pipai.adv.artemis.system.misc.*
import com.pipai.adv.artemis.system.rendering.BattleMapRenderingSystem
import com.pipai.adv.artemis.system.rendering.FpsRenderingSystem
import com.pipai.adv.artemis.system.ui.BattleUiSystem
import com.pipai.adv.gui.BatchHelper
import com.pipai.adv.map.TestMapGenerator
import com.pipai.adv.screen.SwitchableScreen
import net.mostlyoriginal.api.event.common.EventSystem

class BattleMapScreen(game: AdvGame) : SwitchableScreen(game) {

    private val batch: BatchHelper = game.batchHelper

    private val world: World

    init {
        val globals = game.globals
        val mapTileset = globals.mapTilesetList.getTileset("grassy")

        // Local battle copy of the npcList to store temp NPCs (such as enemies), which are not needed after the battle
        val npcList = globals.save!!.globalNpcList.shallowCopy()

        val partyList = (0 until npcList.size()).toList()

        val map = TestMapGenerator()
                .generate(game.globals.schemaList, game.globals.weaponSchemaIndex, npcList, partyList, 30, 20, mapTileset)

        val config = WorldConfigurationBuilder()
                .with(
                        // Managers
                        TagManager(),
                        GroupManager(),
                        EventSystem(),

                        InputProcessingSystem(),
                        CameraMovementInputSystem(game.advConfig),
                        CameraInterpolationSystem(),
                        ZoomInputSystem(),

                        TimerSystem(),
                        AnimationFrameIncrementSystem(),
                        PathInterpolationSystem(),
                        XyInterpolationSystem(),
                        ActorInterpolationSystem(),
                        PartialRenderHeightInterpolationSystem(),

                        NpcIdSystem(),
                        BattleAnimationSystem(game),
                        BattleAiSystem())
                .withPassive(-2,
                        BattleMapRenderingSystem(game.skin, game.batchHelper, mapTileset,
                                game.advConfig, globals.pccManager, globals.animatedTilesetManager, globals.textureManager))
                .withPassive(-5,
                        BattleUiSystem(game))
                .withPassive(-6,
                        FpsRenderingSystem(game.batchHelper))
                .build()

        world = World(config)

        val inputProcessor = world.getSystem(InputProcessingSystem::class.java)
        inputProcessor.addProcessor(world.getSystem(CameraMovementInputSystem::class.java))
        inputProcessor.addProcessor(world.getSystem(ZoomInputSystem::class.java))
        inputProcessor.addProcessor(world.getSystem(BattleUiSystem::class.java))
        inputProcessor.addProcessor(world.getSystem(BattleUiSystem::class.java).stage)
        inputProcessor.addAlwaysOnProcessor(ExitInputProcessor())
        inputProcessor.activateInput()

        BattleMapScreenInit(world, game.advConfig, game.globals.save!!, npcList, partyList, map)
                .initialize()
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
