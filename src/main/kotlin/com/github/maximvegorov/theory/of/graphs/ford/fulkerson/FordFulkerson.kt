package com.github.maximvegorov.theory.of.graphs.ford.fulkerson

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.util.Scanner

data class Edge(val start: Int, val end: Int)

data class Network(
    val adjacencyList: Map<Int, List<Edge>>,
    val source: Int,
    val sink: Int,
    val maxCaps: Map<Edge, Int>,
)

object NetworkReader {
    fun read(reader: Reader): Network {
        val scanner = Scanner(reader)
        val vertexCount = scanner.nextInt()
        val edgeCount = scanner.nextInt()
        val adjacencyList = mutableMapOf<Int, MutableList<Edge>>()
        val maxCaps = mutableMapOf<Edge, Int>()
        for (i in 1..edgeCount) {
            val edge = Edge(scanner.nextInt(), scanner.nextInt())
            val maxCap = scanner.nextInt()
            adjacencyList.computeIfAbsent(edge.start) { mutableListOf() }.add(edge)
            adjacencyList.computeIfAbsent(edge.end) { mutableListOf() }.add(edge)
            maxCaps[edge] = maxCap
        }
        return Network(adjacencyList, 0, vertexCount - 1, maxCaps)
    }
}

data class EdgeFlow(var f: Int)

data class Flow(val edges: Map<Edge, EdgeFlow>, var q: Int) {
    fun changeBy(edge: Edge, cap: Int) {
        edges[edge]!!.f += cap
    }

    operator fun get(edge: Edge): Int = edges[edge]!!.f

    companion object {
        fun zero(network: Network): Flow {
            val edges = network.maxCaps.mapValues { EdgeFlow(0) }
            return Flow(edges, 0)
        }
    }
}

class MinCutMaxFlowFinder(private val network: Network) {
    private val flow = Flow.zero(network)

    fun findMaxFlow(): Flow {
        var path = findUnsaturatedPath()
        while (path != null) {
            path.increaseFlow(flow)
            path = findUnsaturatedPath()
        }
        return flow
    }

    private fun findUnsaturatedPath(): UnsaturatedPath? {
        val head: PathElem? = null
        var minCap = Int.MAX_VALUE
        val queue = ArrayDeque(listOf(network.source))
        val visited = mutableMapOf<Int, PathElem>()
        while (!queue.isEmpty()) {
            val node = queue.removeFirst()
            for (edge in network.adjacencyList.getOrDefault(node, listOf())) {
                val cap = flow[edge]
                val maxCap = network.maxCaps[edge]!!
                val reverse = edge.start != node
                var start: Int
                var end: Int
                if (!reverse) {
                    start = edge.start
                    end = edge.end
                    if (cap == maxCap) {
                        continue
                    }
                    minCap = minOf(minCap, maxCap - cap)
                } else {
                    start = edge.end
                    end = edge.start
                    if (cap == 0) {
                        continue
                    }
                    minCap = minOf(minCap, cap)
                }
                if (end in visited) {
                    continue
                }
                val elem = PathElem(edge, reverse, visited[start])
                if (end == network.sink) {
                    return UnsaturatedPath(elem, minCap)
                }
                visited[end] = elem
                queue.addLast(end)
            }
        }
        return null
    }

    data class UnsaturatedPath(val head: PathElem, val cap: Int) {
        fun increaseFlow(flow: Flow) {
            var elem: PathElem? = head
            while (elem != null) {
                if (!elem.reverse) {
                    flow.changeBy(elem.edge, cap)
                } else {
                    flow.changeBy(elem.edge, -cap)
                }
                elem = elem.prev
            }
            flow.q += cap
        }
    }

    data class PathElem(val edge: Edge, val reverse: Boolean, val prev: PathElem?)
}

fun main() {
    val network = NetworkReader.read(BufferedReader(InputStreamReader(System.`in`)))

    val maxFlow = MinCutMaxFlowFinder(network)
        .findMaxFlow()

    println(maxFlow.q)
}
