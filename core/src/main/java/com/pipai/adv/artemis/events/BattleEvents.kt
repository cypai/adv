package com.pipai.adv.artemis.events

import com.badlogic.gdx.graphics.Color
import com.pipai.adv.backend.battle.domain.GridPosition
import net.mostlyoriginal.api.event.common.Event

data class TileHighlightUpdateEvent(val tileHighlights: Map<Color, List<GridPosition>>) : Event

data class ZoomScrollDisableEvent(val disabled: Boolean) : Event

class CommandAnimationEndEvent() : Event
