package com.pipai.adv.artemis.system.ui

import com.artemis.BaseSystem
import com.artemis.managers.TagManager
import com.badlogic.gdx.graphics.Color
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.components.*
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.gui.UiConstants
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
        const val POST_BAR_PADDING = 64f
        const val UI_WIDTH = PADDING + PORTRAIT_WIDTH + PADDING + BAR_WIDTH + POST_BAR_PADDING
        const val UI_HEIGHT = PADDING + PORTRAIT_HEIGHT + PADDING
    }

    override fun processSystem() {
        val uiCamera = mCamera.get(sTags.getEntityId(Tags.UI_CAMERA.toString()))

        val sideUiEntities = world.fetch(allOf(SideUiBoxComponent::class, XYComponent::class))

        game.spriteBatch.projectionMatrix = uiCamera.camera.combined
        game.spriteBatch.begin()
        game.smallFont.color = Color.BLACK
        sideUiEntities.forEach {
            val cUi = mSideUiBox.get(it)
            if (!cUi.disabled) {
                when (cUi.orientation) {
                    SideUiBoxOrientation.RIGHT -> drawRightSideUi(cUi, mXy.get(it))
                    SideUiBoxOrientation.LEFT -> drawLeftSideUi(cUi, mXy.get(it))
                }
            }
        }
        game.spriteBatch.end()
    }

    private fun drawRightSideUi(cUiBox: SideUiBoxComponent, cXy: XYComponent) {
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
        game.smallFont.draw(game.spriteBatch, cUiBox.name,
                cXy.x + PADDING + PORTRAIT_WIDTH + PADDING,
                cXy.y + UI_HEIGHT - PADDING)
    }

    private fun drawLeftSideUi(cUiBox: SideUiBoxComponent, cXy: XYComponent) {
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
        game.smallFont.draw(game.spriteBatch, cUiBox.name,
                cXy.x + PADDING,
                cXy.y + UI_HEIGHT - PADDING)
    }

    private fun getBackend() = mBackend.get(sTags.getEntityId(Tags.BACKEND.toString())).backend

}
