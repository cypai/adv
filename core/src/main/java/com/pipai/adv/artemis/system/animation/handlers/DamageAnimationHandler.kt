package com.pipai.adv.artemis.system.animation.handlers

import com.artemis.ComponentMapper
import com.artemis.World
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.math.Interpolation
import com.pipai.adv.AdvConfig
import com.pipai.adv.artemis.components.PathInterpolationComponent
import com.pipai.adv.artemis.components.PathInterpolationEndStrategy
import com.pipai.adv.artemis.components.TextComponent
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.artemis.events.BattleEventAnimationEndEvent
import com.pipai.adv.artemis.system.misc.NpcIdSystem
import com.pipai.adv.backend.battle.engine.log.DamageEvent
import net.mostlyoriginal.api.event.common.EventSystem

class DamageAnimationHandler(val config: AdvConfig, private val textFont: BitmapFont, private val world: World) {

    private lateinit var mPath: ComponentMapper<PathInterpolationComponent>
    private lateinit var mText: ComponentMapper<TextComponent>
    private lateinit var mXy: ComponentMapper<XYComponent>

    private lateinit var sNpcId: NpcIdSystem
    private lateinit var sEvent: EventSystem

    private val glayout = GlyphLayout()

    init {
        world.inject(this)
    }

    fun animate(damageEvent: DamageEvent) {
        val targetEntityId = sNpcId.getNpcEntityId(damageEvent.npcId)

        if (targetEntityId != null) {
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
            cPath.onEnd = PathInterpolationEndStrategy.DESTROY
            cPath.onEndpoint = { sEvent.dispatch(BattleEventAnimationEndEvent(damageEvent)) }
        }
    }

}
