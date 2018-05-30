package com.pipai.adv.artemis.system.animation.handlers

import com.artemis.ComponentMapper
import com.artemis.World
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.math.Interpolation
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.components.*
import com.pipai.adv.artemis.events.BattleEventAnimationEndEvent
import com.pipai.adv.artemis.system.misc.CameraInterpolationSystem
import com.pipai.adv.artemis.system.misc.NpcIdSystem
import com.pipai.adv.backend.battle.engine.log.HealEvent
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.fetch
import net.mostlyoriginal.api.event.common.EventSystem

class HealAnimationHandler(val config: AdvConfig, private val textFont: BitmapFont, private val world: World) {

    private lateinit var mPath: ComponentMapper<PathInterpolationComponent>
    private lateinit var mText: ComponentMapper<TextComponent>
    private lateinit var mXy: ComponentMapper<XYComponent>
    private lateinit var mUnitHealthbar: ComponentMapper<UnitHealthbarComponent>
    private lateinit var mSideUiBox: ComponentMapper<SideUiBoxComponent>

    private lateinit var sCameraInterpolation: CameraInterpolationSystem
    private lateinit var sNpcId: NpcIdSystem
    private lateinit var sEvent: EventSystem

    private val glayout = GlyphLayout()

    init {
        world.inject(this)
    }

    fun animate(healEvent: HealEvent) {
        val targetEntityId = sNpcId.getNpcEntityId(healEvent.npcId)

        if (targetEntityId == null) {
            updateSideUiHp(healEvent)
            sEvent.dispatch(BattleEventAnimationEndEvent(healEvent))
        } else {
            val targetXy = mXy.get(targetEntityId)
            sCameraInterpolation.sendCameraToPosition(targetXy.toVector2(), {
                animateHeal(healEvent, targetEntityId)
            })
        }
    }

    private fun animateHeal(healEvent: HealEvent, targetEntityId: Int) {
        val targetXy = mXy.get(targetEntityId)
        val textEntityId = world.create()
        val cText = mText.create(textEntityId)
        cText.text = healEvent.healAmount.toString()
        cText.color = Color.GREEN
        glayout.setText(textFont, cText.text)
        val textWidth = glayout.width
        val cXy = mXy.create(textEntityId)
        val halfTileSize = config.resolution.tileSize / 2f
        cXy.setXy(targetXy.toVector2().add(halfTileSize - textWidth / 2f, halfTileSize))
        val cPath = mPath.create(textEntityId)
        cPath.interpolation = Interpolation.swingOut
        cPath.endpoints.clear()
        cPath.endpoints.add(cXy.toVector2())
        cPath.endpoints.add(cXy.toVector2().add(0f, config.resolution.tileSize * 0.7f))
        cPath.maxT = 30
        cPath.onEnd = EndStrategy.DESTROY
        cPath.onEndpoint = { sEvent.dispatch(BattleEventAnimationEndEvent(healEvent)) }
        val cUnitHealthbar = mUnitHealthbar.getSafe(targetEntityId, null)
        if (cUnitHealthbar != null) {
            val npcInstance = healEvent.npc.unitInstance
            cUnitHealthbar.percentage = npcInstance.hp.toFloat() / npcInstance.stats.hpMax.toFloat()
        }
        updateSideUiHp(healEvent)
    }

    private fun updateSideUiHp(healEvent: HealEvent) {
        world.fetch(allOf(SideUiBoxComponent::class))
                .firstOrNull { mSideUiBox.get(it).npcId == healEvent.npcId }
                ?.let {
                    val cSideUiBox = mSideUiBox.get(it)
                    cSideUiBox.hp = Math.max(0, cSideUiBox.hp + healEvent.healAmount)
                }
    }

}
