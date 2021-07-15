package crypto.algs

import crypto.THEORY_ON
import crypto.wolfram

/**
 * Created by Francesco Saverio Cannizzaro (fcannizzaro)
 */
class HyperElliptic(block: HyperElliptic.() -> Unit) {

    init {
        println("\n------------------------------------------------")
        println("          ESERCIZIO Curve Ellittiche")
        println("------------------------------------------------")
        block(this)
    }

    private var points = emptyList<Pair<Long, Long>>()

    var GF: Long = 0

    var CURVE: String = ""

    var CUBIC: String = ""

    fun point(x: Long, y: Long): Pair<Long, Long> = Pair(x, y)

    fun step1(vararg a: Long) {
        CUBIC = a.mapIndexed { index, a -> "${a}x^$index" }.filterNot { it.startsWith("0") }.joinToString(" + ")
        val step = "expand ($CUBIC)^2 = $CURVE mod $GF"
        println(step)
        step.wolfram()
    }

    fun step2(step2: String) {
        val div = points.map { "(x-${it.first})" }.joinToString("")
        val step = "expand (${step2.replace("[ ]+", "")})/($div) mod $GF"
        println(step)
        step.wolfram()
    }

    fun step3(step3: String) {
        val step = "${step3.replace("[ ]+", "")} = 0 mod $GF"
        println(step)
        step.wolfram()
    }

    fun step4() {
        if (THEORY_ON) {
            println("\n\tSe si ottengono soluzioni:")
            println("\t => si sostituiscono le x nella cubica ($CUBIC) e ottengo le y.")
            println("\n\tSe il Δ non è un quadrato: [range(0,$GF)^2 mod $GF]")
            "range(0,$GF)^2 mod $GF]".wolfram("\t", "\n")
            println("\t => sia j = √Δ, si calcolano le soluzioni in x e si sostituiscono nella cubica ($CUBIC).")
            println("\t => si calcola la richiesta iniziale poichè i punti trovati sono -X₃ e si appattano le Ω.")
        }
    }

    fun step5(vararg x: Long) {
        x.forEach {
            println("\n\tSOLUZIONE PER ($it,y) ")
            val step = CUBIC.replace("x", "*$it")
            println("\t" + step)
            step.wolfram("\t")
        }
    }

    fun points(vararg points: Pair<Long, Long>) {
        this.points = points.toList()
        val matrix = points.map { "{1,${it.first},${it.first}^2,${it.first}^3}" }.joinToString(",")
        val vector = points.map { it.second }.joinToString(",")
        val step = "expand {$matrix}^-1 * {$vector} mod $GF"
        println(step)
        step.wolfram()
    }

}