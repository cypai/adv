package com.pipai.adv.backend.progression

import com.pipai.adv.domain.Quest
import com.pipai.adv.domain.QuestGoal
import com.pipai.adv.map.WorldMapLocation
import com.pipai.adv.save.AdvSave

class ProgressionBackend {

    private val quests: MutableMap<String, Quest> = mutableMapOf()

    init {
        initQuests()
    }

    fun scheduleEvents(save: AdvSave) {
        if (save.finishedQuests.contains("Guild Exam: D")) {
            save.addQuest("Clear Lagos Forest")
        } else {
            save.addQuest("Guild Exam: D")
        }
    }

    private fun AdvSave.addQuest(quest: String) {
        if (!availableQuests.contains(quest) && !activeQuests.contains(quest)) {
            availableQuests.add(quest)
        }
    }

    private fun createQuest(quest: Quest) {
        quests[quest.name] = quest
    }

    private fun initQuests() {
        createQuest(Quest("Guild Exam: D",
                listOf(QuestGoal.ItemRetrievalGoal(listOf("D-Rank Guild Card"), "Lagos Forest")),
                "Enter Lagos Forest, find the chest containing the D-Rank Guild Card, and return to tell the tale!"))
        createQuest(Quest("Clear Lagos Forest",
                listOf(QuestGoal.ClearMapGoal("Lagos Forest")),
                "We need to make sure the monsters in nearby Lagos Forest should be kept at low numbers. " +
                        "Please clear Lagos Forest of monsters."))
        createQuest(Quest("Rabbit Extermination",
                listOf(QuestGoal.ClearRandomMapGoal(WorldMapLocation(200, 150))),
                "A local farmer has a request!"))
    }

}
