package com.github.maximvegorov.theory.of.graphs.menger

fun main() {
    val nodes = readln().split(' ')
    val start = nodes[0]
    val end = nodes[1]
    val used = mutableSetOf(start)
    val startNeighborhood = mutableListOf<String>()
    for (i in start.indices) {
        val s = flipBit(start, i)
        if (s != end) {
            used.add(s)
        }
        startNeighborhood.add(s)
    }
    for (s in startNeighborhood) {
        val path = mutableListOf(start, s)
        var current = s
        nextVertex@ while (current != end) {
            val dr = findDiff(current, end)
            for (list in listOf(dr.diff, dr.same)) {
                var next: String? = null
                for (i in list) {
                    val candidate = flipBit(current, i)
                    if (candidate in used) {
                        continue
                    }
                    next = candidate
                    break
                }
                if (next != null) {
                    current = next
                    if (current != end) {
                        used.add(current)
                    }
                    path.add(current)
                    continue@nextVertex
                }
            }
            throw AssertionError("Should not be reached")
        }
        println(path.joinToString(" "))
    }
}

data class DiffResult(val same: List<Int>, val diff: List<Int>)

fun findDiff(n: String, end: String): DiffResult {
    val same = mutableListOf<Int>()
    val diff = mutableListOf<Int>()
    for (i in n.indices) {
        if (n[i] == end[i]) {
            same.add(i)
        } else {
            diff.add(i)
        }
    }
    return DiffResult(same, diff)
}

fun flipBit(s: String, k: Int): String {
    val builder = StringBuilder(s.length)
    for (i in 0 until k) {
        builder.append(s[i])
    }
    builder.append((1 - (s[k] - '0') + '0'.code).toChar())
    for (i in k + 1 until s.length) {
        builder.append(s[i])
    }
    return builder.toString()
}
