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

    fun showScene(sceneName: String) {
        variables.clear()
        currentIndex = 0
        scene = sceneName
        if (scene in cutscene.scenes) {
            performLine(cutscene.scenes[scene]!![0])
        }
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

    private fun interpolateText(text: String): String {
        var interpolatedText = text.replace("\$player", game.globals.save!!.globalNpcList.getNpc(0)!!.unitInstance.nickname)
        variables.forEach { variable, value -> interpolatedText = interpolatedText.replace("\$$variable", value) }
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
            "jmp" -> {
                currentIndex = -1
                scene = args[0]
                finishLine()
            }
            "saveedit" -> {
                when (args[0]) {
                    "guildname" -> {
                        game.globals.save!!.changePlayerGuildName(interpolateText(args[1]))
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

}
