package com.pipai.adv.artemis.events

import com.badlogic.gdx.graphics.Color
import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.backend.battle.domain.Team
import com.pipai.adv.backend.battle.engine.log.BattleLogEvent
import net.mostlyoriginal.api.event.common.Event

data class TileHighlightUpdateEvent(val tileHighlights: Map<Color, List<GridPosition>>) : Event

data class ZoomScrollDisableEvent(val disabled: Boolean) : Event

data class MouseCameraMoveDisableEvent(val disabled: Boolean) : Event

data class BattleEventAnimationEndEvent(val event: BattleLogEvent) : Event

class CommandAnimationEndEvent : Event

data class BattleTextEvent(val text: String) : Event

data class EndTurnEvent(val team: Team) : Event

class DirectorsFinishedEvent : Event
