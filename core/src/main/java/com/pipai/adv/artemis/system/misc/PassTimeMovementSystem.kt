package com.pipai.adv.artemis.system.misc

import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.math.Interpolation
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.components.LinesComponent
import com.pipai.adv.artemis.components.PathInterpolationComponent
import com.pipai.adv.artemis.components.SquadComponent
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.artemis.system.ui.WorldMapUiSystem
import com.pipai.adv.map.WorldMapLocation
import com.pipai.adv.utils.allOf
import com.pipai.adv.utils.mapper
import com.pipai.adv.utils.require
import com.pipai.adv.utils.system

class PassTimeMovementSystem(private val game: AdvGame) : IteratingSystem(allOf()) {
    private val mXy by require<XYComponent>()
    private val mSquad by require<SquadComponent>()
    private val mLines by require<LinesComponent>()

    private val mPath by mapper<PathInterpolationComponent>()

    private val sUi by system<WorldMapUiSystem>()

    override fun initialize() {
        super.initialize()
        isEnabled = false
    }

    override fun process(entityId: Int) {
        val cLines = mLines.get(entityId)
        val cPath = mPath.create(entityId)
        if (cPath.endpoints.isEmpty()) {
            cPath.interpolation = Interpolation.linear
            cPath.endpoints.add(cLines.lines.first().first.cpy())
            cPath.endpoints.add(cLines.lines.first().second.cpy())
            cPath.setUsingSpeed(2.0)
            cPath.onEndpoint = {
                mLines.remove(entityId)
                sUi.stopPassTime()
            }
        }
        val cXy = mXy.get(entityId)
        cLines.lines.first().first.set(cXy.x, cXy.y)
        game.globals.save!!.squadLocations[mSquad.get(entityId).squad] = WorldMapLocation(cXy.x.toInt(), cXy.y.toInt())
    }
}
