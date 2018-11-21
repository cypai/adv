package com.pipai.adv.artemis.components

import com.artemis.Component
import com.pipai.adv.map.PointOfInterest

class SquadComponent : Component() {
    lateinit var squad: String
}

class PointOfInterestComponent : Component() {
    lateinit var poi: PointOfInterest
}
