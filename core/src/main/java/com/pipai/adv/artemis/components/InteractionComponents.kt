package com.pipai.adv.artemis.components

import com.artemis.Component
import com.badlogic.gdx.Screen

class InteractionComponent : Component() {
    val interactionList: MutableList<Interaction> = mutableListOf()
}

sealed class Interaction {
    data class TextInteraction(val text: String): Interaction()
    data class CallbackInteraction(val callback: () -> Unit): Interaction()
    data class ScreenChangeInteraction(val screenGenerator: () -> Screen): Interaction()
}
