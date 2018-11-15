package com.pipai.adv.artemis.system.input

import com.artemis.managers.TagManager
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.pipai.adv.AdvConfig
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.components.*
import com.pipai.adv.artemis.components.Interaction.*
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.artemis.system.ui.MainTextboxUiSystem
import com.pipai.adv.utils.*

class InteractionInputSystem(private val game: AdvGame,
                             private val currentScreen: Screen,
                             private val config: AdvConfig) : NoProcessingSystem(), InputProcessor {

    private val logger = getLogger()

    private val DISTANCE2_THRESHOLD = config.resolution.tileSize * config.resolution.tileSize

    private val mXy by mapper<XYComponent>()
    private val mInteraction by mapper<InteractionComponent>()
    private val mEnvObjTile by mapper<EnvObjTileComponent>()

    private val mPartialText by mapper<PartialTextComponent>()
    private val mMultipleText by mapper<MultipleTextComponent>()

    private val sTags by system<TagManager>()
    private val sMainTextbox by system<MainTextboxUiSystem>()

    private val currentInteractions: MutableList<Interaction> = mutableListOf()
    private var state: State = State.READY

    override fun keyDown(keycode: Int): Boolean {
        if (isEnabled) {
            if (keycode == Keys.Z) {
                when (state) {
                    State.READY -> {
                        checkInteractions()
                    }
                    State.TEXT -> {
                        if (sMainTextbox.isDone()) {
                            finishTextInteraction()
                        } else {
                            sMainTextbox.showFullText()
                        }
                    }
                }
            }
        }
        return false
    }

    private fun checkInteractions() {
        val charId = sTags.getEntityId(Tags.CONTROLLABLE_CHARACTER.toString())
        val cXy = mXy.get(charId)
        val facingDirection = mEnvObjTile.get(charId).direction

        val entities = world.fetch(allOf(XYComponent::class, InteractionComponent::class))

        for (entity in entities) {
            val cEntityXy = mXy.get(entity)

            val relativeDirection = DirectionUtils.directionFor(cXy.x, cXy.y, cEntityXy.x, cEntityXy.y)
            val isFacing = DirectionUtils.isInGeneralDirection(facingDirection, relativeDirection)

            if (isFacing && MathUtils.distance2(cXy.x, cXy.y, cEntityXy.x, cEntityXy.y) < DISTANCE2_THRESHOLD) {
                startInteractions(mInteraction.get(entity).interactionList)
                break
            }
        }
    }

    fun startInteractions(interactions: List<Interaction>) {
        currentInteractions.addAll(interactions)
        handleInteraction(interactions.first())
    }

    private fun nextInteraction() {
        currentInteractions.removeAt(0)
        if (currentInteractions.size > 0) {
            handleInteraction(currentInteractions.first())
        } else {
            state = State.READY
        }
    }

    private fun handleInteraction(interaction: Interaction) {
        when (interaction) {
            is TextInteraction -> {
                handleTextInteraction(interaction)
            }
            is ScreenChangeInteraction -> {
                game.screen = interaction.screenGenerator()
            }
            is CallbackInteraction -> {
                interaction.callback.invoke()
            }
        }
    }

    private fun handleTextInteraction(interaction: TextInteraction) {
        disableSystems()
        sMainTextbox.setToText(interaction.text)
        sMainTextbox.isEnabled = true
        state = State.TEXT
    }

    private fun finishTextInteraction() {
        enableSystems()
        sMainTextbox.isEnabled = false
        nextInteraction()
    }

    private fun disableSystems() {
        world.getSystemSafe(CharacterMovementInputSystem::class.java)?.disable()
    }

    private fun enableSystems() {
        world.getSystemSafe(CharacterMovementInputSystem::class.java)?.enable()
    }

    override fun keyUp(keycode: Int): Boolean = false

    override fun keyTyped(character: Char) = false

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) = false

    override fun mouseMoved(screenX: Int, screenY: Int) = false

    override fun scrolled(amount: Int): Boolean = false

    enum class State {
        READY, TEXT
    }
}
