package com.pipai.adv.artemis.system.rendering

import com.artemis.BaseSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Interpolation
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.events.BackgroundFadeFinishedEvent
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.EventSystem

class BackgroundRenderingSystem(game: AdvGame) : BaseSystem() {

    private val sEvent by system<EventSystem>()

    private val batch = game.batchHelper
    private val config = game.advConfig
    private val skin = game.skin

    private var background: Texture? = null
    private var bgColor: Color = Color(0f, 0f, 0f, 0f)

    private var t = 0f
    private var maxT = 0f
    private var state: State = State.NONE

    override fun processSystem() {
        when (state) {
            State.FADE_IN -> {
                bgColor.set(0f, 0f, 0f, Interpolation.linear.apply(1f, 0f, t / maxT))
            }
            State.FADE_OUT -> {
                bgColor.set(0f, 0f, 0f, Interpolation.linear.apply(0f, 1f, t / maxT))
            }
            State.NONE -> {
                // Do nothing
            }
        }
        if (state != State.NONE) {
            t++
            if (t > maxT) {
                state = State.NONE
                sEvent.dispatch(BackgroundFadeFinishedEvent())
            }
        }

        batch.spr.color = Color.WHITE
        batch.spr.begin()
        if (background != null) {
            batch.spr.draw(background, 0f, 0f, config.resolution.width.toFloat(), config.resolution.height.toFloat())
        }
        if (bgColor.a > 0) {
            skin.newDrawable("white", bgColor)
                    .draw(batch.spr, 0f, 0f, config.resolution.width.toFloat(), config.resolution.height.toFloat())
        }
        batch.spr.end()
    }

    fun changeBackground(textureName: String) {
        background = skin.get(textureName, Texture::class.java)
    }

    fun fadeIn(time: Int) {
        t = 0f
        maxT = time.toFloat()
        state = State.FADE_IN
    }

    fun fadeOut(time: Int) {
        t = 0f
        maxT = time.toFloat()
        state = State.FADE_OUT
    }

    private enum class State {
        FADE_IN, FADE_OUT, NONE
    }

}
