package com.pipai.adv.backend.battle.generators

import com.pipai.adv.backend.battle.domain.*
import com.pipai.adv.backend.battle.engine.rules.ending.EndingRule
import com.pipai.adv.backend.battle.engine.rules.ending.ItemRetrievalEndingRule
import com.pipai.adv.backend.battle.engine.rules.ending.MapClearEndingRule
import com.pipai.adv.domain.QuestGoal
import com.pipai.adv.tiles.TileDescriptor
import com.pipai.adv.tiles.TilePosition
import com.pipai.adv.utils.AutoIncrementIdMap

class BattleEndingRuleGenerator {

    fun generate(goal: QuestGoal?, envObjList: AutoIncrementIdMap<EnvObject>, map: BattleMap): EndingRule {
        return when (goal) {
            is QuestGoal.ItemRetrievalGoal -> {
                val chestTile = EnvObjTilesetMetadata.SingleTilesetMetadata(TileDescriptor("chest", TilePosition(0, 0)))
                map.getCell(3, map.height - 2).fullEnvObjId = envObjList.add(ChestEnvObject(InventoryItem.MiscItem(goal.item), chestTile))
                return ItemRetrievalEndingRule(goal.item, GridPosition(3, map.height - 2))
            }
            else -> MapClearEndingRule()
        }
    }

}
