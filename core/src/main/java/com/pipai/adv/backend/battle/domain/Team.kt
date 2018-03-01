package com.pipai.adv.backend.battle.domain

enum class Team {
    AI, PLAYER;

    companion object {
        fun opposingTeam(team: Team): Team {
            return when (team) {
                Team.AI -> Team.PLAYER
                Team.PLAYER -> Team.AI
            }
        }
    }
}
