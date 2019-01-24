package com.pipai.adv.backend.progression

import com.pipai.adv.domain.Quest
import com.pipai.adv.domain.QuestGoal
import com.pipai.adv.domain.QuestStage
import com.pipai.adv.map.WorldMapLocation
import com.pipai.adv.save.AdvSave

class ProgressionBackend {

    private val quests: MutableMap<String, Quest> = mutableMapOf()

    init {
        initQuests()
    }

    fun getQuest(name: String): Quest = quests[name]!!

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
        createQuest(Quest("Guild Exam: D", mapOf(
                Pair("start", QuestStage(listOf(QuestGoal.ItemRetrievalGoal("D-Rank Guild Card", "Lagos Forest")),
                        "Enter Lagos Forest, find the chest containing the D-Rank Guild Card, and return to tell the tale!")),
                Pair("report", QuestStage(listOf(QuestGoal.TalkWithNpcGoal("Guild Associate")),
                        "Report your success to the Guild Associate!")))
        ))
        createQuest(Quest("Clear Lagos Forest", mapOf(
                Pair("start", QuestStage(listOf(QuestGoal.ClearMapGoal("Lagos Forest")),
                        "We need to make sure the monsters in nearby Lagos Forest should be kept at low numbers. " +
                                "Please clear Lagos Forest of monsters.")),
                Pair("report", QuestStage(listOf(QuestGoal.TalkWithNpcGoal("Mayor")),
                        "Report your success to the mayor!")))
        ))
        createQuest(Quest("Rabbit Extermination", mapOf(
                Pair("start", QuestStage(listOf(QuestGoal.TalkWithNpcGoal("Farmer Guy")),
                        "A local farmer has a request! Speak with him to get the details.")),
                Pair("extermination", QuestStage(listOf(QuestGoal.ClearTempMapGoal("Carrot Farm", WorldMapLocation(200, 150))),
                        "Go to the farmer's field and exterminate all the rabbits!")),
                Pair("report", QuestStage(listOf(QuestGoal.TalkWithNpcGoal("Farmer Guy")),
                        "Report your success to the farmer!")))
        ))
        createQuest(Quest("Calico Cat Rescue", mapOf(
                Pair("start", QuestStage(listOf(QuestGoal.TalkWithNpcGoal("Calico")),
                        "Calico, a local young girl, has lost her cat. Speak with her to get the details.")),
                Pair("rescue", QuestStage(listOf(QuestGoal.ItemRetrievalGoal("Mr. Whiskers", "Glires Forest")),
                        "Seems like Mr. Whiskers left the village. Where could he be?")),
                Pair("report", QuestStage(listOf(QuestGoal.TalkWithNpcGoal("Calico")),
                        "Bring Mr. Whiskers back to Calico!")))
        ))
    }

}
