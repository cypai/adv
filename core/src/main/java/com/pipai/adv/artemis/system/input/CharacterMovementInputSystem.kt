package com.pipai.adv.artemis.system.input

import com.artemis.BaseSystem
import com.artemis.managers.TagManager
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.components.AnimationFramesComponent
import com.pipai.adv.artemis.components.EnvObjTileComponent
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.artemis.events.CutsceneEvent
import com.pipai.adv.artemis.screens.Tags
import com.pipai.adv.artemis.system.misc.PausableSystem
import com.pipai.adv.backend.battle.domain.Direction
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.Subscribe

class CharacterMovementInputSystem(private val config: AdvConfig) : BaseSystem(), InputProcessor, PausableSystem {

    private val speed = 3

    private val mXy by mapper<XYComponent>()
    private val mEnvObjTile by mapper<EnvObjTileComponent>()
    private val mAnimationFrames by mapper<AnimationFramesComponent>()

    private val sTags by system<TagManager>()

    private val heldKeys: HeldKeys = HeldKeys()
    private var keyDownDirection: MutableList<Direction> = mutableListOf()

    @Subscribe
    fun handleCutsceneEvent(event: CutsceneEvent) {
        if (event.start) {
            disable()
        } else {
            enable()
        }
    }

    fun enable() {
        isEnabled = true

        val charId = sTags.getEntityId(Tags.CONTROLLABLE_CHARACTER.toString())
        val cAnimationFrames = mAnimationFrames.get(charId)
        cAnimationFrames.freeze = false
    }

    fun disable() {
        isEnabled = false

        val charId = sTags.getEntityId(Tags.CONTROLLABLE_CHARACTER.toString())
        val cAnimationFrames = mAnimationFrames.get(charId)
        cAnimationFrames.tMax = 0
        cAnimationFrames.frame = 0
        cAnimationFrames.freeze = true
    }

    override fun processSystem() {
        val charId = sTags.getEntityId(Tags.CONTROLLABLE_CHARACTER.toString())
        translateCharacter(charId)
    }

    private fun translateCharacter(charId: Int) {
        val cXy = mXy.get(charId)
        var isMoving = false
        if (heldKeys.isDown(Keys.W) || heldKeys.isDown(Keys.UP)) {
            cXy.y += speed
            isMoving = true
        }
        if (heldKeys.isDown(Keys.A) || heldKeys.isDown(Keys.LEFT)) {
            cXy.x -= speed
            isMoving = true
        }
        if (heldKeys.isDown(Keys.S) || heldKeys.isDown(Keys.DOWN)) {
            cXy.y -= speed
            isMoving = true
        }
        if (heldKeys.isDown(Keys.D) || heldKeys.isDown(Keys.RIGHT)) {
            cXy.x += speed
            isMoving = true
        }

        val cEnvObjTile = mEnvObjTile.get(charId)
        if (keyDownDirection.size > 0) {
            cEnvObjTile.direction = keyDownDirection.last()
        }

        val cAnimationFrames = mAnimationFrames.get(charId)
        if (isMoving) {
            cAnimationFrames.tMax = 8
            cAnimationFrames.frameMax = 3
        } else {
            cAnimationFrames.tMax = 0
            cAnimationFrames.frame = 0
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        heldKeys.keyDown(keycode)

        if (isEnabled) {
            if (keycode == Keys.W || keycode == Keys.UP) {
                keyDownDirection.add(Direction.N)
            }
            if (keycode == Keys.A || keycode == Keys.LEFT) {
                keyDownDirection.add(Direction.W)
            }
            if (keycode == Keys.S || keycode == Keys.DOWN) {
                keyDownDirection.add(Direction.S)
            }
            if (keycode == Keys.D || keycode == Keys.RIGHT) {
                keyDownDirection.add(Direction.E)
            }
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        heldKeys.keyUp(keycode)

        if (isEnabled) {
            if (keycode == Keys.W || keycode == Keys.UP) {
                keyDownDirection.remove(Direction.N)
            }
            if (keycode == Keys.A || keycode == Keys.LEFT) {
                keyDownDirection.remove(Direction.W)
            }
            if (keycode == Keys.S || keycode == Keys.DOWN) {
                keyDownDirection.remove(Direction.S)
            }
            if (keycode == Keys.D || keycode == Keys.RIGHT) {
                keyDownDirection.remove(Direction.E)
            }
        }
        return false
    }

    override fun keyTyped(character: Char) = false

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) = false

    override fun mouseMoved(screenX: Int, screenY: Int) = false

    override fun scrolled(amount: Int) = false
}
