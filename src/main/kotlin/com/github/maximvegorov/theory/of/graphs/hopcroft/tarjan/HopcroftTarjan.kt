package com.github.maximvegorov.theory.of.graphs.hopcroft.tarjan

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets

data class Edge(val start: Int, val end: Int)

data class Graph(val adjacencyList: Map<Int, List<Edge>>)

object GraphReader {
    fun read(reader: Reader): Graph {
        return reader.useLines { lines -> lines.filter { it.isNotEmpty() }
            .map { it.split(" ") }
            .map { Edge(it[0].toInt(), it[1].toInt()) }
            .flatMap { edge -> listOf(edge.start to edge, edge.end to edge) }
            .groupBy(keySelector={ it.first }, valueTransform = { it.second })
            .run { Graph(this) }
        }
    }
}

class ArticulationPointsFinder(private val graph: Graph) {
    private val nodes = mutableMapOf<Int, SpanningTreeNode>()
    private val visited = mutableSetOf<Edge>()
    private val points = mutableSetOf<Int>()
    private var k = 0

    fun find(): Set<Int> {
        val root = buildTree(graph.adjacencyList.keys.first())
        if (root.children.size == 1) {
            points.remove(root.id)
        }
        return points
    }

    private fun buildTree(id: Int): SpanningTreeNode {
        val root = SpanningTreeNode(id, nextK())
        nodes[root.id] = root
        val adjacent = graph.adjacencyList.getOrDefault(id, listOf())
        for (edge in adjacent) {
            if (!visited.add(edge)) {
                continue
            }
            val targetId = if (edge.start == id) {
                edge.end
            } else {
                edge.start
            }
            val parent = nodes[targetId]
            if (parent == null) {
                val child = buildTree(targetId)
                root.children.add(child)
                if (child.l >= root.k) {
                    points.add(root.id)
                }
                root.l = minOf(root.l, child.l)
            } else {
                root.l = minOf(root.l, parent.k)
            }
        }
        return root
    }

    private fun nextK(): Int {
        return k++
    }

    data class SpanningTreeNode(
        val id: Int,
        val k: Int,
        var l: Int = k,
        val children: MutableList<SpanningTreeNode> = mutableListOf(),
    )
}

fun main() {
    val reader = BufferedReader(InputStreamReader(System.`in`, StandardCharsets.UTF_8))

    val graph = GraphReader.read(reader)

    val points = ArticulationPointsFinder(graph)
        .find()
        .sorted()

    println(points.joinToString(" "))
}
