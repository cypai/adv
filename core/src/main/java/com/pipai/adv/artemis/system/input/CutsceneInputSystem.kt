package com.pipai.adv.artemis.system.input

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.events.BackgroundFadeFinishedEvent
import com.pipai.adv.artemis.events.CutsceneEvent
import com.pipai.adv.artemis.screens.GuildScreen
import com.pipai.adv.artemis.screens.VillageScreen
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.artemis.system.rendering.BackgroundRenderingSystem
import com.pipai.adv.artemis.system.ui.MainTextboxUiSystem
import com.pipai.adv.domain.Cutscene
import com.pipai.adv.domain.CutsceneLine
import com.pipai.adv.domain.CutsceneLineType
import com.pipai.adv.utils.getLogger
import com.pipai.adv.utils.system
import net.mostlyoriginal.api.event.common.EventSystem
import net.mostlyoriginal.api.event.common.Subscribe

class CutsceneInputSystem(private val game: AdvGame) : NoProcessingSystem(), InputProcessor {

    private val logger = getLogger()

    private val sBackgroundRenderingSystem by system<BackgroundRenderingSystem>()
    private val sMainTextbox by system<MainTextboxUiSystem>()

    private val sEvent by system<EventSystem>()

    private var scene: String = "start"
    private var currentIndex: Int = 0
    private var currentLine: CutsceneLine? = null

    private val variables: MutableMap<String, String> = mutableMapOf()

    var cutscene: Cutscene = Cutscene(mapOf())

    fun showScene(sceneName: String, initVariables: Map<String, String>) {
        sEvent.dispatch(CutsceneEvent(true))
        variables.clear()
        variables.putAll(initVariables)
        currentIndex = 0
        scene = sceneName
        if (scene in cutscene.scenes) {
            performLine(cutscene.scenes[scene]!![0])
        } else {
            logger.warn("Scene $sceneName does not exist")
        }
    }

    fun showScene(sceneName: String) {
        showScene(sceneName, mapOf())
    }

    private fun performLine(line: CutsceneLine) {
        currentLine = line
        logger.debug("Performing $line")
        when (line.type) {
            CutsceneLineType.TEXT -> {
                showText(line.speaker ?: "", line.text!!)
            }
            CutsceneLineType.COMMAND -> {
                runCommand(line.command!!, line.args!!)
            }
        }
    }

    private fun showText(speaker: String, text: String) {
        sMainTextbox.setToText(interpolateText(speaker), interpolateText(text))
        sMainTextbox.isEnabled = true
    }

    /**
     * Global interpolations:
     * player: Player character
     * playerGuild: Player guild name
     * squadSize: Amount of members in the current squad
     * squadName: Name of the current squad
     * leader: Name of the squad leader
     * squadRandomName: Name of a random member of the squad
     * squadMember[1-5]: Name of the member of the squad (1 is leader, 2-5 are the others)
     */
    private fun interpolateText(text: String): String {
        var interpolatedText = text.replace("\$player", game.globals.save!!.globalNpcList.getNpc(0)!!.unitInstance.nickname)
        variables.forEach { variable, value -> interpolatedText = interpolatedText.replace("\$$variable", value) }
        // To be replaced with actual squad size/health
        interpolatedText = interpolatedText.replace("\$squadSize", "5")
        interpolatedText = interpolatedText.replace("\$squadHealth", SquadHealth.GOOD.toString())
        return interpolatedText
    }

    private fun runCommand(command: String, args: List<String>) {
        when (command) {
            "bg" -> {
                sBackgroundRenderingSystem.changeBackground(args[0])
                finishCutsceneLine()
            }
            "bgfade" -> {
                when (args[0]) {
                    "in" -> sBackgroundRenderingSystem.fadeIn(Integer.valueOf(args[1]))
                    "out" -> sBackgroundRenderingSystem.fadeOut(Integer.valueOf(args[1]))
                }
            }
            "dialoginput" -> {
                variables[args[0]] = args[2]
                finishCutsceneLine()
            }
            "exit" -> {
                sEvent.dispatch(CutsceneEvent(false))
                sMainTextbox.isEnabled = false
            }
            "ifjmp" -> {
                if (checkCondition(interpolateText(args[0]), args[1], interpolateText(args[2]))) {
                    currentIndex = -1
                    scene = args[3]
                }
                finishCutsceneLine()
            }
            "ifshow" -> {
                if (checkCondition(interpolateText(args[0]), args[1], interpolateText(args[2]))) {
                    val lineSplit = args[3].split('|')
                    showText(lineSplit[0], lineSplit[1])
                } else {
                    finishCutsceneLine()
                }
            }
            "jmp" -> {
                currentIndex = -1
                scene = args[0]
                finishCutsceneLine()
            }
            "quest" -> {
                when (args[0]) {
                    "accept" -> {
                        game.globals.save!!.availableQuests.remove(args[1])
                        game.globals.save!!.activeQuests[args[1]] = args[2]
                        logger.debug("Quest ${args[1]} was accepted and set to stage ${args[2]}")
                        game.globals.autoSave()
                    }
                    "updatestage" -> {
                        game.globals.save!!.activeQuests[args[1]] = args[2]
                        logger.debug("Quest ${args[1]} was updated to stage ${args[2]}")
                        game.globals.autoSave()
                    }
                    else -> logger.warn("Unsupported quest command ${args[0]}")
                }
                finishCutsceneLine()
            }
            "saveedit" -> {
                when (args[0]) {
                    "guildname" -> {
                        val guildName = interpolateText(args[1])
                        game.globals.save!!.changePlayerGuildName(guildName)
                        logger.debug("Player guild name set to $guildName")
                        game.globals.autoSave()
                    }
                    "variables" -> {
                        val value = interpolateText(args[2])
                        game.globals.save!!.variables[args[1]] = value
                        logger.debug("Save variable ${args[1]} set to $value")
                        game.globals.autoSave()
                    }
                    else -> logger.warn("Unsupported saveedit command ${args[0]}")
                }
                finishCutsceneLine()
            }
            "screen" -> {
                when (args[0]) {
                    "GuildScreen" -> game.screen = GuildScreen(game)
                    "VillageScreen" -> game.screen = VillageScreen(game)
                }
            }
            "set" -> {
                variables[args[0]] = args[1]
                finishCutsceneLine()
            }
            "textinput" -> {
                variables[args[0]] = "Moriya"
                finishCutsceneLine()
            }
            else -> logger.warn("Unsupported command $command")
        }
    }

    private fun checkCondition(value1: String, operator: String, value2: String): Boolean {
        return when (operator) {
            "==" -> value1 == value2
            "!=" -> value1 != value2
            ">" -> Integer.valueOf(value1) > Integer.valueOf(value2)
            ">=" -> Integer.valueOf(value1) >= Integer.valueOf(value2)
            "<" -> Integer.valueOf(value1) < Integer.valueOf(value2)
            "<=" -> Integer.valueOf(value1) <= Integer.valueOf(value2)
            else -> throw IllegalStateException("Unsupported if operator $operator")
        }
    }

    private fun finishCutsceneLine() {
        sMainTextbox.isEnabled = false
        nextLine()
    }

    private fun nextLine() {
        currentIndex++
        if (currentIndex < cutscene.scenes[scene]!!.size) {
            performLine(cutscene.scenes[scene]!![currentIndex])
        } else {
            sEvent.dispatch(CutsceneEvent(false))
        }
    }

    fun getVariable(variable: String) = variables[variable]

    @Subscribe
    fun handleBackgroundFadeFinished(@Suppress("UNUSED_PARAMETER") event: BackgroundFadeFinishedEvent) {
        if (currentLine?.command == "bgfade") {
            finishCutsceneLine()
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        if (isEnabled) {
            if (keycode == Keys.Z) {
                if (currentLine?.text != null) {
                    if (sMainTextbox.isDone()) {
                        finishCutsceneLine()
                    } else {
                        sMainTextbox.showFullText()
                    }
                }
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

    private enum class SquadHealth {
        PERFECT, GOOD, BAD
    }

}
