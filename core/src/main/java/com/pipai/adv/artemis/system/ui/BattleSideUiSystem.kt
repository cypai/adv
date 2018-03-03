package com.pipai.adv.artemis.system.ui

import com.artemis.BaseSystem
import com.artemis.managers.TagManager
import com.badlogic.gdx.InputProcessor
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
import com.pipai.adv.artemis.events.NonPlayerUnitSelectedEvent
import com.pipai.adv.artemis.events.NonPlayerUnitUnselectedEvent
import com.pipai.adv.artemis.events.PlayerUnitSelectedEvent
import com.pipai.adv.artemis.events.PlayerUnitUnselectedEvent
import com.pipai.adv.artemis.screens.BattleMapScreenInit
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.system.input.SelectedUnitSystem
import com.pipai.adv.artemis.system.ui.menu.MenuItem
import com.pipai.adv.artemis.system.ui.menu.StringMenuItem
import com.pipai.adv.artemis.system.ui.menu.TargetMenuCommandItem
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata
import com.pipai.adv.backend.battle.engine.commands.NormalAttackCommandFactory
import com.pipai.adv.backend.battle.utils.BattleUtils
import com.pipai.adv.gui.UiConstants
import com.pipai.adv.tiles.UnitAnimationFrame
import com.pipai.adv.utils.*
import net.mostlyoriginal.api.event.common.Subscribe

class BattleSideUiSystem(private val game: AdvGame) : BaseSystem(), InputProcessor {

    private val mSideUiBox by mapper<SideUiBoxComponent>()
    private val mXy by mapper<XYComponent>()
    private val mActor by mapper<ActorComponent>()

    private val mBackend by mapper<BattleBackendComponent>()
    private val mCamera by mapper<OrthographicCameraComponent>()

    private val mPath by mapper<PathInterpolationComponent>()

    private val sTags by system<TagManager>()
    private val sSelectedUnit by system<SelectedUnitSystem>()

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
    private var primaryActionMenuActive = false

    companion object {
        const val PORTRAIT_WIDTH = 80f
        const val PORTRAIT_HEIGHT = 80f
        const val PADDING = 8f
        const val BAR_WIDTH = 80f
        const val BAR_HEIGHT = 6f
        const val BAR_VERTICAL_PADDING = 12f
        const val BAR_TEXT_PADDING = 8f
        const val POST_BAR_PADDING = 64f
        const val SELECTION_DISTANCE = 8f
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
                if (primaryActionMenuActive) {
                    System.out.println(primaryActionMenu.getSelected())
                }
            }
        })

        primaryActionMenuEntityId = world.create()
        val cPrimaryActionMenu = mActor.create(primaryActionMenuEntityId)
        cPrimaryActionMenu.actor = primaryActionMenu
    }

    private fun setPrimaryActionMenuItems(npcId: Int) {
        val backend = getBackend()
        primaryActionMenu.setItems(listOf(
                TargetMenuCommandItem("Attack", null, NormalAttackCommandFactory(backend)),
                StringMenuItem("Skill", null, ""),
                StringMenuItem("Reload", null, ""),
                StringMenuItem("Item", null, ""),
                StringMenuItem("Defend", null, ""),
                StringMenuItem("Wait", null, ""),
                StringMenuItem("Run", null, "")))

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

    @Subscribe
    fun playerUnitSelectedListener(event: PlayerUnitSelectedEvent) {
        setPrimaryActionMenuItems(event.npcId)
        val uiEntityId = world.fetch(allOf(SideUiBoxComponent::class, XYComponent::class))
                .find { mSideUiBox.get(it).npcId == event.npcId }
        if (uiEntityId != null) {
            val cXy = mXy.get(uiEntityId)
            val cPath = mPath.create(uiEntityId)
            cPath.interpolation = Interpolation.linear
            cPath.endpoints.clear()
            cPath.endpoints.add(cXy.toVector2())
            cPath.endpoints.add(Vector2(cXy.x - SELECTION_DISTANCE, cXy.y))
            cPath.maxT = SELECTION_TIME
        }
        val cPrimaryMenuPath = mPath.create(primaryActionMenuEntityId)
        cPrimaryMenuPath.interpolation = Interpolation.linear
        cPrimaryMenuPath.endpoints.clear()
        cPrimaryMenuPath.endpoints.add(Vector2(-ACTION_UI_WIDTH, primaryActionMenu.y))
        cPrimaryMenuPath.endpoints.add(Vector2(PADDING, primaryActionMenu.y))
        cPrimaryMenuPath.maxT = SELECTION_TIME
        stage.addActor(primaryActionMenu)
    }

    @Subscribe
    fun playerUnitUnselectedListener(event: PlayerUnitUnselectedEvent) {
        val uiEntityId = world.fetch(allOf(SideUiBoxComponent::class, XYComponent::class))
                .find { mSideUiBox.get(it).npcId == event.npcId }
        if (uiEntityId != null) {
            val cXy = mXy.get(uiEntityId)
            val cPath = mPath.create(uiEntityId)
            cPath.interpolation = Interpolation.linear
            cPath.endpoints.clear()
            cPath.endpoints.add(cXy.toVector2())
            cPath.endpoints.add(Vector2(cXy.x + SELECTION_DISTANCE, cXy.y))
            cPath.maxT = SELECTION_TIME
        }
        stage.clear()
    }

    @Subscribe
    fun nonPlayerUnitSelectedListener(event: NonPlayerUnitSelectedEvent) {
        val entityId = world.create()
        val cUi = mSideUiBox.create(entityId)
        cUi.setToNpc(event.npcId, getBackend())
        cUi.orientation = SideUiBoxOrientation.PORTRAIT_RIGHT
        val cXy = mXy.create(entityId)
        cXy.x = -UI_WIDTH
        cXy.y = game.advConfig.resolution.height - BattleSideUiSystem.UI_HEIGHT - BattleMapScreenInit.UI_VERTICAL_PADDING
        val cPath = mPath.create(entityId)
        cPath.interpolation = Interpolation.linear
        cPath.endpoints.clear()
        cPath.endpoints.add(cXy.toVector2())
        cPath.endpoints.add(Vector2(0f, cXy.y))
        cPath.maxT = SELECTION_TIME
    }

    @Subscribe
    fun nonPlayerUnitUnselectedListener(event: NonPlayerUnitUnselectedEvent) {
        val uiEntityId = world.fetch(allOf(SideUiBoxComponent::class, XYComponent::class))
                .find { mSideUiBox.get(it).npcId == event.npcId }
        if (uiEntityId != null) {
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
                sSelectedUnit.select(cSideUi.npcId)
                return true
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
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }

}
