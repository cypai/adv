package com.pipai.adv.artemis.system.misc

import com.pipai.adv.artemis.components.PccComponent
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.gui.PccCustomizer
import com.pipai.adv.tiles.PccMetadata
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.mapper

class PccPreviewSystem(pccCustomizer: PccCustomizer) : NoProcessingSystem() {

    private val mPcc by mapper<PccComponent>()

    init {
        pccCustomizer.addChangeListener { onPccCustomizationChange(it) }
    }

    private fun onPccCustomizationChange(pcc: List<PccMetadata>) {
        val pccEntityBag = world.aspectSubscriptionManager.get(allOf(PccComponent::class)).entities
        val pccEntities = pccEntityBag.data.slice(0 until pccEntityBag.size())

        pccEntities.forEach {
            val cPcc = mPcc.get(it)
            cPcc.pcc = pcc
        }
    }
}
