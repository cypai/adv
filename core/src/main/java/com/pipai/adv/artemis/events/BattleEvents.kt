package com.pipai.adv.artemis.events

import com.pipai.adv.backend.battle.engine.MapGraph
import net.mostlyoriginal.api.event.common.Event

data class MovementTileUpdateEvent(val mapGraph: MapGraph) : Event

data class PlayerUnitSelectedEvent(val npcId: Int) : Event

data class PlayerUnitUnselectedEvent(val npcId: Int) : Event

data class NonPlayerUnitSelectedEvent(val npcId: Int) : Event

data class NonPlayerUnitUnselectedEvent(val npcId: Int) : Event

data class TargetSelectionEvent(val npcIds: List<Int>) : Event
