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
import com.pipai.adv.artemis.system.cutscene.BattleCutsceneSystem
import com.pipai.adv.artemis.system.cutscene.directors.BattleCutsceneDirector
import com.pipai.adv.artemis.system.cutscene.directors.OpeningDirector
import com.pipai.adv.artemis.system.cutscene.directors.TutorialDirector
import com.pipai.adv.artemis.system.input.CameraMovementInputSystem
import com.pipai.adv.artemis.system.input.CutsceneInputSystem
import com.pipai.adv.artemis.system.input.InputProcessingSystem
import com.pipai.adv.artemis.system.input.ZoomInputSystem
import com.pipai.adv.artemis.system.misc.*
import com.pipai.adv.artemis.system.rendering.BackgroundRenderingSystem
import com.pipai.adv.artemis.system.rendering.BattleMapRenderingSystem
import com.pipai.adv.artemis.system.rendering.FpsRenderingSystem
import com.pipai.adv.artemis.system.ui.*
import com.pipai.adv.backend.battle.domain.EnvObject
import com.pipai.adv.map.MapGenerator
import com.pipai.adv.utils.AutoIncrementIdMap
import net.mostlyoriginal.api.event.common.EventSystem

class BattleMapScreen(game: AdvGame, partyList: List<Int>, mapGenerator: MapGenerator) : Screen {

    private val stage = Stage(ScreenViewport(), game.spriteBatch)

    private val world: World

    init {
        val globals = game.globals
        val mapTileset = globals.mapTilesetList.getTileset("grassy")

        // Local battle copy of the npcList to store temp NPCs (such as enemies), which are not needed after the battle
        val npcList = globals.save!!.globalNpcList.shallowCopy()
        val envObjList = AutoIncrementIdMap<EnvObject>()

        val map = mapGenerator
                .generate(game.globals.unitSchemaIndex, game.globals.weaponSchemaIndex, npcList, envObjList, partyList, 40, 30, mapTileset)

        val cutsceneDirectors: MutableList<BattleCutsceneDirector> = mutableListOf()

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
                        CutsceneInputSystem(game),

                        TimerSystem(),
                        AnimationFrameIncrementSystem(),
                        PathInterpolationSystem(),
                        XyInterpolationSystem(),
                        ActorInterpolationSystem(),
                        PartialRenderHeightInterpolationSystem(),

                        CameraFollowSystem(game.advConfig),
                        NpcIdSystem(),
                        EnvObjIdSystem(),
                        BattleAnimationSystem(game),
                        BattleCutsceneSystem(cutsceneDirectors),
                        BattleAiSystem(game.globals))
                .withPassive(-1,
                        BattleMapRenderingSystem(game, mapTileset, true),
                        BackgroundRenderingSystem(game))
                .withPassive(-2,
                        BattleUiSystem(game, npcList, stage),
                        MainTextboxUiSystem(game),
                        PauseUiSystem(game, stage, false),
                        BattleEndSystem(game, stage),
                        DevUiSystem(game, stage))
                .withPassive(-3,
                        FpsRenderingSystem(game.batchHelper))
                .build()

        world = World(config)

        cutsceneDirectors.add(OpeningDirector())
        cutsceneDirectors.add(TutorialDirector(world))

        val inputProcessor = world.getSystem(InputProcessingSystem::class.java)
        inputProcessor.addProcessor(world.getSystem(CameraMovementInputSystem::class.java))
        inputProcessor.addProcessor(world.getSystem(ZoomInputSystem::class.java))
        inputProcessor.addProcessor(world.getSystem(BattleUiSystem::class.java))
        inputProcessor.addProcessor(world.getSystem(DevUiSystem::class.java))
        inputProcessor.addProcessor(world.getSystem(PauseUiSystem::class.java))
        inputProcessor.addProcessor(world.getSystem(CutsceneInputSystem::class.java))
        inputProcessor.addProcessor(stage)
        inputProcessor.activateInput()

        BattleMapScreenInit(world, game.advConfig, game.globals, npcList, envObjList, partyList, map)
                .initialize()

        world.getSystem(BattleCutsceneSystem::class.java).checkAllCutsceneDirectors()
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
