package com.pipai.adv.domain

import com.badlogic.gdx.files.FileHandle

data class Cutscene(val scenes: Map<String, List<CutsceneLine>>) {
    fun prettyPrint() {
        for (scene in scenes.entries) {
            println(scene.key)
            scene.value.forEach { println(it) }
            println()
        }
    }
}

data class CutsceneLine(val type: CutsceneLineType, val speaker: String?, val text: String?, val command: String?, val args: List<String>?)

enum class CutsceneLineType {
    TEXT, COMMAND
}

object CutsceneUtils {
    fun loadCutscene(fileHandle: FileHandle): Cutscene {
        val scenes: MutableMap<String, List<CutsceneLine>> = mutableMapOf()
        val lines = fileHandle.readString().split("\n")
        var label = "start"
        var scene: MutableList<CutsceneLine> = mutableListOf()
        for (untrimmedLine in lines) {
            val line = untrimmedLine.trim()
            if (line.isBlank() || line[0] == '#') {
                continue
            }
            if (line[0] == '@') {
                if (scene.isNotEmpty()) {
                    scenes[label] = scene
                }
                label = line.substring(1)
                scene = mutableListOf()
            } else {
                if (line[0] == '!') {
                    val commandDelimitIndex = line.indexOf(' ')
                    val command = line.substring(1, commandDelimitIndex)
                    var argsUnparsed = line.substring(commandDelimitIndex + 1)
                    val args: MutableList<String> = mutableListOf()
                    while (argsUnparsed.isNotBlank()) {
                        if (argsUnparsed[0] == '"') {
                            var enclosingIndex = argsUnparsed.indexOf('"', 1)
                            if (enclosingIndex < 0) enclosingIndex = argsUnparsed.length
                            args.add(argsUnparsed.substring(1, enclosingIndex))
                            if (enclosingIndex + 2 >= argsUnparsed.length) break
                            argsUnparsed = argsUnparsed.substring(enclosingIndex + 2)
                        } else {
                            var nextIndex = argsUnparsed.indexOf(' ')
                            if (nextIndex < 0) nextIndex = argsUnparsed.length
                            args.add(argsUnparsed.substring(0, nextIndex))
                            if (nextIndex + 1 >= argsUnparsed.length) break
                            argsUnparsed = argsUnparsed.substring(nextIndex + 1)
                        }
                    }
                    scene.add(CutsceneLine(CutsceneLineType.COMMAND, null, null, command, args))
                } else {
                    if (line.contains('|')) {
                        val splitTextLine = line.split('|')
                        scene.add(CutsceneLine(CutsceneLineType.TEXT, splitTextLine[0], splitTextLine[1], null, null))
                    } else {
                        throw IllegalStateException("Line could not be parsed: $line")
                    }
                }
            }
        }
        if (scene.isNotEmpty()) {
            scenes[label] = scene
        }
        return Cutscene(scenes)
    }
}
