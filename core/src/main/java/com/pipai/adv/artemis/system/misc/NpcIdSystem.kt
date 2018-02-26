package com.pipai.adv.artemis.system.misc

import com.artemis.EntitySubscription
import com.artemis.utils.IntBag
import com.pipai.adv.artemis.components.NpcIdComponent
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.forEach
import com.pipai.adv.utils.mapper

class NpcIdSystem : NoProcessingSystem() {

    private val mNpcId by mapper<NpcIdComponent>()

    // npcId -> entityId mapping
    private val npcIdIndex: MutableMap<Int, Int> = mutableMapOf()

    override fun initialize() {
        world.aspectSubscriptionManager.get(allOf(NpcIdComponent::class)).addSubscriptionListener(object : EntitySubscription.SubscriptionListener {
            override fun inserted(entities: IntBag?) {
                entities?.forEach {
                    npcIdIndex[mNpcId.get(it).npcId] = it
                }
            }

            override fun removed(entities: IntBag?) {
                entities?.forEach {
                    npcIdIndex.remove(mNpcId.get(it).npcId)
                }
            }
        })
    }

    fun getNpcEntityId(npcId: Int) = npcIdIndex[npcId]

}
