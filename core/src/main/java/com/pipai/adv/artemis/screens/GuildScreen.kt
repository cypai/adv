package com.pipai.adv.artemis.screens

import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import com.artemis.managers.GroupManager
import com.artemis.managers.TagManager
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.system.animation.AnimationFrameIncrementSystem
import com.pipai.adv.artemis.system.collision.NpcCollisionSystem
import com.pipai.adv.artemis.system.init.GuildScreenInit
import com.pipai.adv.artemis.system.input.CharacterMovementInputSystem
import com.pipai.adv.artemis.system.input.ExitInputProcessor
import com.pipai.adv.artemis.system.input.InputProcessingSystem
import com.pipai.adv.artemis.system.input.InteractionInputSystem
import com.pipai.adv.artemis.system.input.ZoomInputSystem
import com.pipai.adv.artemis.system.misc.CameraFollowSystem
import com.pipai.adv.artemis.system.misc.PartialTextUpdateSystem
import com.pipai.adv.artemis.system.rendering.BattleMapRenderingSystem
import com.pipai.adv.artemis.system.rendering.FpsRenderingSystem
import com.pipai.adv.artemis.system.ui.MainTextboxUiSystem
import com.pipai.adv.gui.BatchHelper
import com.pipai.adv.map.TestMapGenerator
import com.pipai.adv.screen.SwitchableScreen
import com.pipai.adv.utils.getLogger
import net.mostlyoriginal.api.event.common.EventSystem

class GuildScreen(game: AdvGame) : SwitchableScreen(game) {

    private val logger = getLogger()

    private val batch: BatchHelper = game.batchHelper

    private val world: World

    init {
        logger.debug("Starting GuildScreen")

        val globals = game.globals

        val mapTileset = globals.mapTilesetList.getTileset("grassy")

        val npcList = globals.save!!.globalNpcList.shallowCopy()

        val map = TestMapGenerator()
                .generate(npcList, listOf(), 30, 20, mapTileset)

        val config = WorldConfigurationBuilder()
                .with(
                        // Managers
                        TagManager(),
                        GroupManager(),
                        EventSystem(),

                        AnimationFrameIncrementSystem(),

                        InputProcessingSystem(),
                        CharacterMovementInputSystem(game.advConfig),
                        ZoomInputSystem(),
                        InteractionInputSystem(game, this, game.advConfig),
                        PartialTextUpdateSystem(),

                        NpcCollisionSystem())
                .withPassive(-1,
                        CameraFollowSystem(game.advConfig))
                .withPassive(-2,
                        BattleMapRenderingSystem(game.batchHelper, mapTileset,
                                game.advConfig, globals.pccManager, globals.textureManager))
                .withPassive(-3,
                        FpsRenderingSystem(game.batchHelper),
                        MainTextboxUiSystem(game))
                .build()

        world = World(config)

        val inputProcessor = world.getSystem(InputProcessingSystem::class.java)
        inputProcessor.addAlwaysOnProcessor(ExitInputProcessor())
        inputProcessor.addAlwaysOnProcessor(world.getSystem(CharacterMovementInputSystem::class.java))
        inputProcessor.addAlwaysOnProcessor(world.getSystem(ZoomInputSystem::class.java))
        inputProcessor.addAlwaysOnProcessor(world.getSystem(InteractionInputSystem::class.java))
        inputProcessor.activateInput()

        GuildScreenInit(world, game, game.advConfig, npcList, map)
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
