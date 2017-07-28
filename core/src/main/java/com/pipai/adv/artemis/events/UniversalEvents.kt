package com.pipai.adv.artemis.events

import com.pipai.adv.ScreenResolution
import net.mostlyoriginal.api.event.common.Event

data class ScreenResolutionChangeEvent(val resolution: ScreenResolution) : Event