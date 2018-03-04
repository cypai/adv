package com.pipai.adv.artemis.events

import com.pipai.adv.backend.battle.engine.MapGraph
import net.mostlyoriginal.api.event.common.Event

data class MovementTileUpdateEvent(val mapGraph: MapGraph?) : Event
