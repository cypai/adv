package com.pipai.adv.artemis.system.ui

import com.artemis.BaseSystem
import com.artemis.managers.TagManager
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ai.fsm.StackStateMachine
import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageList
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.components.*
import com.pipai.adv.artemis.events.MovementTileUpdateEvent
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
import com.pipai.adv.backend.battle.utils.BattleUtils
import com.pipai.adv.gui.UiConstants
import com.pipai.adv.tiles.UnitAnimationFrame
import com.pipai.adv.utils.*
import net.mostlyoriginal.api.event.common.EventSystem

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

    private val stateMachine = StackStateMachine<BattleUiSystem, BattleUiState>(this, BattleUiState.NOTHING_SELECTED)

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

    private val movePreviewDrawable = game.skin.newDrawable("white", Color(0.3f, 0.3f, 0.8f, 0.7f))
    private val movePreviewDrawableSize = 6f

    var selectedNpcId: Int? = null
        private set

    private val targetNpcIds: MutableList<Pair<Int, TargetCommand>> = mutableListOf()

    private var mapGraph: MapGraph? = null
    private var hoverDestination: GridPosition? = null
    private var movePreviewEntityId: Int? = null

    companion object {
        const val PORTRAIT_WIDTH = 80f
        const val PORTRAIT_HEIGHT = 80f
        const val PADDING = 8f
        const val BAR_WIDTH = 80f
        const val BAR_HEIGHT = 6f
        const val BAR_VERTICAL_PADDING = 12f
        const val BAR_TEXT_PADDING = 8f
        const val POST_BAR_PADDING = 64f
        const val SELECTION_DISTANCE = 16f
        const val UI_WIDTH = PADDING + PORTRAIT_WIDTH + PADDING + BAR_WIDTH + POST_BAR_PADDING + SELECTION_DISTANCE
        const val UI_HEIGHT = PADDING + PORTRAIT_HEIGHT + PADDING

        const val SELECTION_TIME = 10

        const val ACTION_UI_WIDTH = 160f
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
                handleMenuSelect(primaryActionMenu.getSelected())
            }
        })

        primaryActionMenuEntityId = world.create()
        val cPrimaryActionMenu = mActor.create(primaryActionMenuEntityId)
        cPrimaryActionMenu.actor = primaryActionMenu
    }

    private fun executeCommand(command: BattleCommand) {
        val backend = getBackend()
        val executionStatus = backend.canBeExecuted(command)
        if (executionStatus.executable) {
            val events = backend.execute(command)
            sBattleAnimation.processBattleEvents(events)
        } else {
            logger.debug("Unable to move: ${executionStatus.reason}")
        }
    }

    private fun showMovementTiles(npcId: Int) {
        val factory = MoveCommandFactory(getBackend())
        mapGraph = factory.getMapGraph(npcId)
        sEvent.dispatch(MovementTileUpdateEvent(mapGraph))
    }

    private fun clearMovementTiles() {
        sEvent.dispatch(MovementTileUpdateEvent(null))
        movePreviewEntityId?.let { world.delete(it) }
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
        cPath.maxT = 5
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
                cPath.endpoints.add(Vector2(game.advConfig.resolution.width - UI_WIDTH + SELECTION_DISTANCE, cXy.y))
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
        primaryActionMenu.setItems(listOf(
                TargetMenuCommandItem("Attack", null, NormalAttackCommandFactory(backend)),
                StringMenuItem("Skill", null, ""),
                StringMenuItem("Reload", null, ""),
                StringMenuItem("Item", null, ""),
                StringMenuItem("Defend", null, ""),
                StringMenuItem("Wait", null, ""),
                StringMenuItem("Run", null, "")))

        val npcId = selectedNpcId!!
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
                setTargets(menuItem.factory.generate(selectedNpcId!!))
            }
        }
    }

    private fun setTargets(targetCommands: List<TargetCommand>) {
        targetNpcIds.clear()
        targetNpcIds.addAll(targetCommands.map { Pair(it.targetId, it) })
        var index = 0
        targetCommands.forEach {
            createLeftUiBox(it.targetId, index)
            index++
        }
    }

    private fun createLeftUiBox(npcId: Int, index: Int) {
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
        cPath.endpoints.add(Vector2(0f, cXy.y))
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

    private fun selectNext() {
        val playerUnits = fetchPlayerUnits()
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
                    SideUiBoxOrientation.PORTRAIT_LEFT -> drawLeftSideUi(cUi, mXy.get(it))
                    SideUiBoxOrientation.PORTRAIT_RIGHT -> drawRightSideUi(cUi, mXy.get(it))
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

    private fun drawLeftSideUi(cUiBox: SideUiBoxComponent, cXy: XYComponent) {
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
        game.shapeRenderer.rect(
                cXy.x + PADDING + PORTRAIT_WIDTH + PADDING - 1,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight - PADDING - 1,
                BAR_WIDTH + 2,
                BAR_HEIGHT + 2)
        game.shapeRenderer.rect(
                cXy.x + PADDING + PORTRAIT_WIDTH + PADDING,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight - PADDING,
                BAR_WIDTH,
                BAR_HEIGHT,
                Color.RED, Color.YELLOW, Color.YELLOW, Color.RED)
        game.shapeRenderer.rect(
                cXy.x + PADDING + PORTRAIT_WIDTH + PADDING + cUiBox.hp.toFloat() / cUiBox.hpMax.toFloat() * BAR_WIDTH,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight - PADDING,
                BAR_WIDTH - cUiBox.hp.toFloat() / cUiBox.hpMax.toFloat() * BAR_WIDTH,
                BAR_HEIGHT,
                Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK)
        game.smallFont.draw(game.spriteBatch, cUiBox.hp.toString(),
                cXy.x + PADDING + PORTRAIT_WIDTH + PADDING + BAR_WIDTH + BAR_TEXT_PADDING,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight)
        game.shapeRenderer.rect(
                cXy.x + PADDING + PORTRAIT_WIDTH + PADDING - 1,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight - PADDING - 1 - BAR_HEIGHT - BAR_VERTICAL_PADDING,
                BAR_WIDTH + 2,
                BAR_HEIGHT + 2)
        game.shapeRenderer.rect(
                cXy.x + PADDING + PORTRAIT_WIDTH + PADDING,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight - PADDING - BAR_HEIGHT - BAR_VERTICAL_PADDING,
                BAR_WIDTH,
                BAR_HEIGHT,
                Color(0f, 0.3f, 1f, 1f), Color.CYAN, Color.CYAN, Color(0f, 0.3f, 1f, 1f))
        game.shapeRenderer.rect(
                cXy.x + PADDING + PORTRAIT_WIDTH + PADDING + cUiBox.tp.toFloat() / cUiBox.tpMax.toFloat() * BAR_WIDTH,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight - PADDING - BAR_HEIGHT - BAR_VERTICAL_PADDING,
                BAR_WIDTH - cUiBox.tp.toFloat() / cUiBox.tpMax.toFloat() * BAR_WIDTH,
                BAR_HEIGHT,
                Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK)
        game.smallFont.draw(game.spriteBatch, cUiBox.tp.toString(),
                cXy.x + PADDING + PORTRAIT_WIDTH + PADDING + BAR_WIDTH + BAR_TEXT_PADDING,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight - BAR_HEIGHT - BAR_VERTICAL_PADDING)
    }

    private fun drawRightSideUi(cUiBox: SideUiBoxComponent, cXy: XYComponent) {
        frameBgDrawable.draw(game.spriteBatch, cXy.x, cXy.y, UI_WIDTH, UI_HEIGHT)
        frameDrawable.draw(game.spriteBatch,
                cXy.x - UiConstants.FRAME_LEFT_PADDING,
                cXy.y - UiConstants.FRAME_BOTTOM_PADDING,
                UI_WIDTH + UiConstants.FRAME_LEFT_PADDING + UiConstants.FRAME_RIGHT_PADDING,
                UI_HEIGHT + UiConstants.FRAME_TOP_PADDING + UiConstants.FRAME_BOTTOM_PADDING)
        portraitBgDrawable.draw(game.spriteBatch,
                cXy.x + POST_BAR_PADDING + BAR_WIDTH + PADDING,
                cXy.y + PADDING,
                PORTRAIT_WIDTH, PORTRAIT_HEIGHT)
        val onFieldPortrait = cUiBox.onFieldPortrait
        val animationFrame = UnitAnimationFrame(Direction.S, 0)
        when (onFieldPortrait) {
            is EnvObjTilesetMetadata.PccTilesetMetadata -> {
                for (pcc in onFieldPortrait.pccMetadata) {
                    val pccTexture = game.globals.pccManager.getPccFrame(pcc, animationFrame)
                    game.spriteBatch.draw(pccTexture,
                            cXy.x + POST_BAR_PADDING + BAR_WIDTH + PADDING + PORTRAIT_WIDTH / 2 - pccTexture.regionWidth / 2,
                            cXy.y + PADDING + PORTRAIT_HEIGHT / 2 - pccTexture.regionHeight / 2)
                }
            }
            is EnvObjTilesetMetadata.AnimatedUnitTilesetMetadata -> {
                val unitTexture = game.globals.animatedTilesetManager.getTilesetFrame(onFieldPortrait.filename, animationFrame)
                game.spriteBatch.draw(unitTexture,
                        cXy.x + POST_BAR_PADDING + BAR_WIDTH + PADDING + PORTRAIT_WIDTH / 2 - unitTexture.regionWidth / 2,
                        cXy.y + PADDING + PORTRAIT_HEIGHT / 2 - unitTexture.regionHeight / 2)
            }
        }
        game.smallFont.draw(game.spriteBatch, cUiBox.name,
                cXy.x + PADDING,
                cXy.y + UI_HEIGHT - PADDING)
        game.shapeRenderer.rect(
                cXy.x + PADDING - 1,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight - PADDING - 1,
                BAR_WIDTH + 2,
                BAR_HEIGHT + 2)
        game.shapeRenderer.rect(
                cXy.x + PADDING,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight - PADDING,
                BAR_WIDTH,
                BAR_HEIGHT,
                Color.RED, Color.YELLOW, Color.YELLOW, Color.RED)
        game.shapeRenderer.rect(
                cXy.x + PADDING + cUiBox.hp.toFloat() / cUiBox.hpMax.toFloat() * BAR_WIDTH,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight - PADDING,
                BAR_WIDTH - cUiBox.hp.toFloat() / cUiBox.hpMax.toFloat() * BAR_WIDTH,
                BAR_HEIGHT,
                Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK)
        game.smallFont.draw(game.spriteBatch, cUiBox.hp.toString(),
                cXy.x + PADDING + BAR_TEXT_PADDING + BAR_WIDTH,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight)
        game.shapeRenderer.rect(
                cXy.x + PADDING - 1,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight - PADDING - 1 - BAR_HEIGHT - BAR_VERTICAL_PADDING,
                BAR_WIDTH + 2,
                BAR_HEIGHT + 2)
        game.shapeRenderer.rect(
                cXy.x + PADDING,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight - PADDING - BAR_HEIGHT - BAR_VERTICAL_PADDING,
                BAR_WIDTH,
                BAR_HEIGHT,
                Color(0f, 0.3f, 1f, 1f), Color.CYAN, Color.CYAN, Color(0f, 0.3f, 1f, 1f))
        game.shapeRenderer.rect(
                cXy.x + PADDING + cUiBox.tp.toFloat() / cUiBox.tpMax.toFloat() * BAR_WIDTH,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight - PADDING - BAR_HEIGHT - BAR_VERTICAL_PADDING,
                BAR_WIDTH - cUiBox.tp.toFloat() / cUiBox.tpMax.toFloat() * BAR_WIDTH,
                BAR_HEIGHT,
                Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK)
        game.smallFont.draw(game.spriteBatch, cUiBox.tp.toString(),
                cXy.x + PADDING + BAR_TEXT_PADDING + BAR_WIDTH,
                cXy.y + UI_HEIGHT - PADDING - game.smallFont.lineHeight - BAR_HEIGHT - BAR_VERTICAL_PADDING)
    }

    private fun getBackend() = mBackend.get(sTags.getEntityId(Tags.BACKEND.toString())).backend

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.SHIFT_LEFT) {
            selectNext()
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
        val sideUiEntities = world.fetch(allOf(SideUiBoxComponent::class, XYComponent::class))
        val bounds = CollisionBounds.CollisionBoundingBox(0f, 0f, UI_WIDTH, UI_HEIGHT)
        sideUiEntities.forEach {
            val cXy = mXy.get(it)
            if (CollisionUtils.withinBounds(screenX.toFloat(), game.advConfig.resolution.height - screenY.toFloat(),
                            cXy.x, cXy.y, bounds)) {
                val cSideUi = mSideUiBox.get(it)
                select(cSideUi.npcId)
                return true
            }
        }

        val cCamera = mCamera.get(sTags.getEntityId(Tags.CAMERA.toString()))
        val pickRay = cCamera.camera.getPickRay(screenX.toFloat(), screenY.toFloat())
        val mouseX = pickRay.origin.x
        val mouseY = pickRay.origin.y

        when (button) {
            Input.Buttons.LEFT -> {
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
                    select(npcId)
                }
            }
            Input.Buttons.RIGHT -> {
                if (stateMachine.isInState(BattleUiState.PLAYER_SELECTED)) {
                    val destination = GridUtils.localToGridPosition(mouseX, mouseY, game.advConfig.resolution.tileSize.toFloat())
                    if (mapGraph!!.canMoveTo(destination)) {
                        val moveCommand = MoveCommand(selectedNpcId!!, mapGraph!!.getPath(destination))
                        executeCommand(moveCommand)
                    }
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
                uiSystem.clearLeftUiBoxes()
                uiSystem.deselectRightUiBoxes()
                uiSystem.selectRightUiBox(uiSystem.selectedNpcId!!)
                uiSystem.activatePrimaryActionMenu()
                uiSystem.stage.addActor(uiSystem.primaryActionMenu)
                uiSystem.showMovementTiles(uiSystem.selectedNpcId!!)
            }

            override fun exit(uiSystem: BattleUiSystem) {
                uiSystem.clearMovementTiles()
            }
        },
        TARGET_SELECTION() {
        };

        override fun enter(uiSystem: BattleUiSystem) {
        }

        override fun exit(uiSystem: BattleUiSystem) {
        }

        override fun onMessage(uiSystem: BattleUiSystem, telegram: Telegram): Boolean {
            return false
        }

        override fun update(uiSystem: BattleUiSystem) {
        }
    }
}