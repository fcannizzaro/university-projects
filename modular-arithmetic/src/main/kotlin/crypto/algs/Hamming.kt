package crypto.algs

import crypto.*
import crypto.model.GF
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction
import java.util.*

/**
 * Created by Francesco Saverio Cannizzaro (fcannizzaro)
 */
class Hamming(block: Hamming.() -> Unit) {

    private val combinations = mutableListOf<List<Int>>()
    lateinit var gx: PolynomialFunction

    init {
        block(this)
        calc()
    }

    lateinit var code: Triple<Long, Long, Long>

    var q = 0L

    fun code(n: Long, k: Long, d: Long, q: Long) {
        code = Triple(n, k, d)
        this.q = q
    }

    private fun calc() {

        val (n, k, d) = code
        val factors = factors(n + 1)
        val r = factors.size
        val p = factors[0]

        val gf = Math.pow(p.toDouble(), r.double).toInt()

        if (THEORY_ON)
            println("\n\tPer determinare tale polinomio è possibile ricordare che la matrice di parità di questo codice è" +
                    "\n\tottenuta disponendo in colonna tutti gli p^r - 1 vettori non nulli di GF(pr). In questo caso p = $p e r = $r, infatti n = $p^$r = $n." +
                    "\n\tPertanto, H appartiene a GF(2)^(n-k)*n sarà ottenuta disponendo tutti i vettori non nulli di GF($p^$r) = GF($gf)")

        permutations("", 4)

        val matrix = (0 until n - k).map { arrayListOf<Int>() }

        matrix.forEachIndexed { index, row ->
            combinations.forEach { line ->
                row.add(line[index])
            }
        }

        println("\n\t" + matrix.joinToString("\n\t"))

        if (::gx.isInitialized && THEORY_ON) {
            println("\n\tSi costruisca allora il campo GF($p^$r) utilizzando un polinomio di grado irriducibile di grado n - k = ${n - k} su GF($p}")
            println("\tIl polinomio irriducibile di grado ${n - k} su GF($p) è $gx. Si imponga che j sia una radice per tale polinomio, ovvero che g(j) = 0. Allora:\n")

            val y = gx
            val max = y.degree()
            val subst = -(y - pol(max + 1, max to y[max].int))

            val chars = "abcdefghilmno"
            val degExt = n - k - 1

            val ext = (0..degExt).mapIndexed { i, _ -> chars[i] + (if (i > 0) "j${if (i > 1) "^$i" else ""}" else "") }.joinToString(" + ")

            println("\tGF($gf) = { $ext , j^$max = $subst }" +
                    "\n\n\tOccorre quindi determinare un generatore per GF($gf)*. Per fare ciò bisogna ricordare che i polinomi che non sono generatori per GF(p)*" +
                    "\n\tallora generano un sotto-gruppo di dimensione pari ad un divisore di p - 1." +
                    "\n\tQuindi o δ è un polinomio in GF(p) che genera tutti gli p - 1 elementi del gruppo (e quindi δ = γ) oppure è una potenza del" +
                    "\n\tgeneratore δ = γ^d_i, con d_i un divisore di p - 1. In quest’ultimo caso δ genera di elementi di GF(p)*.")

            var generator: PolynomialFunction = pol(0.0)
            var generatorSteps: ArrayList<PolynomialFunction> = arrayListOf()

            combinations.clear()
            permutations("", max)

            GF(p) {

                combinations.forEach {

                    val y = pol(*it.toIntArray())
                    val steps = arrayListOf(y)
                    var isGenerator = true
                    var tmp = y

                    val tmpSteps = arrayListOf<String>()

                    tmpSteps.add("\n\tVerifica per ${tmp.y}:")
                    tmpSteps.add("\tγ^1 = ${tmp.y}")

                    (1 until gf - 1).map {

                        tmp = (tmp * y).fix()
                        val step = tmp.substitute(max, subst, this)
                        tmp = step.fix()
                        steps.add(step)

                        if (it < gf - 2 && tmp.isModulusOne()) {
                            isGenerator = false
                        }

                        if (it == gf - 2) {
                            isGenerator = tmp.isModulusOne() && isGenerator
                        }

                        tmpSteps.add("\tγ^${it + 1} = ${tmp.fix().y}")

                    }

                    if (isGenerator) {
                        tmpSteps.forEach(::println)
                        println("\n\t${y.y} è un generatore in GF($q)")
                        generator = y
                        generatorSteps = steps
                        return@GF
                    }

                }

            }

            val powers = arrayListOf(generator)

            println("\n\t${generator.y}^1 = ${powers.first().y}")

            GF(p) {

                (2..n).forEach {
                    val power = (powers.last() * generator).substitute(max, subst, this).fix()
                    powers.add(power)
                    println("\t${generator.y}^$it = ${power.y}")
                }

                val hTilde = (0 until max).map { arrayListOf<Int>() }
                val hTildeHadamard = ArrayList<List<Int>>()

                hTilde.forEachIndexed { index, row ->
                    powers.forEach { power ->
                        if (power.size > index) {
                            row.add(power[index].int)
                        } else {
                            row.add(0)
                        }
                    }
                }

                val remain = hTilde.toMutableList()

                hTildeHadamard.add(remain[0])
                remain.removeAt(0)

                while (hTildeHadamard.size < hTilde.size) {

                    var delets = arrayListOf<String>()

                    remain.forEach {

                        val obj = it
                        val right = obj.toList()
                        val left = obj.toList()

                        Collections.rotate(right, 1)
                        Collections.rotate(left, -1)

                        val that = obj.joinToString("")
                        val lf = left.joinToString("")
                        val rf = right.joinToString("")

                        val rPos = hTildeHadamard.indexOfFirst { it.joinToString("") == rf }
                        val lPos = hTildeHadamard.indexOfFirst { it.joinToString("") == lf }

                        if (rPos > -1) {
                            hTildeHadamard.add(rPos, obj)
                            delets.add(that)
                        }

                        if (rPos < 0 && lPos > -1) {
                            hTildeHadamard.add(lPos + 1, obj)
                            delets.add(that)
                        }

                    }

                    delets.forEach { rem ->
                        remain.removeAll { it.joinToString("") == rem }
                    }

                    remain.shuffle()

                }

                if (THEORY_ON) {
                    println("\n\tAllora la matrice di parità: ")
                    println("\n\t" + hTilde.joinToString("\n\t"))

                    println("\n\tSi osservi che la matrice ~H non rispetta la proprietà di ciclicità in quanto la riga i" +
                            "\n\tnon risulta in uno shift ciclico della riga precedente. Ordinare le righe in modo da ottenere" +
                            "\n\tuna matrice la matrice ~H generata dal polinomio rappresentante la prima riga, costituisce la" +
                            "\n\tmatrice generatrice del codice duale al codice di Hamming, ovvero il codice di Hadamard." +
                            "\n\n\tOttenuta la matrice ~H, generata dal polinomio rappresentante la prima riga, costituisce la matrice\n" +
                            "\n\tgeneratrice del codice duale al codice di Hamming, ovvero il codice di Hadamard")

                    println("\n\t" + hTildeHadamard.joinToString("\n\t"))

                    println("\n\tPer ricavare il polinomio g(x) generatore [$n, $k, $d]${p.sub()} codice ciclico di Hamming," +
                            "\n\toccorre notare che x^n - 1 = h(x) * g(x) e quindi x^$n - 1 = h(x) * g(x), ovvero" +
                            "\n\tg(x) è il polinomio risultante dalla divisione in modulo:\n")

                    val xn = pol(n.int + 1, 0 to -1, n.int to 1).fix()
                    val hx = pol(*hTildeHadamard.first().toIntArray())

                    val gx = xn.div(hx, this)
                    val original = DoubleArray(n.int) { 0.0 }
                    gx.coefficients.forEachIndexed { i, d -> original[i] = d }

                    println("\tg(x) = $gx = (${original.map { it.int }.joinToString(",")}) ")
                    println("\n\tse il grado di g(x) > ${n-k} effettuare uno shift per ottenere il polinomio generatore cercato.")


                }
            }
        }

    }

    private fun permutations(soFar: String, iterations: Int) {
        if (iterations == 0) {

            val item = soFar.toCharArray().map { it.toString().toInt() }.toList()

            if (item.sum() == 0)
                return

            combinations.add(item)

        } else {
            permutations(soFar + "0", iterations - 1)
            permutations(soFar + "1", iterations - 1)
        }
    }

}