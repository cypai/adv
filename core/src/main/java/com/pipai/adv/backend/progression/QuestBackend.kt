package com.pipai.adv.backend.progression

import com.pipai.adv.domain.Quest
import com.pipai.adv.domain.QuestGoal
import com.pipai.adv.domain.QuestStage
import com.pipai.adv.map.WorldMapLocation
import com.pipai.adv.save.AdvSave

class QuestBackend {

    fun scheduleEvents(save: AdvSave) {
    }

    private fun AdvSave.addQuest(quest: String) {
        if (!availableQuests.contains(quest) && !activeQuests.contains(quest)) {
            availableQuests.add(quest)
        }
    }

    private fun generateQuestsForTown(town: String): List<Quest> {
        val questList = mutableListOf<Quest>()
        questList.add(Quest("Pest Extermination", mapOf(
                Pair("start", QuestStage(listOf(QuestGoal.ClearTempMapGoal("$town Farm", WorldMapLocation(200, 100))),
                        "Clear the $town Farm of pests!")),
                Pair("report", QuestStage(listOf(QuestGoal.TalkWithNpcGoal("Guild Associate")),
                        "Report your success to the Guild Associate!")))
        ))
        return questList
    }

}
