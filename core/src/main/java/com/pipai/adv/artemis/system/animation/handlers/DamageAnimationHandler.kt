package com.pipai.adv.artemis.system.animation.handlers

import com.artemis.ComponentMapper
import com.artemis.World
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.math.Interpolation
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.components.*
import com.pipai.adv.artemis.events.BattleEventAnimationEndEvent
import com.pipai.adv.artemis.system.misc.CameraInterpolationSystem
import com.pipai.adv.artemis.system.misc.NpcIdSystem
import com.pipai.adv.backend.battle.engine.log.DamageEvent
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.fetch
import net.mostlyoriginal.api.event.common.EventSystem

class DamageAnimationHandler(val config: AdvConfig, private val textFont: BitmapFont, private val world: World) {

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

    fun animate(damageEvent: DamageEvent) {
        val targetEntityId = sNpcId.getNpcEntityId(damageEvent.npcId)

        if (targetEntityId == null) {
            updateSideUiHp(damageEvent)
        } else {
            val targetXy = mXy.get(targetEntityId)
            sCameraInterpolation.sendCameraToPosition(targetXy.toVector2(), { animateDamage(damageEvent, targetEntityId) })
        }
    }

    private fun animateDamage(damageEvent: DamageEvent, targetEntityId: Int) {
        val targetXy = mXy.get(targetEntityId)
        val textEntityId = world.create()
        val cText = mText.create(textEntityId)
        cText.text = damageEvent.damage.toString()
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
        cPath.onEndpoint = { sEvent.dispatch(BattleEventAnimationEndEvent(damageEvent)) }
        val cUnitHealthbar = mUnitHealthbar.getSafe(targetEntityId, null)
        if (cUnitHealthbar != null) {
            val npcInstance = damageEvent.npc.unitInstance
            cUnitHealthbar.percentage = npcInstance.hp.toFloat() / npcInstance.schema.baseStats.hpMax.toFloat()
        }
        updateSideUiHp(damageEvent)
    }

    private fun updateSideUiHp(damageEvent: DamageEvent) {
        world.fetch(allOf(SideUiBoxComponent::class))
                .firstOrNull { mSideUiBox.get(it).npcId == damageEvent.npcId }
                ?.let {
                    val cSideUiBox = mSideUiBox.get(it)
                    cSideUiBox.hp = Math.max(0, cSideUiBox.hp - damageEvent.damage)
                }
    }

}
