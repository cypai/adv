package com.pipai.adv.backend.progression

import com.pipai.adv.domain.Quest
import com.pipai.adv.domain.QuestGoal
import com.pipai.adv.save.AdvSave

class ProgressionBackend {

    fun scheduleEvents(save: AdvSave) {
        save.availableQuests.add(Quest(
                "Monthly Maintenance: Lagos Forest",
                listOf(QuestGoal.ClearMapGoal("Lagos Forest"))))
    }

}
