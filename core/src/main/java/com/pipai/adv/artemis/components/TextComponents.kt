package com.pipai.adv.artemis.components

import com.artemis.Component

class TextComponent : Component() {
    var text = ""
}

class PartialTextComponent : Component() {

    var timer: Int = 0
    var timerSlowness: Int = 1
    var fullText: String = ""
    var currentText: String = ""
    var textUpdateRate: Int = 1

    fun setToText(text: String) {
        timer = timerSlowness;
        fullText = text;
        currentText = "";
    }

}

class MultipleTextComponent : Component() {
    val textList: MutableList<String> = mutableListOf()
}
