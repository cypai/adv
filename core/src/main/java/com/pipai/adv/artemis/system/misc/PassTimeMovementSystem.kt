package com.pipai.adv.artemis.system.misc

import com.artemis.BaseSystem
import com.pipai.adv.AdvGameGlobals
import com.pipai.adv.artemis.system.input.CharacterMovementInputSystem
import com.pipai.adv.utils.system
import java.time.Duration

class PassTimeMovementSystem(private val globals: AdvGameGlobals) : BaseSystem() {

    private val sMovement by system<CharacterMovementInputSystem>()

    private val save = globals.save!!
    private val updateTimespan: Duration = Duration.ofHours(2)

    private var timeBuffer = 0
    private val maxTimeBuffer = 60

    override fun processSystem() {
        if (sMovement.isMoving) {
            timeBuffer += 1
            if (timeBuffer > maxTimeBuffer) {
                timeBuffer = 0
                updateTime()
            }
        }
    }

    private fun updateTime() {
        save.time = save.time.plus(updateTimespan)
        globals.questBackend.scheduleEvents(globals)
    }
}
