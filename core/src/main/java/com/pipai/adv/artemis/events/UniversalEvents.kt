package com.pipai.adv.artemis.events

import com.pipai.adv.ScreenResolution
import net.mostlyoriginal.api.event.common.Event

data class ScreenResolutionChangeEvent(val resolution: ScreenResolution) : Event

data class PauseEvent(val isPaused: Boolean) : Event

class BackgroundFadeFinishedEvent : Event

data class CutsceneEvent(val start: Boolean) : Event
