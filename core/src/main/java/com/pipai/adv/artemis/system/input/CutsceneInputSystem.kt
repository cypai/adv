package com.pipai.adv.artemis.system.input

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.pipai.adv.AdvGame
import com.pipai.adv.artemis.system.NoProcessingSystem
import com.pipai.adv.artemis.system.ui.MainTextboxUiSystem
import com.pipai.adv.domain.Cutscene
import com.pipai.adv.domain.CutsceneLine
import com.pipai.adv.domain.CutsceneLineType
import com.pipai.adv.utils.getLogger
import com.pipai.adv.utils.getSystemSafe
import com.pipai.adv.utils.system

class CutsceneInputSystem(private val game: AdvGame, var cutscene: Cutscene) : NoProcessingSystem(), InputProcessor {

    private val logger = getLogger()

    private val sMainTextbox by system<MainTextboxUiSystem>()

    private var scene: String = "start"
    private var currentIndex: Int = 0
    private var currentLine: CutsceneLine? = null

    private val variables: MutableMap<String, String> = mutableMapOf()

    override fun keyDown(keycode: Int): Boolean {
        if (isEnabled) {
            if (keycode == Keys.Z) {
                if (currentLine?.text != null) {
                    if (sMainTextbox.isDone()) {
                        finishLine()
                    } else {
                        sMainTextbox.showFullText()
                    }
                }
            }
        }
        return false
    }

    fun showScene(sceneName: String, initVariables: Map<String, String>) {
        variables.clear()
        variables.putAll(initVariables)
        currentIndex = 0
        scene = sceneName
        if (scene in cutscene.scenes) {
            performLine(cutscene.scenes[scene]!![0])
        }

    }

    fun showScene(sceneName: String) {
        showScene(sceneName, mapOf())
    }

    private fun performLine(line: CutsceneLine) {
        currentLine = line
        when (line.type) {
            CutsceneLineType.TEXT -> {
                showText(line.text!!)
            }
            CutsceneLineType.COMMAND -> {
                runCommand(line.command!!, line.args!!)
            }
        }
    }

    private fun showText(text: String) {
        disableSystems()
        sMainTextbox.setToText(interpolateText(text))
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
                // Change background somehow
                finishLine() // This will probably be moved somewhere else
            }
            "exit" -> {
                enableSystems()
                sMainTextbox.isEnabled = false
            }
            "ifjmp" -> {
                if (checkCondition(interpolateText(args[1]), args[2], interpolateText(args[3]))) {
                    currentIndex = -1
                    scene = args[0]
                }
                finishLine()
            }
            "ifshow" -> {
                if (checkCondition(interpolateText(args[1]), args[2], interpolateText(args[3]))) {
                    val lineSplit = args[4].split('|')
                    showText(lineSplit[1])
                } else {
                    finishLine()
                }
            }
            "jmp" -> {
                currentIndex = -1
                scene = args[0]
                finishLine()
            }
            "quest" -> {
                when (args[0]) {
                    "accept" -> {
                        game.globals.save!!.availableQuests.remove(args[1])
                        game.globals.save!!.activeQuests[args[1]] = args[2]
                        game.globals.autoSave()
                    }
                    "updatestage" -> {
                        game.globals.save!!.activeQuests[args[1]] = args[2]
                        game.globals.autoSave()
                    }
                    else -> logger.warn("Unsupported quest command ${args[0]}")
                }
            }
            "saveedit" -> {
                when (args[0]) {
                    "guildname" -> {
                        game.globals.save!!.changePlayerGuildName(interpolateText(args[1]))
                        game.globals.autoSave()
                    }
                    else -> logger.warn("Unsupported saveedit command ${args[0]}")
                }
                finishLine()
            }
            "set" -> {
                variables[args[0]] = args[1]
                finishLine()
            }
            "textinput" -> {
                variables[args[0]] = "Moriya"
                finishLine()
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

    private fun finishLine() {
        enableSystems()
        sMainTextbox.isEnabled = false
        nextLine()
    }

    private fun nextLine() {
        currentIndex++
        if (currentIndex < cutscene.scenes[scene]!!.size) {
            performLine(cutscene.scenes[scene]!![currentIndex])
        }
    }

    private fun disableSystems() {
        world.getSystemSafe(CharacterMovementInputSystem::class.java)?.disable()
    }

    private fun enableSystems() {
        world.getSystemSafe(CharacterMovementInputSystem::class.java)?.enable()
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
