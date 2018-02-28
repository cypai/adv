package com.pipai.adv.artemis.system.ui

import com.artemis.BaseSystem
import com.artemis.managers.TagManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.components.*
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.backend.battle.domain.EnvObjTilesetMetadata
import com.pipai.adv.gui.UiConstants
import com.pipai.adv.tiles.UnitAnimationFrame
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.fetch
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system

class BattleSideUiSystem(private val game: AdvGame) : BaseSystem() {

    private val mSideUiBox by mapper<SideUiBoxComponent>()
    private val mXy by mapper<XYComponent>()

    private val mBackend by mapper<BattleBackendComponent>()
    private val mCamera by mapper<OrthographicCameraComponent>()

    private val sTags by system<TagManager>()

    private val frameDrawable = game.skin.getDrawable("frame")
    private val frameBgDrawable = game.skin.getDrawable("bg")
    private val portraitBgDrawable = game.skin.newDrawable("white", Color.DARK_GRAY)

    companion object {
        const val PORTRAIT_WIDTH = 80f
        const val PORTRAIT_HEIGHT = 80f
        const val PADDING = 8f
        const val BAR_WIDTH = 80f
        const val BAR_HEIGHT = 6f
        const val BAR_VERTICAL_PADDING = 12f
        const val BAR_TEXT_PADDING = 8f
        const val POST_BAR_PADDING = 64f
        const val UI_WIDTH = PADDING + PORTRAIT_WIDTH + PADDING + BAR_WIDTH + POST_BAR_PADDING
        const val UI_HEIGHT = PADDING + PORTRAIT_HEIGHT + PADDING
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

}
