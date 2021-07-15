package crypto.algs

import crypto.model.GF
import crypto.model.RoPollardRow
import java.util.*

/**
 * Created by Francesco Saverio Cannizzaro (fcannizzaro)
 */
object RoPollard {

    private fun calc(n: Long, complete: Boolean, x0: Long): Triple<List<RoPollardRow>, Long, Boolean> {

        val K = GF(n)
        val list = mutableListOf<RoPollardRow>()
        var factor = n
        var i = 0

        K.compute {

            val x1 = x0 * x0 + 1
            val x2 = x1 * x1 + 1
            list += RoPollardRow(x1.modular, x2.modular, 1L)

            while (i++ < 2000) {
                val (xi, x2i, _) = list.last()
                if (!complete) list.clear()
                val med = x2i * x2i + 1
                val newXi = xi * xi + 1
                val newX2i = med * med + 1
                val mcd = mcd(Math.abs(newX2i.value - newXi.value), n)
                val new = RoPollardRow(newXi, newX2i, mcd)
                list += new
                if (mcd != 1L) {
                    factor = mcd
                    break
                }
            }

        }

        return Triple(list, factor, factor == n)

    }

    fun all(n: Long, complete: Boolean, x0: Long = 1): List<Long> = full(n, complete).second

    fun full(n: Long, complete: Boolean, x0: Long = 1): Pair<List<List<RoPollardRow>>, List<Long>> {

        val results = mutableListOf<Long>()
        val tables = mutableListOf<List<RoPollardRow>>()

        val stack = Stack<Long>()
        stack += n

        while (stack.isNotEmpty()) {

            val v = stack.pop()

            if (v == 1L) continue

            if (v % 2L == 0L) {
                results += 2
                stack += v / 2
                continue
            }

            val (table, factor, prime) = calc(v, complete, x0)

            results += factor

            if (!prime) {
                tables += table
                stack += v / factor
            }

        }

        return Pair(tables, results)

    }

}