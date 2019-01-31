package com.pipai.adv.artemis.system.ui

import com.artemis.BaseSystem
import com.artemis.managers.TagManager
import com.badlogic.gdx.Input
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageList
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.components.*
import com.pipai.adv.artemis.events.PauseEvent
import com.pipai.adv.artemis.screens.BattleMapScreen
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.screens.VillageScreen
import com.pipai.adv.artemis.system.misc.PassTimeMovementSystem
import com.pipai.adv.artemis.system.ui.menu.StringMenuItem
import com.pipai.adv.domain.GuildRank
import com.pipai.adv.domain.QuestGoal
import com.pipai.adv.gui.StandardImageListItemView
import com.pipai.adv.map.PointOfInterest
import com.pipai.adv.map.PointOfInterestType
import com.pipai.adv.map.TestMapGenerator
import com.pipai.adv.utils.*
import net.mostlyoriginal.api.event.common.EventSystem

class WorldMapUiSystem(private val game: AdvGame,
                       private val stage: Stage) : BaseSystem(), InputProcessor {

    private val mXy by mapper<XYComponent>()
    private val mPoi by mapper<PointOfInterestComponent>()
    private val mSquad by mapper<SquadComponent>()
    private val mDrawable by mapper<DrawableComponent>()
    private val mText by mapper<TextComponent>()
    private val mCamera by mapper<OrthographicCameraComponent>()
    private val mPath by mapper<PathInterpolationComponent>()

    private val sTags by system<TagManager>()
    private val sEvent by system<EventSystem>()
    private val sPassTimeMovement by system<PassTimeMovementSystem>()

    private val stateMachine = DefaultStateMachine<WorldMapUiSystem, WorldMapUiState>(this)

    private val screenTable = Table()
    private val excursionTable = Table()
    private val excursionLabel = Label("", game.skin)
    private val excursionList = ImageList(game.skin, "smallMenuList", StandardImageListItemView<StringMenuItem>())

    init {
        stateMachine.setInitialState(WorldMapUiState.DISABLED)
        createUi()
    }

    private fun createUi() {
        excursionTable.background = game.skin.getDrawable("frameDrawable")
        excursionTable.pad(16f)
        excursionTable.add(excursionLabel)
                .expand()
                .top()
        excursionTable.row()
        excursionTable.add(excursionList)
                .padTop(16f)

        excursionList.keySelection = true
        excursionList.hoverSelect = true
        excursionList.addConfirmCallback { handleExcursionEvent(it) }

        stage.addActor(screenTable)
    }

    fun stopPassTime() {
        val movingSquadEntities = world.fetch(allOf(SquadComponent::class, PathInterpolationComponent::class))
        movingSquadEntities.forEach {
            mPath.remove(it)
        }
        stateMachine.changeState(WorldMapUiState.DISABLED)
    }

    override fun processSystem() {
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Keys.ESCAPE -> {
                when (stateMachine.currentState) {
                    WorldMapUiState.RUNNING_TIME -> {
                        stopPassTime()
                        stateMachine.changeState(WorldMapUiState.DISABLED)
                    }
                    WorldMapUiState.EXCURSION_SELECTION -> {
                        stateMachine.changeState(WorldMapUiState.DISABLED)
                    }
                    else -> {
                    }
                }
            }
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean = false

    override fun keyTyped(character: Char) = false

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val cCamera = mCamera.get(sTags.getEntityId(Tags.CAMERA.toString()))
        val pickRay = cCamera.camera.getPickRay(screenX.toFloat(), screenY.toFloat())
        val mouseX = pickRay.origin.x
        val mouseY = pickRay.origin.y
        when (button) {
            Input.Buttons.LEFT -> {
                val squadEntities = world.fetch(allOf(SquadComponent::class, XYComponent::class))
                val poiEntities = world.fetch(allOf(PointOfInterestComponent::class, XYComponent::class, DrawableComponent::class))
                for (poiEntity in poiEntities) {
                    val cPoi = mPoi.get(poiEntity)
                    val cXy = mXy.get(poiEntity)
                    val cDrawable = mDrawable.get(poiEntity)
                    val bounds = CollisionBounds.CollisionBoundingBox(cDrawable.width, cDrawable.height, cDrawable.centered)
                    if (CollisionUtils.withinBounds(mouseX, mouseY, cXy.x, cXy.y, bounds)) {
                        val closeSquad = squadEntities.find { squad ->
                            val cSquadXy = mXy.get(squad)
                            CollisionUtils.withinBounds(cSquadXy.x, cSquadXy.y, cXy.x, cXy.y, bounds)
                        }
                        if (closeSquad != null) {
                            val cSquad = mSquad.get(closeSquad)
                            interactWithPoi(cSquad.squad, cPoi.poi)
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    private fun interactWithPoi(squad: String, poi: PointOfInterest) {
        when (poi.type) {
            PointOfInterestType.VILLAGE -> game.screen = VillageScreen(game)
            PointOfInterestType.DUNGEON -> selectDungeonExcursionType(squad, poi)
            PointOfInterestType.QUEST_DUNGEON -> {
            }
        }
    }

    private fun selectDungeonExcursionType(squad: String, poi: PointOfInterest) {
        excursionLabel.setText(poi.name)

        val excursions: MutableList<StringMenuItem> = mutableListOf()
        if (game.globals.save!!.playerTheoreticalRank() != GuildRank.F) {
            excursions.add(StringMenuItem("Exploration", null, "")
                    .withData("squad", squad))
        }

        game.globals.save!!.activeQuests.forEach { questName, stageName ->
            val quest = game.globals.progressionBackend.getQuest(questName)
            val stage = quest.stages[stageName]!!
            stage.goals.forEach {
                when (it) {
                    is QuestGoal.ItemRetrievalGoal -> {
                        if (it.location == poi.name) {
                            excursions.add(StringMenuItem("Retrieve Item: $questName", null, "")
                                    .withData("squad", squad)
                                    .withData("quest", quest)
                                    .withData("stage", stage)
                                    .withData("goal", it))
                        }
                    }
                    is QuestGoal.ClearMapGoal -> {
                        if (it.location == poi.name) {
                            excursions.add(StringMenuItem("Clear Map: $questName", null, "")
                                    .withData("squad", squad)
                                    .withData("quest", quest)
                                    .withData("stage", stage)
                                    .withData("goal", it))
                        }
                    }
                }
            }
        }

        excursionList.setItems(excursions)
        excursionList.setSelectedIndex(0)
        excursionTable.width = excursionTable.prefWidth
        excursionTable.height = excursionTable.prefHeight
        excursionTable.x = (game.advConfig.resolution.width - excursionTable.width) / 2
        excursionTable.y = (game.advConfig.resolution.height - excursionTable.height) / 2

        stateMachine.changeState(WorldMapUiState.EXCURSION_SELECTION)
    }

    private fun handleExcursionEvent(excursion: StringMenuItem) {
        val squad = game.globals.save!!.squads[excursion.getData("squad") as String]!!
        when {
            excursion.text == "Exploration" -> game.screen = BattleMapScreen(game, squad, TestMapGenerator(), null)
            excursion.text.startsWith("Retrieve Item") -> {
                game.screen = BattleMapScreen(game, squad, TestMapGenerator(), excursion.getData("goal")!! as QuestGoal)
            }
            excursion.text.startsWith("Clear Map") -> {
                game.screen = BattleMapScreen(game, squad, TestMapGenerator(), null)
            }
        }
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) = false

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        val cCamera = mCamera.get(sTags.getEntityId(Tags.CAMERA.toString()))
        val pickRay = cCamera.camera.getPickRay(screenX.toFloat(), screenY.toFloat())
        val mouseX = pickRay.origin.x
        val mouseY = pickRay.origin.y
        val poiEntities = world.fetch(allOf(PointOfInterestComponent::class, XYComponent::class, DrawableComponent::class))
        for (poiEntity in poiEntities) {
            val cPoi = mPoi.get(poiEntity)
            val cXy = mXy.get(poiEntity)
            val cDrawable = mDrawable.get(poiEntity)
            val bounds = CollisionBounds.CollisionBoundingBox(cDrawable.width, cDrawable.height, cDrawable.centered)
            if (CollisionUtils.withinBounds(mouseX, mouseY, cXy.x, cXy.y, bounds)) {
                val cText = mText.create(poiEntity)
                cText.text = cPoi.poi.name
            } else {
                mText.remove(poiEntity)
            }
        }
        return false
    }

    override fun scrolled(amount: Int): Boolean = false

    enum class WorldMapUiState : State<WorldMapUiSystem> {
        DISABLED() {
            override fun enter(uiSystem: WorldMapUiSystem) {
                uiSystem.sEvent.dispatch(PauseEvent(false))
            }
        },
        EXCURSION_SELECTION() {
            override fun enter(uiSystem: WorldMapUiSystem) {
                uiSystem.stage.keyboardFocus = uiSystem.excursionTable
                uiSystem.stage.addActor(uiSystem.excursionTable)
            }

            override fun exit(uiSystem: WorldMapUiSystem) {
                uiSystem.excursionTable.remove()
            }
        },
        RUNNING_TIME() {
            override fun enter(uiSystem: WorldMapUiSystem) {
                uiSystem.sPassTimeMovement.isEnabled = true
            }

            override fun exit(uiSystem: WorldMapUiSystem) {
                uiSystem.sPassTimeMovement.isEnabled = false
            }
        };

        override fun enter(uiSystem: WorldMapUiSystem) {
        }

        override fun exit(uiSystem: WorldMapUiSystem) {
        }

        override fun onMessage(uiSystem: WorldMapUiSystem, telegram: Telegram): Boolean {
            return false
        }

        override fun update(uiSystem: WorldMapUiSystem) {
        }
    }

}
