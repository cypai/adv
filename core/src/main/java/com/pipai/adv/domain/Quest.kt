package com.pipai.adv.domain

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.pipai.adv.map.WorldMapLocation

data class Quest(val name: String, val goals: List<QuestGoal>)

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
sealed class QuestGoal {
    data class FetchGoal(val items: List<Pair<String, Int>>) : QuestGoal()
    data class ClearMapGoal(val location: String) : QuestGoal()
    data class ClearRandomMapGoal(val location: WorldMapLocation) : QuestGoal()
}
