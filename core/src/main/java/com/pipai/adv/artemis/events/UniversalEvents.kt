package com.pipai.adv.artemis.events

import com.pipai.adv.ScreenResolution
import net.mostlyoriginal.api.event.common.Event

data class ScreenResolutionChangeEvent(val resolution: ScreenResolution) : Event

data class MouseDownEvent(val x: Float, val y: Float, val button: Int) : Event

data class MouseUpEvent(val x: Float, val y: Float, val button: Int) : Event

data class MouseHoverEvent(val x: Float, val y: Float) : Event

data class KeyDownEvent(val keycode: Int) : Event
