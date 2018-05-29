package com.pipai.adv.artemis.system.ui

import com.artemis.BaseSystem
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.screens.GuildScreen
import com.pipai.adv.backend.progression.LevelBackend
import com.pipai.adv.domain.ResultsData

class ResultsUiSystem(private val game: AdvGame,
                      private val stage: Stage,
                      private val resultsData: ResultsData) : BaseSystem(), InputProcessor {

    private val globals = game.globals
    private val levelBackend = LevelBackend()

    private val table = Table()

    private val levelLabelMap: MutableMap<Int, Label> = mutableMapOf()
    private val expLabelMap: MutableMap<Int, Label> = mutableMapOf()
    private val maxExpLabelMap: MutableMap<Int, Label> = mutableMapOf()

    private val currentExp: MutableMap<Int, Int> = mutableMapOf()
    private val expAnimation: MutableMap<Int, Int> = mutableMapOf()
    private val currentExpMax: MutableMap<Int, Int> = mutableMapOf()
    private val currentLevel: MutableMap<Int, Int> = mutableMapOf()

    private var expAnimationDelay = 30

    init {
        createMainForm()
        resultsData.expGiven.forEach {
            giveExp(it.key, it.value)
        }
    }

    private fun createMainForm() {
        val skin = game.skin
        val save = game.globals.save!!

        val width = game.advConfig.resolution.width.toFloat()
        val height = game.advConfig.resolution.height.toFloat()
        table.x = 8f
        table.y = 8f
        table.width = width - 16f
        table.height = height - 16f
        table.background = skin.getDrawable("frameDrawable")

        table.left().top().padLeft(8f).padTop(8f)
        table.add(Label("Results", skin))
        table.row()
        table.add(Label("EXP Received:", skin))
        table.row()

        val expTable = Table()
        expTable.left().top()

        resultsData.expGiven.keys.sorted().forEach {
            val npc = save.globalNpcList.getNpc(it)!!
            val nameLabel = Label("${npc.unitInstance.nickname}: ", skin)
            nameLabel.setAlignment(Align.left)
            val levelLabel = Label("Lv. ${npc.unitInstance.level} ", skin)
            levelLabel.setAlignment(Align.left)
            levelLabelMap[it] = levelLabel
            val expLabel = Label("${npc.unitInstance.exp}", skin)
            expLabelMap[it] = expLabel
            val maxExpLabel = Label("/${levelBackend.expRequired(npc.unitInstance.level)}", skin)
            maxExpLabelMap[it] = maxExpLabel
            expTable.add(nameLabel).minWidth(160f)
            expTable.add(levelLabel).minWidth(100f)
            expTable.add(expLabel)
            expTable.add(maxExpLabel).minWidth(100f)
            expTable.add(Label("+ ${resultsData.expGiven[it]}", skin))
            expTable.row()
            currentLevel[it] = npc.unitInstance.level
            currentExp[it] = npc.unitInstance.exp
            expAnimation[it] = resultsData.expGiven[it]!!
            currentExpMax[it] = levelBackend.expRequired(npc.unitInstance.level)

        }
        table.add(expTable)
        table.row()

        table.validate()
        stage.addActor(table)
    }

    fun giveExp(npcId: Int, expGained: Int): Boolean {
        val save = globals.save!!
        val unitInstance = save.globalNpcList.getNpc(npcId)!!.unitInstance
        unitInstance.exp += expGained
        val levelExp = levelBackend.expRequired(unitInstance.level)
        val levelledUp = unitInstance.exp > levelBackend.expRequired(unitInstance.level)
        if (levelledUp) {
            unitInstance.level += 1
            unitInstance.exp -= levelExp
            save.sp[npcId] = save.sp[npcId]!! + 1
        }
        return levelledUp
    }

    override fun processSystem() {
        if (expAnimationDelay < 0) {
            resultsData.expGiven.keys.forEach {
                val expToGive = expAnimation[it]!!
                if (expToGive > 0) {
                    val amountAnimation = Math.ceil(expToGive / 60.0).toInt()
                    expAnimation[it] = expToGive - amountAnimation
                    currentExp[it] = currentExp[it]!! + amountAnimation
                    if (currentExp[it]!! > currentExpMax[it]!!) {
                        currentExp[it] = currentExp[it]!! - currentExpMax[it]!!
                        currentLevel[it] = currentLevel[it]!! + 1
                        currentExpMax[it] = levelBackend.expRequired(currentLevel[it]!!)
                    }
                    levelLabelMap[it]!!.setText("Lv. ${currentLevel[it]!!}")
                    expLabelMap[it]!!.setText("${currentExp[it]!!}")
                    maxExpLabelMap[it]!!.setText("/${currentExpMax[it]!!}")
                }
            }
        } else {
            expAnimationDelay--
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            Keys.ESCAPE -> {
                game.screen = GuildScreen(game)
            }
            Keys.ENTER -> {
                game.screen = GuildScreen(game)
            }
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean = false

    override fun keyTyped(character: Char) = false

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = false

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) = false

    override fun mouseMoved(screenX: Int, screenY: Int) = false

    override fun scrolled(amount: Int): Boolean = false

}
