package com.pipai.adv.artemis.system.misc

import com.artemis.BaseSystem
import com.badlogic.gdx.math.Interpolation
import com.pipai.adv.AdvGameGlobals
import com.pipai.adv.artemis.system.input.CharacterMovementInputSystem
import com.pipai.adv.map.WorldMapLocation
import com.pipai.adv.save.AdvSave
import com.pipai.adv.utils.system

class PassTimeMovementSystem(private val globals: AdvGameGlobals) : BaseSystem() {

    private val sMovement by system<CharacterMovementInputSystem>()

    private var timeBuffer = 0
    private val maxTimeBuffer = 60

    override fun processSystem() {
        if (sMovement.isMoving) {
            timeBuffer += 1
            if (timeBuffer > maxTimeBuffer) {
                timeBuffer = 0
                globals.timeBackend.update()
            }
        }
    }
}
