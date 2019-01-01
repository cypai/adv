package com.pipai.adv.artemis.system.cutscene.directors

import com.artemis.ComponentMapper
import com.artemis.World
import com.badlogic.gdx.Gdx
import com.pipai.adv.artemis.components.XYComponent
import com.pipai.adv.artemis.system.input.CutsceneInputSystem
import com.pipai.adv.artemis.system.misc.CameraInterpolationSystem
import com.pipai.adv.artemis.system.misc.NpcIdSystem
import com.pipai.adv.artemis.system.rendering.BattleMapRenderingSystem
import com.pipai.adv.backend.battle.domain.Team
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.domain.Cutscene
import com.pipai.adv.domain.CutsceneUtils
import com.pipai.adv.map.TileVisibility

class TutorialDirector(world: World) : BattleCutsceneDirector {

    private lateinit var cXy: ComponentMapper<XYComponent>

    private lateinit var sCutscene: CutsceneInputSystem
    private lateinit var sCameraInterpolation: CameraInterpolationSystem
    private lateinit var sBattleMapRenderer: BattleMapRenderingSystem
    private lateinit var sNpcId: NpcIdSystem

    private val cutscene: Cutscene

    private var controlsExplained = false
    private var attackExplained = false
    private var statusExplained = false

    init {
        world.inject(this)
        cutscene = CutsceneUtils.loadCutscene(Gdx.files.local("assets/data/cutscenes/opening.txt"))
    }

    override fun check(backend: BattleBackend): Boolean {
        if (controlsExplained) {
            if (!attackExplained || !statusExplained) {
                val enemyTeam = backend.getTeam(Team.AI)
                val fogOfWar = sBattleMapRenderer.fogOfWar
                val visibleEnemies = enemyTeam.filter { fogOfWar.getPlayerTileVisibility(backend.getNpcPosition(it)!!) == TileVisibility.VISIBLE }
                if (visibleEnemies.isNotEmpty()) {
                    val butterfly: Boolean = visibleEnemies.any { backend.getNpc(it)!!.unitInstance.schema == "Black Butterfly" }
                    val cutsceneVariables = mapOf("blackButterfly" to if (butterfly) "TRUE" else "FALSE")
                    val cameraFocusEnemy = if (butterfly) {
                        visibleEnemies.find { backend.getNpc(it)!!.unitInstance.schema == "Black Butterfly" }!!
                    } else {
                        visibleEnemies[0]
                    }
                    sCameraInterpolation.sendCameraToPosition(cXy.get(sNpcId.getNpcEntityId(cameraFocusEnemy)!!).toVector2())
                    sCutscene.cutscene = cutscene
                    if (attackExplained) {
                        if (butterfly) {
                            sCutscene.showScene("tutorialStatus", cutsceneVariables)
                            statusExplained = true
                            return true
                        }
                    } else {
                        sCutscene.showScene("tutorialEnemyEncounter", cutsceneVariables)
                        attackExplained = true
                        statusExplained = butterfly
                        return true
                    }
                }
            }
        } else {
            sCutscene.cutscene = cutscene
            sCutscene.showScene("tutorialSkip")
            controlsExplained = true
            if (sCutscene.getVariable("skipTutorial") == "Yes") {
                attackExplained = true
                statusExplained = true
            }
            return true
        }
        return false
    }
}
