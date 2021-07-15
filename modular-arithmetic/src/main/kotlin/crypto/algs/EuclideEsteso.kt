package crypto.algs

import crypto.model.EEAEquation
import crypto.model.Euclide
import crypto.sub

/*
*    Francesco Saverio Cannizzaro (fcannizzaro)
*/
fun euclideEsteso(vararg values: Long): Euclide {

    val r = values.sortedDescending().toMutableList()
    val s = mutableListOf(1L, 0L)
    val t = mutableListOf(0L, 1L)

    val eqs = mutableListOf<EEAEquation>()

    (0..1).forEach {
        eqs += EEAEquation(
                "r${it.sub()} = ${r[it]}",
                "s${it.sub()} = ${s[it]}",
                "t${it.sub()} = ${t[it]}"
        )
    }

    while (r.last() != 0L) {
        val i0 = r.lastIndex - 1
        val i1 = r.lastIndex
        val q = r[i0] / r[i1]
        r += -q * r[i1] + r[i0]
        s += -q * s[i1] + s[i0]
        t += -q * t[i1] + t[i0]
        eqs += EEAEquation(
                "r${r.lastIndex.sub()} = -$q * ${r[i1]} + ${r[i0]} = ${r.last()}",
                "s${r.lastIndex.sub()} = -$q * ${s[i1]} + ${s[i0]} =  ${s.last()}",
                "t${r.lastIndex.sub()} = -$q * ${t[i1]} + ${t[i0]} = ${t.last()} "
        )
    }

    eqs += EEAEquation(
            "r= ${r[r.lastIndex - 1]}",
            "s = ${s[s.lastIndex - 1]}",
            "t = ${t[t.lastIndex - 1]}",
            r[r.lastIndex - 1],
            s[s.lastIndex - 1],
            t[t.lastIndex - 1]
    )

    return Euclide(r[r.lastIndex - 1], s[s.lastIndex - 1], t[t.lastIndex - 1], eqs)

}