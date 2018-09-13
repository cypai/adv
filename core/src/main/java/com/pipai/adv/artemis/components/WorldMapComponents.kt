package com.pipai.adv.artemis.components

import com.artemis.Component
import com.badlogic.gdx.Screen

class SquadComponent : Component() {
    lateinit var squad: String
}

class PointOfInterestComponent : Component() {
    lateinit var name: String
    lateinit var screenCallback: () -> Screen
}
