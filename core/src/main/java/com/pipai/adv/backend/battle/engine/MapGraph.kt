package com.pipai.adv.backend.battle.engine

import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.domain.GridPosition
import com.pipai.adv.utils.MathUtils
import com.pipai.adv.utils.getLogger
import java.util.*
import kotlin.math.pow

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
            val bound = mobilitySegment * i
            moveBounds[i - 1] = bound
            if (debug) {
                logger.debug("Move bound @ AP $i: $bound")
            }
        }
    }

    private fun initializeMap() {
        val startX = Math.max(0, start.x - mobility)
        val startY = Math.max(0, start.y - mobility)
        val endX = Math.min(start.x + mobility, map.width - 1)
        val endY = Math.min(start.y + mobility, map.height - 1)
        for (x in startX..endX) {
            for (y in startY..endY) {
                val cellPos = GridPosition(x, y)
                if (map.getCell(cellPos).fullEnvObject == null || cellPos == start) {
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
                    val ssw = getNode(GridPosition(x - 1, y - 2))
                    if (ssw != null && south != null && sw != null) {
                        ssw.addEdge(cell)
                        cell.addEdge(ssw)
                    }
                    val wsw = getNode(GridPosition(x - 2, y - 1))
                    if (wsw != null && west != null && sw != null) {
                        wsw.addEdge(cell)
                        cell.addEdge(wsw)
                    }
                    val north = getNode(GridPosition(x, y + 1))
                    val nnw = getNode(GridPosition(x - 1, y + 2))
                    if (nnw != null && north != null && nw != null) {
                        nnw.addEdge(cell)
                        cell.addEdge(nnw)
                    }
                    val wnw = getNode(GridPosition(x - 2, y + 1))
                    if (wnw != null && west != null && nw != null) {
                        wnw.addEdge(cell)
                        cell.addEdge(wnw)
                    }
                    if (x == start.x && y == start.y) {
                        root = cell
                    }
                }
            }
        }
    }

    private fun getNode(pos: GridPosition): Node? {
        return nodeMap[pos.toString()]
    }

    private fun apRequiredToMoveTo(destination: Node): Int? {
        val delta = 0.000001f
        return (1..moveBounds.size).firstOrNull { destination.totalCost - delta <= moveBounds[it - 1] }
    }

    fun apRequiredToMoveTo(destination: GridPosition): Int {
        return getNode(destination)!!.apNeeded
    }

    private fun runDijkstra(maxMove: Float) {
        root.totalCost = 0.0
        val pqueue = PriorityQueue(maxMove.pow(2).toInt(), NodeComparator())
        pqueue.addAll(nodeMap.values)

        while (pqueue.isNotEmpty()) {
            val current = pqueue.poll()
            current.visit()
            if (debug) {
                logger.debug("Current $current")
            }
            if (current != root) {
                val apNeeded = apRequiredToMoveTo(current) ?: return
                val index = apNeeded - 1
                val reachableList = reachableLists[index]
                reachableList.add(current.position)
                current.apNeeded = apNeeded
            }
            for (edge in current.edges) {
                if (!edge.destination.isVisited) {
                    val altCost = current.totalCost + edge.cost
                    if (altCost < edge.destination.totalCost) {
                        edge.destination.totalCost = altCost
                        edge.destination.path = current
                        pqueue.remove(edge.destination)
                        pqueue.add(edge.destination)
                    }
                }
            }
        }
    }

    fun getMovableCellPositions(ap: Int): List<GridPosition> {
        var list: List<GridPosition>
        try {
            list = reachableLists[ap - 1]
        } catch (e: IndexOutOfBoundsException) {
            list = listOf()
        }
        return list.toList()
    }

    fun getPath(destinationPos: GridPosition): LinkedList<GridPosition> {
        val node = getNode(destinationPos)
        if (node == null || !node.isVisited) {
            return LinkedList()
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
        return node?.isVisited ?: false
    }

    fun startingPosition(): GridPosition {
        return root.position
    }

    private data class Edge internal constructor(val destination: Node, val cost: Double)

    private inner class Node internal constructor(val position: GridPosition) {
        val edges: ArrayList<Edge> = ArrayList<Edge>()
        var isVisited: Boolean = false
        var totalCost: Double = Double.MAX_VALUE
        var path: Node? = null
        var apNeeded: Int = 0

        init {
            apNeeded = Integer.MAX_VALUE
        }

        fun addEdge(node: Node) {
            edges.add(Edge(node, MathUtils.distance(position.x, position.y, node.position.x, node.position.y)))
        }

        fun visit() {
            isVisited = true
        }

        override fun toString(): String {
            var s = "Node: $position Cost $totalCost Edges [ "
            edges.map { it.destination }
                    .forEach { s += "{" + it.position + " " + it.isVisited + "} " }
            s += "]"
            return s
        }
    }

    private inner class NodeComparator : Comparator<Node> {
        override fun compare(a: Node, b: Node): Int {
            if (a.totalCost > b.totalCost) {
                return 1
            } else if (a.totalCost < b.totalCost) {
                return -1
            }
            return 0
        }
    }
}
