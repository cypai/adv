package com.pipai.adv.backend.battle.engine

import java.util.ArrayList
import java.util.Comparator
import java.util.HashMap
import java.util.LinkedList
import java.util.PriorityQueue
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.pipai.utils.getLogger
import com.pipai.adv.backend.battle.utils.distance
import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.GridPosition

/*
* To be used as a disposable BattleMap representation for Dijkstra's and other pathfinding algorithms
*/
class MapGraph(val map: BattleMap, val start: GridPosition, val mobility: Int, val ap: Int, val apMax: Int, val debug: Boolean = false) {

    private val logger = getLogger()

    lateinit private var root: Node

    private val nodeMap: MutableMap<String, Node> = mutableMapOf()
    private val moveBounds: FloatArray = FloatArray(ap)
    private val reachableLists: MutableList<MutableList<GridPosition>> = mutableListOf()

    init {
        if (ap > 0) {
            for (i in 0..ap - 1) {
                reachableLists.add(mutableListOf())
            }
            calculateApMoveBounds()
            initializeMap()
            runDijkstra(moveBounds[moveBounds.size - 1])
        }
    }

    private fun calculateApMoveBounds() {
        val mobilitySegment = (mobility.toFloat()) / (apMax.toFloat())
        for (i in 1..ap) {
            moveBounds[i - 1] = mobilitySegment * i
        }
    }

    private fun initializeMap() {
        val width = map.width
        val height = map.height
        for (x in 0 until width) {
            for (y in 0 until height) {
                val cellPos = GridPosition(x, y)
                if (map.getCell(cellPos).fullEnvironmentObject == null || cellPos.equals(start)) {
                    val cell = Node(cellPos)
                    nodeMap.put(cellPos.toString(), cell)
                    val west = getNode(GridPosition(x - 1, y))
                    if (west != null) {
                        west.addEdge(cell)
                        cell.addEdge(west)
                    }
                    val south = getNode(GridPosition(x, y - 1))
                    if (south != null) {
                        south.addEdge(cell)
                        cell.addEdge(south)
                    }
                    val sw = getNode(GridPosition(x - 1, y - 1))
                    if (sw != null) {
                        sw.addEdge(cell)
                        cell.addEdge(sw)
                    }
                    val nw = getNode(GridPosition(x - 1, y + 1))
                    if (nw != null) {
                        nw.addEdge(cell)
                        cell.addEdge(nw)
                    }
                    if (x == start.x && y == start.y) {
                        root = cell
                    }
                }
            }
        }
    }

    private fun getNode(pos: GridPosition): Node? {
        return nodeMap.get(pos.toString())
    }

    private fun apRequiredToMoveTo(destination: Node): Int {
        val delta = 0.000001f
        for (i in 1..moveBounds.size) {
            if (destination.totalCost - delta <= moveBounds[i - 1]) {
                return i
            }
        }
        return Integer.MAX_VALUE
    }

    fun apRequiredToMoveTo(destination: GridPosition): Int {
        return getNode(destination)!!.apNeeded
    }

    private fun runDijkstra(maxMove: Float) {
        val pqueue = PriorityQueue((maxMove * maxMove).toInt(), NodeComparator())
        var current: Node? = root
        while (current != null) {
            if (!current.position.equals(root.position)) {
                val apNeeded = apRequiredToMoveTo(current)
                val index = apNeeded - 1
                var reachableList = reachableLists.get(index)
                reachableList.add(current.position)
                current.apNeeded = apNeeded
            }
            current.visit()
            if (debug) {
                logger.debug("Current " + current)
            }
            for (edge in current.edges) {
                val node = edge.destination
                if (debug) {
                    logger.debug("Checking " + node.position)
                }
                if (!node.isVisited && !node.isAdded) {
                    val totalCost = edge.cost + current.totalCost
                    if (totalCost <= maxMove) {
                        if (debug) {
                            logger.debug("Added " + node.position + " Dist " + totalCost)
                        }
                        node.setAdded()
                        node.totalCost = totalCost
                        node.path = current
                        pqueue.add(node)
                    }
                }
            }
            current = pqueue.poll()
        }
    }

    fun getMovableCellPositions(ap: Int): List<GridPosition> {
        var list: List<GridPosition>
        try {
            list = reachableLists.get(ap - 1)
        } catch (e: IndexOutOfBoundsException) {
            list = listOf()
        }
        return list.toList()
    }

    fun getPath(destinationPos: GridPosition): LinkedList<GridPosition> {
        val node = getNode(destinationPos)
        if (node == null || !node.isVisited) {
            return LinkedList<GridPosition>()
        }
        val pathList = LinkedList<GridPosition>()
        var path = getNode(destinationPos)
        while (path != null) {
            pathList.addFirst(path.position)
            path = path.path
        }
        return pathList
    }

    fun canMoveTo(pos: GridPosition): Boolean {
        val node = getNode(pos)
        return if (node == null) false else node.isVisited
    }

    fun startingPosition(): GridPosition {
        return root.position
    }

    private data class Edge internal constructor(val destination: Node, val cost: Double)

    private inner class Node internal constructor(val position: GridPosition) {
        val edges: ArrayList<Edge>
        var isAdded: Boolean = false
        var isVisited: Boolean = false
        var totalCost: Double = 0.0
        var path: Node? = null
        var apNeeded: Int = 0

        init {
            edges = ArrayList<Edge>()
            apNeeded = Integer.MAX_VALUE
        }

        fun addEdge(node: Node) {
            edges.add(Edge(node, distance(position, node.position)))
        }

        fun visit() {
            isVisited = true
        }

        fun setAdded() {
            isAdded = true
        }

        override fun toString(): String {
            var s = "Node: " + position + " Edges [ "
            for (edge in edges) {
                val node = edge.destination
                s += "{" + node.position + " " + node.isVisited + " " + node.isAdded + "} "
            }
            s += "]"
            return s
        }
    }

    private inner class NodeComparator : Comparator<Node> {
        override fun compare(x: Node, y: Node): Int {
            if (x.totalCost > y.totalCost) {
                return 1
            } else if (x.totalCost < y.totalCost) {
                return -1
            }
            return 0
        }
    }
}
