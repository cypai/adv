package com.pipai.adv.artemis.screens

import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import com.artemis.managers.GroupManager
import com.artemis.managers.TagManager
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.system.animation.AnimationFrameIncrementSystem
import com.pipai.adv.artemis.system.animation.BattleAnimationSystem
import com.pipai.adv.artemis.system.input.CameraMovementInputSystem
import com.pipai.adv.artemis.system.input.InputProcessingSystem
import com.pipai.adv.artemis.system.input.ZoomInputSystem
import com.pipai.adv.artemis.system.misc.*
import com.pipai.adv.artemis.system.rendering.BattleMapRenderingSystem
import com.pipai.adv.artemis.system.rendering.FpsRenderingSystem
import com.pipai.adv.artemis.system.ui.BattleEndSystem
import com.pipai.adv.artemis.system.ui.BattleUiSystem
import com.pipai.adv.artemis.system.ui.DevUiSystem
import com.pipai.adv.artemis.system.ui.PauseUiSystem
import com.pipai.adv.map.TestMapGenerator
import net.mostlyoriginal.api.event.common.EventSystem

class BattleMapScreen(game: AdvGame) : Screen {

    private val stage = Stage(ScreenViewport(), game.spriteBatch)

    private val world: World

    init {
        val globals = game.globals
        val mapTileset = globals.mapTilesetList.getTileset("grassy")

        // Local battle copy of the npcList to store temp NPCs (such as enemies), which are not needed after the battle
        val npcList = globals.save!!.globalNpcList.shallowCopy()

        val partyList = (0 until npcList.size()).toList()

        val map = TestMapGenerator()
                .generate(game.globals.schemaList, game.globals.weaponSchemaIndex, npcList, partyList, 40, 30, mapTileset)

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

                        CameraFollowSystem(game.advConfig),
                        NpcIdSystem(),
                        BattleAnimationSystem(game),
                        BattleAiSystem(game.globals))
                .withPassive(-2,
                        BattleMapRenderingSystem(game, mapTileset, true))
                .withPassive(-5,
                        BattleUiSystem(game, stage),
                        PauseUiSystem(game, stage, false),
                        BattleEndSystem(game, stage),
                        DevUiSystem(game, stage))
                .withPassive(-6,
                        FpsRenderingSystem(game.batchHelper))
                .build()

        world = World(config)

        val inputProcessor = world.getSystem(InputProcessingSystem::class.java)
        inputProcessor.addProcessor(world.getSystem(CameraMovementInputSystem::class.java))
        inputProcessor.addProcessor(world.getSystem(ZoomInputSystem::class.java))
        inputProcessor.addProcessor(world.getSystem(BattleUiSystem::class.java))
        inputProcessor.addProcessor(world.getSystem(DevUiSystem::class.java))
        inputProcessor.addProcessor(world.getSystem(PauseUiSystem::class.java))
        inputProcessor.addProcessor(stage)
        inputProcessor.activateInput()

        BattleMapScreenInit(world, game.advConfig, game.globals, npcList, partyList, map)
                .initialize()
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        world.setDelta(delta)
        world.process()
        stage.act()
        stage.draw()
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
        stage.dispose()
    }
}
