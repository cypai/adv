package com.pipai.adv.ai

import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.ai.msg.Telegram
import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.backend.battle.engine.ActionPointState
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.commands.*
import com.pipai.adv.backend.battle.engine.rules.execution.AttackCalculationExecutionRule
import com.pipai.adv.backend.battle.utils.BattleUtils
import com.pipai.adv.utils.GridUtils
import com.pipai.adv.utils.RNG
import com.pipai.adv.utils.getLogger
import kotlin.math.roundToInt

class SimpleAi(private val backend: BattleBackend, private val npcId: Int) {

    private val logger = getLogger()

    private var stateMachine = DefaultStateMachine<SimpleAi, SimpleAiState>(this)

    private val moveCommandFactory = MoveCommandFactory(backend)
    private val attackCommandFactory = NormalAttackCommandFactory(backend)

    private val attackCalculator = AttackCalculationExecutionRule()

    init {
        stateMachine.changeState(SimpleAiState.WANDERING)
    }

    fun generateCommand(): BattleCommand {
        stateMachine.update()
        val npc = backend.getNpc(npcId)!!
        logger.debug("${npc.unitInstance.nickname} AI state: ${stateMachine.currentState}")
        return when (stateMachine.currentState) {
            SimpleAiState.WANDERING -> generateWanderingCommand()
            SimpleAiState.ALERT -> generateAlertCommand()
            SimpleAiState.ATTACKING -> generateAttackingCommand()
            SimpleAiState.FLEEING -> generateFleeingCommand()
            else -> generateWanderingCommand()
        }
    }

    private fun generateWanderingCommand(): BattleCommand {
        return if (backend.getNpcAp(npcId) == ActionPointState.startingNumAPs) {
            val commands = moveCommandFactory.generate(npcId)
            val commandIndex = RNG.nextInt(commands.size)
            commands[commandIndex]
        } else {
            WaitCommand(npcId)
        }
    }

    private fun generateAlertCommand(): BattleCommand {
        val commands = moveCommandFactory.generate(npcId, 1)
        val commandIndex = RNG.nextInt(commands.size)
        return commands[commandIndex]
    }

    private fun generateAttackingCommand(): BattleCommand {
        val scoredCommands: MutableList<Pair<Int, BattleCommand>> = mutableListOf()
        scoredCommands.addAll(generateScoredAttackCommands(backend.getNpcPosition(npcId)!!))
        val scoredMoveCommands = moveCommandFactory.generate(npcId, 1)
                .map {
                    val destination = it.path.last()
                    val bestAttack = generateScoredAttackCommands(destination)
                            .maxBy { it.first }
                    if (bestAttack == null) {
                        Pair(0, it)
                    } else {
                        Pair(bestAttack.first, it)
                    }
                }
        scoredCommands.addAll(scoredMoveCommands)
        return scoredCommands.sortedBy { it.first }.firstOrNull()?.second ?: DefendCommand(npcId)
    }

    private fun generateScoredAttackCommands(position: GridPosition): List<Pair<Int, BattleCommand>> {
        val commands = attackCommandFactory.generateForPosition(npcId, position)
        return commands.map {
            val preview = backend.preview(it)
            val toHit = attackCalculator.calculateToHit(preview)!!
            val damageRange = attackCalculator.calculateDamageRange(preview)!!
            val targetHp = backend.getNpc(it.targetId)!!.unitInstance.hp
            val koScoreBoost = if (targetHp < damageRange.second) 100 else 0
            Pair(toHit + koScoreBoost, it)
        }
    }

    private fun generateFleeingCommand(): BattleCommand {
        val moveCommands = moveCommandFactory.generate(npcId, 1)
        val scoredMoveCommands = moveCommands.map {
            val destination = it.path.last()
            val nearbyTeammates = BattleUtils.teammatesInRange(npcId, destination, backend, BattleBackend.AGGRO_DISTANCE2)
            val nearbyEnemies = BattleUtils.enemiesInRange(npcId, destination, backend, BattleBackend.AGGRO_DISTANCE2)
            val minEnemyDistance = nearbyEnemies.map { GridUtils.gridDistance(destination, backend.getNpcPosition(it)!!) }
                    .min()?.roundToInt()
            val teammateBoost = nearbyTeammates.size * 100
            val enemyBoost = nearbyEnemies.size * -50
            val distanceBoost = minEnemyDistance ?: 50
            Pair(teammateBoost + enemyBoost + distanceBoost, it)
        }
        return scoredMoveCommands.sortedBy { it.first }.firstOrNull()?.second ?: DefendCommand(npcId)
    }

    enum class SimpleAiState : State<SimpleAi> {
        WANDERING() {
            override fun update(entity: SimpleAi) {
                val enemiesInRange = BattleUtils.enemiesInRange(entity.npcId, entity.backend, BattleBackend.AGGRO_DISTANCE2)
                if (enemiesInRange.isNotEmpty()) {
                    entity.stateMachine.changeState(ATTACKING)
                }
            }
        },
        ALERT() {
            override fun update(entity: SimpleAi) {
                val enemiesInRange = BattleUtils.enemiesInRange(entity.npcId, entity.backend, BattleBackend.AGGRO_DISTANCE2)
                if (enemiesInRange.isNotEmpty()) {
                    entity.stateMachine.changeState(ATTACKING)
                }
            }
        },
        ATTACKING() {
            override fun update(entity: SimpleAi) {
                val enemiesInRange = BattleUtils.enemiesInRange(entity.npcId, entity.backend, BattleBackend.AGGRO_DISTANCE2)
                if (enemiesInRange.isEmpty()) {
                    entity.stateMachine.changeState(ALERT)
                }
                if (entity.backend.getNpc(entity.npcId)!!.unitInstance.hp <= 20) {
                    entity.stateMachine.changeState(FLEEING)
                }
            }
        },
        FLEEING() {
            override fun update(entity: SimpleAi) {
                val enemiesInRange = BattleUtils.enemiesInRange(entity.npcId, entity.backend, BattleBackend.AGGRO_DISTANCE2)
                if (enemiesInRange.isEmpty()) {
                    entity.stateMachine.changeState(ALERT)
                }
                val position = entity.backend.getNpcPosition(entity.npcId)!!
                val nearbyTeammates = BattleUtils.teammatesInRange(entity.npcId, position, entity.backend, BattleBackend.AGGRO_DISTANCE2)
                if (nearbyTeammates.isNotEmpty()) {
                    entity.stateMachine.changeState(ATTACKING)
                }
            }
        }
        ;

        override fun enter(entity: SimpleAi) {
        }

        override fun exit(entity: SimpleAi) {
        }

        override fun onMessage(entity: SimpleAi, telegram: Telegram): Boolean {
            return false
        }

        override fun update(entity: SimpleAi) {
        }
    }

}
