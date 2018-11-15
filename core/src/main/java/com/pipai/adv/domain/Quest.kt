package com.pipai.adv.domain

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.pipai.adv.map.WorldMapLocation

data class Quest(val name: String, val stages: Map<String, QuestStage>)

data class QuestStage(val goals: List<QuestGoal>, val description: String)

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
sealed class QuestGoal {
    data class TalkWithNpcGoal(val npcName: String) : QuestGoal()
    data class FetchGoal(val items: List<Pair<String, Int>>) : QuestGoal()
    data class ClearMapGoal(val location: String) : QuestGoal()
    data class ClearRandomMapGoal(val location: WorldMapLocation) : QuestGoal()
    data class ItemRetrievalGoal(val items: List<String>, val location: String) : QuestGoal()
}
