package com.pipai.adv.artemis.components

import com.artemis.Component

class PartialTextComponent : Component() {

    var timer: Int = 0
    var timerSlowness: Int = 0
    var fullText: String = ""
    var currentText: String = ""
    var textUpdateRate: Int = 0

    fun setToText(text: String) {
        timer = timerSlowness;
        fullText = text;
        currentText = "";
    }

}
