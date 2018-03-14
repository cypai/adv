package com.pipai.adv.artemis.system.misc

import com.pipai.adv.artemis.events.PauseEvent
import net.mostlyoriginal.api.event.common.Subscribe

interface PausableSystem {

    fun setEnabled(enabled: Boolean)

    @Subscribe
    fun handlePause(event: PauseEvent) {
        setEnabled(!event.isPaused)
    }

}
