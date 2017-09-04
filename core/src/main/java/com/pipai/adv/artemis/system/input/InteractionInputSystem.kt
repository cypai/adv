package com.pipai.adv.artemis.system.input

import com.artemis.managers.TagManager
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.components.EnvObjTileComponent
import com.pipai.adv.artemis.components.MainTextboxFlagComponent
import com.pipai.adv.artemis.components.MultipleTextComponent
import com.pipai.adv.artemis.components.PartialTextComponent
import com.pipai.adv.artemis.components.TextInteractionComponent
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.utils.DirectionUtils
import com.pipai.adv.utils.MathUtils
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.fetch
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system
import com.pipai.utils.getLogger

class InteractionInputSystem(private val config: AdvConfig) : NoProcessingSystem(), InputProcessor {

    private val logger = getLogger()

    private val DISTANCE2_THRESHOLD = config.resolution.tileSize * config.resolution.tileSize

    private val mXy by mapper<XYComponent>()
    private val mText by mapper<TextInteractionComponent>()
    private val mEnvObjTile by mapper<EnvObjTileComponent>()

    private val mPartialText by mapper<PartialTextComponent>()
    private val mMultipleText by mapper<MultipleTextComponent>()
    private val mMainTextboxFlag by mapper<MainTextboxFlagComponent>()

    private val sTags by system<TagManager>()

    override fun keyDown(keycode: Int): Boolean {
        if (isEnabled && keycode == Keys.Z) {
            checkInteractions()
        }
        return false
    }

    private fun checkInteractions() {
        val charId = sTags.getEntityId(Tags.CONTROLLABLE_CHARACTER.toString())
        val cXy = mXy.get(charId)
        val facingDirection = mEnvObjTile.get(charId).direction

        val entities = world.fetch(allOf(XYComponent::class, TextInteractionComponent::class))

        for (entity in entities) {
            val cEntityXy = mXy.get(entity)

            val relativeDirection = DirectionUtils.directionFor(cXy.x, cXy.y, cEntityXy.x, cEntityXy.y)
            val isFacing = DirectionUtils.isInGeneralDirection(facingDirection, relativeDirection)

            if (isFacing && MathUtils.distance2(cXy.x, cXy.y, cEntityXy.x, cEntityXy.y) < DISTANCE2_THRESHOLD) {
                logger.info("Started")
                startMainTextbox(mText.get(entity).textList)
                break
            }
        }
    }

    private fun startMainTextbox(textList: List<String>) {
        if (textList.size > 0) {
            val id = world.create()
            val cPartialText = mPartialText.create(id)
            cPartialText.setToText(textList.get(0))
            val cMultipleText = mMultipleText.create(id)
            cMultipleText.textList.addAll(textList)
            cMultipleText.textList.removeAt(0)
            mMainTextboxFlag.create(id)
        }
    }

    override fun keyUp(keycode: Int): Boolean = false

    override fun keyTyped(character: Char) = false

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) = false

    override fun mouseMoved(screenX: Int, screenY: Int) = false

    override fun scrolled(amount: Int): Boolean = false
}
