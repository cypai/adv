package com.pipai.adv.artemis.system.ui

import com.artemis.BaseSystem
import com.artemis.managers.TagManager
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.*
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.components.*
import com.pipai.adv.artemis.events.CommandAnimationEndEvent
import com.pipai.adv.artemis.events.MouseCameraMoveDisableEvent
import com.pipai.adv.artemis.events.TileHighlightUpdateEvent
import com.pipai.adv.artemis.events.ZoomScrollDisableEvent
import com.pipai.adv.artemis.screens.BattleMapScreenInit
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.system.animation.BattleAnimationSystem
import com.pipai.adv.artemis.system.misc.CameraInterpolationSystem
import com.pipai.adv.artemis.system.misc.NpcIdSystem
import com.pipai.adv.artemis.system.ui.menu.MenuItem
import com.pipai.adv.artemis.system.ui.menu.StringMenuItem
import com.pipai.adv.artemis.system.ui.menu.TargetMenuCommandItem
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata
import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.backend.battle.domain.Team
import com.pipai.adv.backend.battle.engine.MapGraph
import com.pipai.adv.backend.battle.engine.commands.*
import com.pipai.adv.backend.battle.engine.domain.PreviewComponent
import com.pipai.adv.backend.battle.engine.rules.execution.AttackCalculationExecutionRule
import com.pipai.adv.backend.battle.utils.BattleUtils
import com.pipai.adv.gui.UiConstants
import com.pipai.adv.tiles.UnitAnimationFrame
import com.pipai.adv.utils.*
import net.mostlyoriginal.api.event.common.EventSystem
import net.mostlyoriginal.api.event.common.Subscribe

class BattleUiSystem(private val game: AdvGame) : BaseSystem(), InputProcessor {

    private val logger = getLogger()

    private val mDrawable by mapper<DrawableComponent>()
    private val mSideUiBox by mapper<SideUiBoxComponent>()
    private val mActor by mapper<ActorComponent>()
    private val mXy by mapper<XYComponent>()
    private val mPath by mapper<PathInterpolationComponent>()

    private val mBackend by mapper<BattleBackendComponent>()
    private val mCamera by mapper<OrthographicCameraComponent>()

    private val mNpcId by mapper<NpcIdComponent>()
    private val mPlayerUnit by mapper<PlayerUnitComponent>()
    private val mCollision by mapper<CollisionComponent>()

    private val sNpcId by system<NpcIdSystem>()
    private val sCameraInterpolation by system<CameraInterpolationSystem>()
    private val sBattleAnimation by system<BattleAnimationSystem>()

    private val sTags by system<TagManager>()
    private val sEvent by system<EventSystem>()

    private val stateMachine = DefaultStateMachine<BattleUiSystem, BattleUiState>(this, BattleUiState.NOTHING_SELECTED)

    private val frameDrawable = game.skin.getDrawable("frame")
    private val frameBgDrawable = game.skin.getDrawable("bg")
    private val portraitBgDrawable = game.skin.newDrawable("white", Color.DARK_GRAY)

    val stage = Stage(ScreenViewport())
    private val primaryActionMenu = ImageList(game.skin, "menuList", object : ImageList.ImageListItemView<MenuItem> {
        override fun getItemImage(item: MenuItem): TextureRegion? = item.image
        override fun getItemText(item: MenuItem): String = item.text
        override fun getSpacing(): Float = 10f
    })
    private var primaryActionMenuEntityId: Int = 0

    private val commandPreviewTable = Table()
    private val commandPreviewTitle = Label("", game.skin)
    private val commandPreviewList = ImageList(game.skin, "smallMenuList", object : ImageList.ImageListItemView<StringMenuItem> {
        override fun getItemImage(item: StringMenuItem): TextureRegion? = null
        override fun getItemText(item: StringMenuItem): String = item.text
        override fun getSpacing(): Float = 10f
        override fun getItemRightText(item: StringMenuItem): String = item.rightText
        override fun getRightSpacing(): Float = 16f
    })
    private val commandPreviewDetailsList = ImageList(game.skin, "smallMenuList", object : ImageList.ImageListItemView<StringMenuItem> {
        override fun getItemImage(item: StringMenuItem): TextureRegion? = null
        override fun getItemText(item: StringMenuItem): String = item.text
        override fun getSpacing(): Float = 10f
        override fun getItemRightText(item: StringMenuItem): String = item.rightText
        override fun getRightSpacing(): Float = 16f
    })
    private val commandPreviewDetailsScrollPane = ScrollPane(commandPreviewDetailsList, game.skin)
    private val commandConfirmButton = TextButton("Execute", game.skin)

    private val movePreviewDrawable = game.skin.newDrawable("white", Color(0.3f, 0.3f, 0.8f, 0.7f))
    private val movePreviewDrawableSize = 6f

    var selectedNpcId: Int? = null
        private set

    private val targetNpcIds: MutableList<Pair<Int, TargetCommand>> = mutableListOf()
    private var targetIndex: Int? = null

    private var mapGraph: MapGraph? = null
    private var hoverDestination: GridPosition? = null
    private var movePreviewEntityId: Int? = null

    companion object {
        const val PORTRAIT_WIDTH = 80f
        const val PORTRAIT_HEIGHT = 80f
        const val PADDING = 8f
        const val BAR_WIDTH = 80f
        const val BAR_HEIGHT = 8f
        const val BAR_VERTICAL_PADDING = 12f
        const val BAR_TEXT_PADDING = 8f
        const val POST_BAR_PADDING = 64f
        const val SELECTION_DISTANCE = 16f
        const val UI_WIDTH = PADDING + PORTRAIT_WIDTH + PADDING + BAR_WIDTH + POST_BAR_PADDING + SELECTION_DISTANCE
        const val UI_HEIGHT = PADDING + PORTRAIT_HEIGHT + PADDING

        const val SELECTION_TIME = 10

        const val ACTION_UI_WIDTH = 160f

        val BLUE_MOVE_COLOR = Color(0.3f, 0.3f, 0.8f, 0.4f)
        val YELLOW_MOVE_COLOR = Color(0.8f, 0.6f, 0f, 0.4f)
        val TARGET_COLOR = YELLOW_MOVE_COLOR
        val NON_TARGET_COLOR = Color(0.3f, 0.3f, 0.3f, 0.4f)
    }

    override fun initialize() {
        primaryActionMenu.hoverSelect = true
        primaryActionMenu.disabledFontColor = Color.GRAY
        primaryActionMenu.x = PADDING
        primaryActionMenu.y = PADDING
        primaryActionMenu.width = ACTION_UI_WIDTH
        primaryActionMenu.height = primaryActionMenu.prefHeight
        primaryActionMenu.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                if (!primaryActionMenu.lockSelection) {
                    handleMenuSelect(primaryActionMenu.getSelected())
                }
            }
        })

        primaryActionMenuEntityId = world.create()
        val cPrimaryActionMenu = mActor.create(primaryActionMenuEntityId)
        cPrimaryActionMenu.actor = primaryActionMenu

        commandPreviewTitle.setText("Attack")
        commandPreviewList.disabledFontColor = Color.GRAY
        commandPreviewList.setItems(listOf(
                StringMenuItem("Hit", null, ""),
                StringMenuItem("Crit", null, ""),
                StringMenuItem("Damage", null, ""),
                StringMenuItem("Effects", null, "")))
        commandPreviewList.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                val backend = getBackend()
                val previewComponents = backend.preview(targetNpcIds[targetIndex!!].second)
                updateRightPreviewDetails(previewComponents)
            }
        })
        commandPreviewDetailsList.setItems(listOf())
        commandPreviewTable.add(commandPreviewTitle)
                .expandX()
        commandPreviewTable.row()

        val previewWidth = MathUtils.clamp(game.advConfig.resolution.width * 0.4f, 500f, 600f)
        val leftPreviewPercentage = 0.45f
        val rightPreviewPercentage = 1f - leftPreviewPercentage - 0.05f
        val commandPreviewInnerTable = Table()
        commandPreviewInnerTable.add(commandPreviewList)
                .width(previewWidth * leftPreviewPercentage)
        commandPreviewDetailsScrollPane.setFadeScrollBars(false)
        commandPreviewDetailsScrollPane.addListener(object : InputListener() {
            override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                stage.scrollFocus = commandPreviewDetailsScrollPane
                sEvent.dispatch(ZoomScrollDisableEvent(true))
            }

            override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                stage.scrollFocus = null
                sEvent.dispatch(ZoomScrollDisableEvent(false))
            }
        })
        commandPreviewInnerTable.add(commandPreviewDetailsScrollPane)
                .width(previewWidth * rightPreviewPercentage)
                .minHeight(commandPreviewList.prefHeight)
                .maxHeight(commandPreviewList.prefHeight)
        commandPreviewTable.add(commandPreviewInnerTable)
                .padBottom(PADDING)
        commandPreviewTable.row()
        commandPreviewTable.add(commandConfirmButton)
                .width(120f)
                .padBottom(PADDING)
        commandPreviewTable.touchable = Touchable.enabled
        commandPreviewTable.background = game.skin.getDrawable("frameDrawable")
        commandPreviewTable.height = commandPreviewTable.prefHeight
        commandPreviewTable.width = previewWidth
        commandPreviewTable.x = game.advConfig.resolution.width / 2f - previewWidth / 2f
        commandPreviewTable.y = PADDING

        primaryActionMenu.addListener(object : InputListener() {
            override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                sEvent.dispatch(MouseCameraMoveDisableEvent(true))
            }

            override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                sEvent.dispatch(MouseCameraMoveDisableEvent(false))
            }
        })

        commandPreviewTable.addListener(object : InputListener() {
            override fun enter(event: InputEvent?, x: Float, y: Float, pointer: Int, fromActor: Actor?) {
                sEvent.dispatch(MouseCameraMoveDisableEvent(true))
            }

            override fun exit(event: InputEvent?, x: Float, y: Float, pointer: Int, toActor: Actor?) {
                sEvent.dispatch(MouseCameraMoveDisableEvent(false))
            }
        })

        commandConfirmButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                executeCommand(targetNpcIds[targetIndex!!].second)
            }
        })
    }

    @Subscribe
    fun commandAnimationEndListener(@Suppress("UNUSED_PARAMETER") event: CommandAnimationEndEvent) {
        val backend = getBackend()
        if (backend.getNpcAp(selectedNpcId!!) <= 0) {
            selectNextPlayer()
        }
        if (selectedNpcId == null) {
            // End turn
        } else {
            stateMachine.changeState(BattleUiState.PLAYER_SELECTED)
        }
    }

    private fun executeCommand(command: BattleCommand) {
        val backend = getBackend()
        val executionStatus = backend.canBeExecuted(command)
        if (executionStatus.executable) {
            val events = backend.execute(command)
            stateMachine.changeState(BattleUiState.ANIMATING)
            sBattleAnimation.processBattleEvents(events)
        } else {
            logger.debug("Unable to execute: ${executionStatus.reason}")
        }
    }

    private fun showMoveTileHighlights(npcId: Int) {
        val factory = MoveCommandFactory(getBackend())
        val theMapGraph = factory.getMapGraph(npcId)
        mapGraph = theMapGraph
        val ap = theMapGraph.ap
        val tileHighlights: MutableMap<Color, List<GridPosition>> = mutableMapOf()
        when (ap) {
            1 -> tileHighlights[YELLOW_MOVE_COLOR] = theMapGraph.getMovableCellPositions(1)
            2 -> {
                tileHighlights[BLUE_MOVE_COLOR] = theMapGraph.getMovableCellPositions(1)
                tileHighlights[YELLOW_MOVE_COLOR] = theMapGraph.getMovableCellPositions(2)
            }
            else -> {
            }
        }
        sEvent.dispatch(TileHighlightUpdateEvent(tileHighlights))
    }

    private fun clearTileHighlights() {
        sEvent.dispatch(TileHighlightUpdateEvent(mapOf()))
    }

    private fun clearMovementPreview() {
        movePreviewEntityId?.let { world.delete(it) }
        movePreviewEntityId = null
    }

    private fun createMovePreview(path: List<Vector2>) {
        movePreviewEntityId?.let { world.delete(it) }

        val previewId = world.create()

        val cDrawable = mDrawable.create(previewId)
        cDrawable.drawable = movePreviewDrawable
        cDrawable.width = movePreviewDrawableSize
        cDrawable.height = movePreviewDrawableSize
        val cXy = mXy.create(previewId)
        cXy.setXy(path.first())
        val cPath = mPath.create(previewId)
        cPath.onEnd = PathInterpolationEndStrategy.RESTART
        cPath.interpolation = Interpolation.linear
        cPath.setUsingSpeed(6.0)
        cPath.endpoints.addAll(path)

        movePreviewEntityId = previewId
    }

    private fun selectRightUiBox(npcId: Int) {
        val uiEntityId = world.fetch(allOf(SideUiBoxComponent::class, XYComponent::class))
                .find { mSideUiBox.get(it).orientation == SideUiBoxOrientation.PORTRAIT_LEFT && mSideUiBox.get(it).npcId == npcId }
        if (uiEntityId != null) {
            val cXy = mXy.get(uiEntityId)
            val cPath = mPath.create(uiEntityId)
            cPath.interpolation = Interpolation.linear
            cPath.endpoints.clear()
            cPath.endpoints.add(cXy.toVector2())
            cPath.endpoints.add(Vector2(game.advConfig.resolution.width - UI_WIDTH, cXy.y))
            cPath.maxT = SELECTION_TIME
        }
    }

    private fun deselectRightUiBoxes() {
        val uiEntityIds = world.fetch(allOf(SideUiBoxComponent::class, XYComponent::class))
                .filter { mSideUiBox.get(it).orientation == SideUiBoxOrientation.PORTRAIT_LEFT }
        uiEntityIds.forEach { uiEntityId ->
            val destinationX = game.advConfig.resolution.width - UI_WIDTH + SELECTION_DISTANCE
            val cXy = mXy.get(uiEntityId)
            if (cXy.x < destinationX) {
                val cPath = mPath.create(uiEntityId)
                cPath.interpolation = Interpolation.linear
                cPath.endpoints.clear()
                cPath.endpoints.add(cXy.toVector2())
                cPath.endpoints.add(Vector2(destinationX, cXy.y))
                cPath.maxT = SELECTION_TIME
            }
        }
    }

    private fun activatePrimaryActionMenu() {
        setPrimaryActionMenuItems()
        val cPrimaryMenuPath = mPath.create(primaryActionMenuEntityId)
        cPrimaryMenuPath.interpolation = Interpolation.linear
        cPrimaryMenuPath.endpoints.clear()
        cPrimaryMenuPath.endpoints.add(Vector2(-ACTION_UI_WIDTH, primaryActionMenu.y))
        cPrimaryMenuPath.endpoints.add(Vector2(PADDING, primaryActionMenu.y))
        cPrimaryMenuPath.maxT = SELECTION_TIME
        stage.addActor(primaryActionMenu)
    }

    private fun setPrimaryActionMenuItems() {
        val backend = getBackend()
        val normalAttackFactory = NormalAttackCommandFactory(backend)
        primaryActionMenu.setItems(listOf(
                TargetMenuCommandItem("Attack", null, normalAttackFactory),
                StringMenuItem("Skill", null, ""),
                StringMenuItem("Reload", null, ""),
                StringMenuItem("Item", null, ""),
                StringMenuItem("Defend", null, ""),
                StringMenuItem("Wait", null, ""),
                StringMenuItem("Run", null, "")))

        val npcId = selectedNpcId!!
        if (normalAttackFactory.generateInvalid(npcId).isEmpty()) {
            primaryActionMenu.setDisabledIndex(0, true)
        }
        val weapon = backend.getNpc(npcId)!!.unitInstance.weapon
        if (weapon == null || !BattleUtils.weaponRequiresAmmo(weapon) || weapon.ammo < weapon.schema.magazineSize) {
            primaryActionMenu.setDisabledIndex(2, true)
        }
        val position = backend.getNpcPosition(npcId)!!
        val map = backend.getBattleMapState()
        if (position.x != 0 && position.x != map.width - 1 && position.y != 0 && position.y != map.height - 1) {
            primaryActionMenu.setDisabledIndex(6, true)
        }
        primaryActionMenu.height = primaryActionMenu.prefHeight
    }

    private fun handleMenuSelect(menuItem: MenuItem) {
        when (menuItem) {
            is TargetMenuCommandItem -> {
                val commands = menuItem.factory.generateInvalid(selectedNpcId!!)
                targetNpcIds.clear()
                targetNpcIds.addAll(commands.map { Pair(it.targetId, it) })
                stateMachine.changeState(BattleUiState.TARGET_SELECTION)
            }
        }
    }

    private fun createLeftUiBox(npcId: Int, index: Int, selected: Boolean = false) {
        val entityId = world.create()
        val cUi = mSideUiBox.create(entityId)
        cUi.setToNpc(npcId, getBackend())
        cUi.orientation = SideUiBoxOrientation.PORTRAIT_RIGHT
        val cXy = mXy.create(entityId)
        cXy.x = -UI_WIDTH / 2
        cXy.y = game.advConfig.resolution.height - (BattleUiSystem.UI_HEIGHT + BattleMapScreenInit.UI_VERTICAL_PADDING) * (index + 1)
        val cPath = mPath.create(entityId)
        cPath.interpolation = Interpolation.linear
        cPath.endpoints.clear()
        cPath.endpoints.add(cXy.toVector2())
        cPath.endpoints.add(Vector2(if (selected) 0f else -SELECTION_DISTANCE, cXy.y))
        cPath.maxT = SELECTION_TIME
    }

    private fun clearLeftUiBoxes() {
        val uiEntityIds = world.fetch(allOf(SideUiBoxComponent::class, XYComponent::class))
                .filter { mSideUiBox.get(it).orientation == SideUiBoxOrientation.PORTRAIT_RIGHT }
        uiEntityIds.forEach { uiEntityId ->
            val cXy = mXy.get(uiEntityId)
            val cPath = mPath.create(uiEntityId)
            cPath.interpolation = Interpolation.linear
            cPath.endpoints.clear()
            cPath.endpoints.add(cXy.toVector2())
            cPath.endpoints.add(Vector2(-UI_WIDTH, cXy.y))
            cPath.maxT = SELECTION_TIME
            cPath.onEnd = PathInterpolationEndStrategy.DESTROY
        }
    }

    private fun selectLeftUiBox(npcId: Int) {
        val uiEntityId = world.fetch(allOf(SideUiBoxComponent::class, XYComponent::class))
                .find { mSideUiBox.get(it).orientation == SideUiBoxOrientation.PORTRAIT_RIGHT && mSideUiBox.get(it).npcId == npcId }
        if (uiEntityId != null) {
            val cXy = mXy.get(uiEntityId)
            val cPath = mPath.create(uiEntityId)
            cPath.interpolation = Interpolation.linear
            cPath.endpoints.clear()
            cPath.endpoints.add(cXy.toVector2())
            cPath.endpoints.add(Vector2(0f, cXy.y))
            cPath.maxT = SELECTION_TIME
        }
    }

    private fun deselectLeftUiBoxes() {
        val uiEntityIds = world.fetch(allOf(SideUiBoxComponent::class, XYComponent::class))
                .filter { mSideUiBox.get(it).orientation == SideUiBoxOrientation.PORTRAIT_RIGHT }
        uiEntityIds.forEach { uiEntityId ->
            val destinationX = -SELECTION_DISTANCE
            val cXy = mXy.get(uiEntityId)
            if (cXy.x > destinationX) {
                val cPath = mPath.create(uiEntityId)
                cPath.interpolation = Interpolation.linear
                cPath.endpoints.clear()
                cPath.endpoints.add(cXy.toVector2())
                cPath.endpoints.add(Vector2(destinationX, cXy.y))
                cPath.maxT = SELECTION_TIME
            }
        }
    }

    private fun fetchNpcUnits(): List<Int> {
        val npcUnitEntityBag = world.aspectSubscriptionManager.get(allOf(
                NpcIdComponent::class, XYComponent::class, CollisionComponent::class)).entities
        return npcUnitEntityBag.data.slice(0 until npcUnitEntityBag.size())
    }

    private fun fetchPlayerUnits(): List<Int> {
        val playerUnitEntityBag = world.aspectSubscriptionManager.get(allOf(
                NpcIdComponent::class, PlayerUnitComponent::class, XYComponent::class, CollisionComponent::class)).entities
        return playerUnitEntityBag.data.slice(0 until playerUnitEntityBag.size())
    }

    private fun select(npcId: Int?) {
        if (npcId == selectedNpcId && npcId != null) {
            // Implementation of double-clicking sending camera to unit position
            val unitEntityId = sNpcId.getNpcEntityId(npcId)!!
            val cUnitXy = mXy.get(unitEntityId)
            sCameraInterpolation.sendCameraToPosition(cUnitXy.toVector2())
            return
        }

        selectedNpcId = npcId

        if (npcId == null) {
            stateMachine.changeState(BattleUiState.NOTHING_SELECTED)
        } else {
            val backend = mBackend.get(sTags.getEntityId(Tags.BACKEND.toString())).backend
            val nextSelectedTeam = backend.getNpcTeams()[npcId]!!
            when (nextSelectedTeam) {
                Team.PLAYER -> stateMachine.changeState(BattleUiState.PLAYER_SELECTED)
                Team.AI -> stateMachine.changeState(BattleUiState.ENEMY_SELECTED)
            }
            val npcEntityId = sNpcId.getNpcEntityId(npcId)!!
            val cUnitXy = mXy.get(npcEntityId)
            sCameraInterpolation.sendCameraToPosition(cUnitXy.toVector2())
        }
    }

    private fun selectNextPlayer() {
        val backend = getBackend()
        val playerUnits = fetchPlayerUnits()
                .filter { backend.getNpcAp(mNpcId.get(it).npcId) > 0 }
                .map { Pair(mPlayerUnit.get(it).index, mNpcId.get(it).npcId) }
                .sortedBy { it.first }
        val currentSelectedUnit = selectedNpcId
        if (currentSelectedUnit == null) {
            select(playerUnits.firstOrNull()?.second)
        } else {
            val currentIndex = mPlayerUnit.getSafe(sNpcId.getNpcEntityId(currentSelectedUnit)!!, null)?.index
            if (currentIndex != null) {
                val next = playerUnits.firstOrNull { it.first > currentIndex }
                if (next == null) {
                    select(playerUnits.minBy { it.first }?.second)
                } else {
                    select(next.second)
                }
            } else {
                select(playerUnits.firstOrNull()?.second)
            }
        }
    }

    private fun selectNextTarget() {
        targetIndex?.let {
            var index = it + 1
            if (index >= targetNpcIds.size) {
                index = 0
            }
            selectTarget(targetNpcIds[index].first)
        }
    }

    private fun selectTarget(npcId: Int) {
        var index = 0
        targetNpcIds.forEach {
            if (it.first == npcId) {
                deselectLeftUiBoxes()
                selectLeftUiBox(npcId)
                targetIndex = index
                val npcEntityId = sNpcId.getNpcEntityId(npcId)!!
                val cXy = mXy.get(npcEntityId)
                sCameraInterpolation.sendCameraToPosition(cXy.toVector2())
                updatePreviewDetails(it.second)
                return@forEach
            }
            index++
        }
        showTargetTileHighlights()
    }

    private fun updatePreviewDetails(command: BattleCommand) {
        val backend = getBackend()
        val previewComponents = backend.preview(command)
        val calculator = AttackCalculationExecutionRule()
        val toHit = calculator.calculateToHit(previewComponents)
        val toCrit = calculator.calculateToCrit(previewComponents)
        val damageRange = calculator.calculateDamageRange(previewComponents)
        val leftPreviewList: MutableList<StringMenuItem> = mutableListOf()
        leftPreviewList.add(StringMenuItem("Hit", null, toHit?.let { "$it %" } ?: ""))
        leftPreviewList.add(StringMenuItem("Crit", null, toCrit?.let { "$it %" } ?: ""))
        leftPreviewList.add(StringMenuItem("Damage", null, damageRange?.let { "${it.first} - ${it.second}" } ?: ""))
        leftPreviewList.add(StringMenuItem("Effects", null, ""))
        commandPreviewList.clearItems()
        commandPreviewList.setItems(leftPreviewList)
        if (calculator.toHitComponents(previewComponents) == null) {
            commandPreviewList.setDisabledIndex(0, true)
        }
        if (calculator.toCritComponents(previewComponents) == null) {
            commandPreviewList.setDisabledIndex(1, true)
        }
        if (calculator.damageRangeComponents(previewComponents) == null) {
            commandPreviewList.setDisabledIndex(2, true)
        }
        commandPreviewList.setDisabledIndex(3, true)
        updateRightPreviewDetails(previewComponents)
        if (commandPreviewTable.stage == null) {
            stage.addActor(commandPreviewTable)
        }
    }

    private fun updateRightPreviewDetails(preview: List<PreviewComponent>) {
        val rightPreviewList: MutableList<StringMenuItem> = mutableListOf()
        val selected = commandPreviewList.getSelected()
        val calculator = AttackCalculationExecutionRule()
        when (selected.text) {
            "Hit" -> {
                val details = calculator.toHitComponents(preview)
                if (details != null) {
                    rightPreviewList.add(previewToStringMenuItem(details.first))
                    rightPreviewList.addAll(details.second.map { previewToStringMenuItem(it) })
                }
            }
            "Crit" -> {
                val details = calculator.toCritComponents(preview)
                if (details != null) {
                    rightPreviewList.add(previewToStringMenuItem(details.first))
                    rightPreviewList.addAll(details.second.map { previewToStringMenuItem(it) })
                }
            }
            "Damage" -> {
                val details = calculator.damageRangeComponents(preview)
                if (details != null) {
                    rightPreviewList.add(previewToStringMenuItem(details.first))
                    rightPreviewList.addAll(details.second.map { previewToStringMenuItem(it) })
                }
            }
        }
        if (rightPreviewList.isEmpty()) {
            commandPreviewDetailsList.clearItems()
        } else {
            commandPreviewDetailsList.setItems(rightPreviewList)
        }
    }

    private fun previewToStringMenuItem(preview: PreviewComponent): StringMenuItem {
        return StringMenuItem(preview.description, null, preview.rightText())
    }

    private fun showTargetTileHighlights() {
        val backend = getBackend()
        val tileHighlights: MutableMap<Color, List<GridPosition>> = mutableMapOf()
        val untargetedTiles: MutableList<GridPosition> = mutableListOf()
        var index = 0
        targetNpcIds.forEach {
            val position = backend.getNpcPosition(it.first)!!
            if (index == targetIndex) {
                tileHighlights[TARGET_COLOR] = listOf(position)
            } else {
                untargetedTiles.add(position)
            }
            index++
        }
        tileHighlights[NON_TARGET_COLOR] = untargetedTiles
        sEvent.dispatch(TileHighlightUpdateEvent(tileHighlights))
    }

    override fun processSystem() {
        val uiCamera = mCamera.get(sTags.getEntityId(Tags.UI_CAMERA.toString()))
        val sideUiEntities = world.fetch(allOf(SideUiBoxComponent::class, XYComponent::class))

        game.spriteBatch.projectionMatrix = uiCamera.camera.combined
        game.spriteBatch.begin()
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        game.smallFont.color = Color.BLACK
        game.shapeRenderer.color = Color.DARK_GRAY
        sideUiEntities.forEach {
            val cUi = mSideUiBox.get(it)
            if (!cUi.disabled) {
                when (cUi.orientation) {
                    SideUiBoxOrientation.PORTRAIT_LEFT -> drawPortraitLeftUi(cUi, mXy.get(it))
                    SideUiBoxOrientation.PORTRAIT_RIGHT -> drawPortraitRightUi(cUi, mXy.get(it))
                }
            }
        }
        game.spriteBatch.end()
        game.shapeRenderer.end()

        stage.act()
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
    }

    private fun drawPortraitLeftUi(cUiBox: SideUiBoxComponent, cXy: XYComponent) {
        frameBgDrawable.draw(game.spriteBatch, cXy.x, cXy.y, UI_WIDTH, UI_HEIGHT)
        frameDrawable.draw(game.spriteBatch,
                cXy.x - UiConstants.FRAME_LEFT_PADDING,
                cXy.y - UiConstants.FRAME_BOTTOM_PADDING,
                UI_WIDTH + UiConstants.FRAME_LEFT_PADDING + UiConstants.FRAME_RIGHT_PADDING,
                UI_HEIGHT + UiConstants.FRAME_TOP_PADDING + UiConstants.FRAME_BOTTOM_PADDING)
        portraitBgDrawable.draw(game.spriteBatch,
                cXy.x + PADDING,
                cXy.y + PADDING,
                PORTRAIT_WIDTH, PORTRAIT_HEIGHT)
        val onFieldPortrait = cUiBox.onFieldPortrait
        val animationFrame = UnitAnimationFrame(Direction.S, 0)
        when (onFieldPortrait) {
            is EnvObjTilesetMetadata.PccTilesetMetadata -> {
                for (pcc in onFieldPortrait.pccMetadata) {
                    val pccTexture = game.globals.pccManager.getPccFrame(pcc, animationFrame)
                    game.spriteBatch.draw(pccTexture,
                            cXy.x + PADDING + PORTRAIT_WIDTH / 2 - pccTexture.regionWidth / 2,
                            cXy.y + PADDING + PORTRAIT_HEIGHT / 2 - pccTexture.regionHeight / 2)
                }
            }
            is EnvObjTilesetMetadata.AnimatedUnitTilesetMetadata -> {
                val unitTexture = game.globals.animatedTilesetManager.getTilesetFrame(onFieldPortrait.filename, animationFrame)
                game.spriteBatch.draw(unitTexture,
                        cXy.x + PADDING + PORTRAIT_WIDTH / 2 - unitTexture.regionWidth / 2,
                        cXy.y + PADDING + PORTRAIT_HEIGHT / 2 - unitTexture.regionHeight / 2)
            }
        }
        game.smallFont.draw(game.spriteBatch, cUiBox.name,
                cXy.x + PADDING + PORTRAIT_WIDTH + PADDING,
                cXy.y + UI_HEIGHT - PADDING)
        game.shapeRenderer.drawHealthbar(
                cXy.x + PADDING + PORTRAIT_WIDTH + PADDING,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight - PADDING,
                BAR_WIDTH,
                BAR_HEIGHT,
                Color.DARK_GRAY, Color.RED, Color.YELLOW, Color.BLACK,
                cUiBox.hp.toFloat() / cUiBox.hpMax.toFloat())
        game.smallFont.draw(game.spriteBatch, cUiBox.hp.toString(),
                cXy.x + PADDING + PORTRAIT_WIDTH + PADDING + BAR_WIDTH + BAR_TEXT_PADDING,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight)
        game.shapeRenderer.drawHealthbar(
                cXy.x + PADDING + PORTRAIT_WIDTH + PADDING,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight - PADDING - BAR_HEIGHT - BAR_VERTICAL_PADDING,
                BAR_WIDTH,
                BAR_HEIGHT,
                Color.DARK_GRAY, Color(0f, 0.3f, 1f, 1f), Color.CYAN, Color.BLACK,
                cUiBox.tp.toFloat() / cUiBox.tpMax.toFloat())
        game.smallFont.draw(game.spriteBatch, cUiBox.tp.toString(),
                cXy.x + PADDING + PORTRAIT_WIDTH + PADDING + BAR_WIDTH + BAR_TEXT_PADDING,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight - BAR_HEIGHT - BAR_VERTICAL_PADDING)
    }

    private fun drawPortraitRightUi(cUiBox: SideUiBoxComponent, cXy: XYComponent) {
        frameBgDrawable.draw(game.spriteBatch, cXy.x, cXy.y, UI_WIDTH, UI_HEIGHT)
        frameDrawable.draw(game.spriteBatch,
                cXy.x - UiConstants.FRAME_LEFT_PADDING,
                cXy.y - UiConstants.FRAME_BOTTOM_PADDING,
                UI_WIDTH + UiConstants.FRAME_LEFT_PADDING + UiConstants.FRAME_RIGHT_PADDING,
                UI_HEIGHT + UiConstants.FRAME_TOP_PADDING + UiConstants.FRAME_BOTTOM_PADDING)
        portraitBgDrawable.draw(game.spriteBatch,
                cXy.x + SELECTION_DISTANCE + POST_BAR_PADDING + BAR_WIDTH + PADDING,
                cXy.y + PADDING,
                PORTRAIT_WIDTH, PORTRAIT_HEIGHT)
        val onFieldPortrait = cUiBox.onFieldPortrait
        val animationFrame = UnitAnimationFrame(Direction.S, 0)
        when (onFieldPortrait) {
            is EnvObjTilesetMetadata.PccTilesetMetadata -> {
                for (pcc in onFieldPortrait.pccMetadata) {
                    val pccTexture = game.globals.pccManager.getPccFrame(pcc, animationFrame)
                    game.spriteBatch.draw(pccTexture,
                            cXy.x + SELECTION_DISTANCE + POST_BAR_PADDING + BAR_WIDTH + PADDING + PORTRAIT_WIDTH / 2 - pccTexture.regionWidth / 2,
                            cXy.y + PADDING + PORTRAIT_HEIGHT / 2 - pccTexture.regionHeight / 2)
                }
            }
            is EnvObjTilesetMetadata.AnimatedUnitTilesetMetadata -> {
                val unitTexture = game.globals.animatedTilesetManager.getTilesetFrame(onFieldPortrait.filename, animationFrame)
                game.spriteBatch.draw(unitTexture,
                        cXy.x + SELECTION_DISTANCE + POST_BAR_PADDING + BAR_WIDTH + PADDING + PORTRAIT_WIDTH / 2 - unitTexture.regionWidth / 2,
                        cXy.y + PADDING + PORTRAIT_HEIGHT / 2 - unitTexture.regionHeight / 2)
            }
        }
        game.smallFont.draw(game.spriteBatch, cUiBox.name,
                cXy.x + SELECTION_DISTANCE + PADDING,
                cXy.y + UI_HEIGHT - PADDING)
        game.shapeRenderer.drawHealthbar(
                cXy.x + SELECTION_DISTANCE + PADDING,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight - PADDING,
                BAR_WIDTH,
                BAR_HEIGHT,
                Color.DARK_GRAY, Color.RED, Color.YELLOW, Color.BLACK,
                cUiBox.hp.toFloat() / cUiBox.hpMax.toFloat())
        game.smallFont.draw(game.spriteBatch, cUiBox.hp.toString(),
                cXy.x + SELECTION_DISTANCE + PADDING + BAR_TEXT_PADDING + BAR_WIDTH,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight)
        game.shapeRenderer.drawHealthbar(
                cXy.x + SELECTION_DISTANCE + PADDING,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight - PADDING - BAR_HEIGHT - BAR_VERTICAL_PADDING,
                BAR_WIDTH,
                BAR_HEIGHT,
                Color.DARK_GRAY, Color(0f, 0.3f, 1f, 1f), Color.CYAN, Color.BLACK,
                cUiBox.tp.toFloat() / cUiBox.tpMax.toFloat())
        game.smallFont.draw(game.spriteBatch, cUiBox.tp.toString(),
                cXy.x + SELECTION_DISTANCE + PADDING + BAR_TEXT_PADDING + BAR_WIDTH,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight - BAR_HEIGHT - BAR_VERTICAL_PADDING)
    }

    private fun getBackend() = mBackend.get(sTags.getEntityId(Tags.BACKEND.toString())).backend

    private fun changeStateBack() {
        when (stateMachine.currentState) {
            BattleUiState.PLAYER_SELECTED -> stateMachine.changeState(BattleUiState.NOTHING_SELECTED)
            BattleUiState.ENEMY_SELECTED -> stateMachine.changeState(BattleUiState.NOTHING_SELECTED)
            BattleUiState.TARGET_SELECTION -> {
                stateMachine.changeState(BattleUiState.PLAYER_SELECTED)

                val npcEntityId = sNpcId.getNpcEntityId(selectedNpcId!!)!!
                val cXy = mXy.get(npcEntityId)
                sCameraInterpolation.sendCameraToPosition(cXy.toVector2())
            }
            else -> {
            }
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Input.Keys.SHIFT_LEFT -> {
                when (stateMachine.currentState) {
                    BattleUiState.NOTHING_SELECTED -> selectNextPlayer()
                    BattleUiState.PLAYER_SELECTED -> selectNextPlayer()
                    BattleUiState.ENEMY_SELECTED -> selectNextPlayer()
                    BattleUiState.TARGET_SELECTION -> selectNextTarget()
                    else -> {
                    }
                }
            }
            Input.Keys.ESCAPE -> {
                changeStateBack()
                return true
            }
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val cCamera = mCamera.get(sTags.getEntityId(Tags.CAMERA.toString()))
        val pickRay = cCamera.camera.getPickRay(screenX.toFloat(), screenY.toFloat())
        val mouseX = pickRay.origin.x
        val mouseY = pickRay.origin.y

        when (button) {
            Input.Buttons.LEFT -> {
                val sideUiEntities = world.fetch(allOf(SideUiBoxComponent::class, XYComponent::class))
                val bounds = CollisionBounds.CollisionBoundingBox(0f, 0f, UI_WIDTH, UI_HEIGHT)
                sideUiEntities.forEach {
                    val cXy = mXy.get(it)
                    if (CollisionUtils.withinBounds(screenX.toFloat(), game.advConfig.resolution.height - screenY.toFloat(),
                                    cXy.x, cXy.y, bounds)) {
                        val cSideUi = mSideUiBox.get(it)

                        when {
                            stateMachine.isInState(BattleUiState.NOTHING_SELECTED)
                                    || stateMachine.isInState(BattleUiState.PLAYER_SELECTED)
                                    || stateMachine.isInState(BattleUiState.ENEMY_SELECTED) -> {
                                select(cSideUi.npcId)
                            }
                            stateMachine.isInState(BattleUiState.TARGET_SELECTION) -> {
                                selectTarget(cSideUi.npcId)
                            }
                        }
                        return true
                    }
                }

                val npcUnitEntities = fetchNpcUnits()

                var minY = Float.MAX_VALUE
                var minYId: Int? = null
                for (entityId in npcUnitEntities) {
                    val cXy = mXy.get(entityId)
                    val cCollision = mCollision.get(entityId)
                    if (cXy.y < minY && CollisionUtils.withinBounds(mouseX, mouseY, cXy.x, cXy.y, cCollision.bounds)) {
                        minY = cXy.y
                        minYId = entityId
                    }
                }
                if (minYId != null) {
                    val npcId = mNpcId.get(minYId).npcId
                    when {
                        stateMachine.isInState(BattleUiState.NOTHING_SELECTED)
                                || stateMachine.isInState(BattleUiState.PLAYER_SELECTED)
                                || stateMachine.isInState(BattleUiState.ENEMY_SELECTED) -> {
                            select(npcId)
                        }
                        stateMachine.isInState(BattleUiState.TARGET_SELECTION) -> {
                            selectTarget(npcId)
                        }
                    }
                }
            }
            Input.Buttons.RIGHT -> {
                if (stateMachine.isInState(BattleUiState.PLAYER_SELECTED)) {
                    val destination = GridUtils.localToGridPosition(mouseX, mouseY, game.advConfig.resolution.tileSize.toFloat())
                    if (mapGraph!!.canMoveTo(destination)) {
                        val moveCommand = MoveCommand(selectedNpcId!!, mapGraph!!.getPath(destination).toList())
                        executeCommand(moveCommand)
                    }
                } else {
                    changeStateBack()
                }
            }
        }

        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        val cCamera = mCamera.get(sTags.getEntityId(Tags.CAMERA.toString()))
        val pickRay = cCamera.camera.getPickRay(screenX.toFloat(), screenY.toFloat())
        val mouseX = pickRay.origin.x
        val mouseY = pickRay.origin.y

        when (stateMachine.currentState) {
            BattleUiState.PLAYER_SELECTED -> {
                val theMapGraph = mapGraph
                if (theMapGraph != null) {
                    val tileSize = game.advConfig.resolution.tileSize.toFloat()

                    val destination = GridUtils.localToGridPosition(mouseX, mouseY, tileSize)
                    if (destination != hoverDestination && theMapGraph.canMoveTo(destination)) {
                        hoverDestination = destination

                        val path = theMapGraph.getPath(destination).map {
                            GridUtils.gridPositionToLocalOffset(it, tileSize,
                                    tileSize / 2f - movePreviewDrawableSize / 2,
                                    tileSize / 2f - movePreviewDrawableSize / 2)
                        }
                        val start = GridUtils.gridPositionToLocalOffset(theMapGraph.start, tileSize,
                                tileSize / 2f - movePreviewDrawableSize / 2,
                                tileSize / 2f - movePreviewDrawableSize / 2)

                        val previewPath = path.toMutableList()
                        previewPath.add(0, start)
                        createMovePreview(previewPath.toList())
                    }
                }
            }
            else -> {
            }
        }
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }

    enum class BattleUiState : State<BattleUiSystem> {
        NOTHING_SELECTED() {
            override fun enter(uiSystem: BattleUiSystem) {
                uiSystem.clearLeftUiBoxes()
                uiSystem.deselectRightUiBoxes()
                uiSystem.stage.clear()
            }
        },
        ENEMY_SELECTED() {
            override fun enter(uiSystem: BattleUiSystem) {
                uiSystem.clearLeftUiBoxes()
                uiSystem.deselectRightUiBoxes()
                uiSystem.stage.clear()
                uiSystem.createLeftUiBox(uiSystem.selectedNpcId!!, 0)
            }
        },
        PLAYER_SELECTED() {
            override fun enter(uiSystem: BattleUiSystem) {
                uiSystem.commandPreviewTable.remove()
                uiSystem.clearLeftUiBoxes()
                uiSystem.deselectRightUiBoxes()
                uiSystem.selectRightUiBox(uiSystem.selectedNpcId!!)
                if (uiSystem.primaryActionMenu.lockSelection) {
                    uiSystem.primaryActionMenu.lockSelection = false
                } else {
                    if (uiSystem.getBackend().getNpcAp(uiSystem.selectedNpcId!!) > 0) {
                        uiSystem.activatePrimaryActionMenu()
                        uiSystem.stage.addActor(uiSystem.primaryActionMenu)
                    }
                }
                uiSystem.showMoveTileHighlights(uiSystem.selectedNpcId!!)
            }

            override fun exit(uiSystem: BattleUiSystem) {
                uiSystem.clearTileHighlights()
                uiSystem.clearMovementPreview()
            }
        },
        TARGET_SELECTION() {
            override fun enter(uiSystem: BattleUiSystem) {
                uiSystem.primaryActionMenu.lockSelection = true
                if (uiSystem.targetNpcIds.isNotEmpty()) {
                    var index = 0
                    uiSystem.targetNpcIds.forEach {
                        uiSystem.createLeftUiBox(it.first, index, index == 0)
                        index++
                    }
                    uiSystem.selectTarget(uiSystem.targetNpcIds[0].first)
                }
            }
        },
        ANIMATING() {
            override fun enter(uiSystem: BattleUiSystem) {
                uiSystem.clearTileHighlights()
                uiSystem.clearMovementPreview()
                uiSystem.clearLeftUiBoxes()
                uiSystem.stage.clear()
            }
        };

        override fun exit(uiSystem: BattleUiSystem) {
        }

        override fun onMessage(uiSystem: BattleUiSystem, telegram: Telegram): Boolean {
            return false
        }

        override fun update(uiSystem: BattleUiSystem) {
        }
    }
}
