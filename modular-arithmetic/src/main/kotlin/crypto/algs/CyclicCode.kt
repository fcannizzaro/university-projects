package crypto.algs

import crypto.*
import crypto.model.GF
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction

/*
*    Francesco Saverio Cannizzaro (fcannizzaro)
*/
class CyclicCode(block: CyclicCode.() -> Unit) {

    init {
        block(this)
        calc()
    }

    lateinit var gx: PolynomialFunction

    lateinit var K: GF

    fun gx(vararg polinomio: Int) {
        gx = pol(*polinomio.map { it.double }.toDoubleArray())
    }

    fun gf(p: Long) {
        K = GF(p)
    }

    private fun calc() {

        K.compute {

            val deg = gx.degree()
            val max = pol(deg + 1, deg to 1)
            val result = -(gx - max)
            val j = pol(0.0, 1.0)

            if (THEORY_ON)
                println("\n\tPoiché C è generato da g(x) allora la matrice generatrice G = <g(x)>. le dimensioni della matrice G" +
                        "\n\tnon sono state definite ma è solo noto il polinomio generatore:" +
                        "\n\n\tg(x) = $gx, δg(x) = $deg\n" +
                        "\n\tPer determinare il valore di n è possibile notare che g(x) è un polinomio" +
                        "\n\tdivisibile per x^n - 1 e quindi deve valere che x^n - 1 = g(x) * h(x)" +
                        "\n\tPer il teorema di Ruffini è possibile affermare che se g(x) divide x^n - 1 allora" +
                        "\n\ttutte le radici di g(x) sono anche radici per x^n - 1." +
                        "\n\n\tSi definisca allora α come radice \"simbolica\" di g(x), ovvero tale che g(α) = 0." +
                        "\n\tSe α è radice (simbolica) di g(x) allora α^n - 1 = g(α) * h(α) = 0")
            else
                println("\n\tgx = $gx")

            var tmp = result
            var n = deg

            if (THEORY_ON)
                println("\n\t - CALCOLO DELLE POTENZE α^i per trovare \"n\"")

            while (!tmp.isOne()) {
                if (tmp.size > 0) println("\n\t   α^$n\t = ${tmp.str}")
                if (tmp.degree() >= deg) {
                    val term = tmp[deg].toInt()
                    if (term != 0) {
                        tmp -= pol(deg + 1, deg to term).fix()
                        tmp += (pol(term.double) * result).fix()
                    }
                    println("\t\t     = ${tmp.str}\n")
                }
                tmp = if (!tmp.isOne()) (tmp * j).fix() else break
                n++
            }

            if (THEORY_ON)
                println("\t - TROVATO n = $n\n\t   allora il polinomio g(x) divide tutti i polinomi nella forma x^($n*j) - 1 con j = 1, 2, .. poichè sono\n\t   infiniti questi polinomi prendiamo quello di grado minimo ovvero grado $n. (Codice Minimale)\n\t   Si trova il polinomio h(x) tale che x^$n - 1 = g(x) * h(x):\n")

            val xn = pol(n + 1, 0 to -1, n to 1).fix()

            val num = "($xn)"
            val den = "($gx)"
            val sep = "-" * den.length
            val space = " " * ((den.length - num.length) / 2)

            val hx = xn.div(gx, K).fix()

            println("\t $space$num\n\t $sep  = $hx\n\t $den")

            val original = DoubleArray(n) { 0.0 }
            val coefficients = DoubleArray(n) { 0.0 }
            hx.coefficients.forEachIndexed { i, d -> original[i] = d }
            hx.coefficients.forEachIndexed { i, d -> coefficients[n - i - 1] = d }
            val hTilde = pol(*coefficients)

            if (THEORY_ON)
                println("\n\t - OTTENUTO h(x) = $hx, per determinare la matrice di parità H di C occorre determinare il polinomio ~h(x)" +
                        "\n\t   reciproco di h(x), equivalente a (${original.map { it.int }.joinToString(",")}) e " +
                        "quindi ~h(x) = (${coefficients.map { it.int }.joinToString(",")}) ≡ $hTilde")
            else
                println("\n\th(x) = $hTilde")

            if (THEORY_ON)
                println("\n\t Allora H = <~h(x)> e C è un [$n, ${n - deg}, d]${K.p.sub()} codice ciclico con k = n - δg(x)")

        }

    }

}