package com.pipai.adv.backend.progression

import com.pipai.adv.AdvGameGlobals
import com.pipai.adv.artemis.components.CollisionBounds
import com.pipai.adv.domain.Quest
import com.pipai.adv.domain.QuestGoal
import com.pipai.adv.domain.QuestStage
import com.pipai.adv.map.PointOfInterest
import com.pipai.adv.map.PointOfInterestType
import com.pipai.adv.map.WorldMapLocation
import com.pipai.adv.save.AdvSave
import com.pipai.adv.utils.CollisionUtils
import com.pipai.adv.utils.RNG
import com.pipai.adv.utils.getLogger

class QuestBackend {

    private val logger = getLogger()

    private val quests: MutableMap<String, Quest> = mutableMapOf()

    init {
        initQuests()
    }

    fun getQuest(name: String): Quest = quests[name]!!

    fun scheduleEvents(globals: AdvGameGlobals) {
        val save = globals.save!!
        if (save.finishedQuests.contains("Guild Exam: D")) {
            save.addQuest("Clear Lagos Forest")
        } else {
            save.addQuest("Guild Exam: D")
        }
        globals.worldMap.getAllPois().forEach { generateQuestsForPoi(save, it) }
    }

    private fun generateQuestsForPoi(save: AdvSave, poi: PointOfInterest) {
        val questList = mutableListOf<Quest>()
        when (poi.type) {
            PointOfInterestType.VILLAGE -> {
                if (RNG.nextInt(4) == 0) {
                    val town = poi.name
                    val location = randomLocationNear(poi.location, 16, 160)
                    val quest = Quest("Pest Extermination: $town Farm", mapOf(
                            Pair("start", QuestStage(listOf(QuestGoal.ClearTempMapGoal("$town Farm", location)),
                                    "Clear the $town Farm of pests!")),
                            Pair("report", QuestStage(listOf(QuestGoal.TalkWithNpcGoal("Guild Associate")),
                                    "Report your success to the Guild Associate!")))
                    )
                    logger.info("Generated quest $quest")
                    questList.add(quest)
                }
            }
            else -> {
            }
        }
        save.availableQuests
    }

    private fun randomLocationNear(location: WorldMapLocation, innerBounds: Int, outerBounds: Int): WorldMapLocation {
        var x = RNG.nextInt(2 * outerBounds) - outerBounds
        var y = RNG.nextInt(2 * outerBounds) - outerBounds
        while (CollisionUtils.withinBounds(x.toFloat(), y.toFloat(), location.x.toFloat(), location.y.toFloat(),
                        CollisionBounds.CollisionBoundingBox(innerBounds.toFloat(), innerBounds.toFloat(), true))) {
            x = RNG.nextInt(2 * outerBounds) - outerBounds
            y = RNG.nextInt(2 * outerBounds) - outerBounds
        }
        return WorldMapLocation(x, y)
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
