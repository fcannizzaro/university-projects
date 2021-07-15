package crypto.algs

import com.sun.org.apache.xml.internal.security.Init.isInitialized
import crypto.*
import crypto.model.GF
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealVector
import java.util.*

/**
 * Created by Francesco Saverio Cannizzaro (fcannizzaro)
 */
class ReedSolomon(block: ReedSolomon.() -> Unit = {}) {

    var words: Long = 0
    var errors: Long = 0
    var d: Long = 0
    var lambda: Long = 0
    private lateinit var K: GF
    private var n: Long = 0
    private var k: Long = 0
    private var q: Long = 0

    val combinations = mutableListOf<List<Int>>()

    lateinit var j: PolynomialFunction

    init {
        println("\n------------------------------------------------")
        println("            ESERCIZIO REED-SOLOMON")
        println("------------------------------------------------")
        block()
        parameters()
    }

    private fun parameters(): Triple<Long, Long, Long> {
        val factors = RoPollard.all(words, false)
        val possibility = factors.size / 2 - 1
        var last = factors.size
        var tmp = 1L

        val results = mutableListOf<Pair<Long, Long>>()

        for (i in 0..possibility + 1) {
            val value = Math.pow(factors[0].toDouble(), last.toDouble()).toLong()
            results.add(Pair(value, tmp))
            last /= 2
            tmp *= 2
        }

        if (THEORY_ON)
            print("\n\tPoichè i codici di Reed-Solomon sono BCH primitivi e in senso stretto\n\toccorre determinare n tale che q - 1 = n * b e b = 1.\n\n\tIl codice deve avere $words parole e quindi le possibile combinazioni do q e k sono: ")

        println(results.joinToString(", ") { "(q=${it.first}, k=${it.second})" })

        results.forEachIndexed { index, (q, k) ->
            println("\t${index + 1}) $q - 1 = $d + $k - 1 \t ${if (q - 1 == d + k - 1) "Valido" else "Non Valido"}")
        }

        val found = results.find {
            val q = it.first
            val k = it.second
            q - 1 == d + k - 1
        }

        found?.let {
            k = found.second
            n = d + k - 1
            q = found.first
            K = GF(q)

            println("\n\t - Parametri")
            println("\t   [n,k,d] = [$n,$k,$d]${q.sub()} codice ciclico su GF($q)")

            val facts = RoPollard.all(q - 1, false)

            K.compute {

                fun clean(vector: RealVector): String = "(" + vector.toArray().map { it.modular.value }.joinToString(",") + ")"

                if (q.isPrime())
                    (1..q).find { base ->
                        facts.all { Math.pow(base.toDouble(), it.toDouble()).toLong().modular != 1.modular }
                    }?.let {
                                if (lambda == 0L) lambda = it
                                println("\n\t - Generatore per GF($q)*")
                                println("\t   λ = $it\t(${(1 until q).map { exp -> "$lambda${exp.superscript()}" }.joinToString(", ")}) =  (${(1 until q).map { exp -> (lambda.modular pow exp).modular.full() }.joinToString(", ")})")
                                K.compute {
                                    val powers = (1..n - k).map { Math.pow(lambda.toDouble(), it.toDouble()).toLong().modular }
                                    val solutions = powers.map { pol(-it.toDouble(), 1.0) }
                                    val exp = solutions.joinToString("") { "($it)" }
                                    val gx = solutions.fold(pol(1.0), { current, value -> current * value }).fix()
                                    val powersForHx = (n - k + 1..n).map { Math.pow(lambda.toDouble(), it.toDouble()).toLong().modular }
                                    val solutionsForHx = powersForHx.map { pol(-it.toDouble(), 1.0) }
                                    val expForHx = solutionsForHx.joinToString("") { "($it)" }
                                    val hx = solutionsForHx.fold(pol(1.0), { current, value -> current * value }).fix()

                                    val hRows = generate(hx, n - k, true)
                                    val gRows = generate(gx, k)

                                    val hMatrix = MatrixUtils.createRealMatrix(hRows)
                                    val gMatrix = MatrixUtils.createRealMatrix(gRows)

                                    val format = " %-4s" * n.int
                                    val g0 = gMatrix.getRowVector(0)
                                    val h0 = hMatrix.getRowVector(0)

                                    if (THEORY_ON)
                                        println("\n\t - Per scrivere il polinomio g(x) si considerano" +
                                                " le prime ${n - k} radici, ovvero i primi ${n - k} elementi del generatore:" +
                                                "\n\n\t - g(x)\n\t         = $exp\n\t\t     = $gx\n\t\t     ≡ ${clean(g0)}")

                                    println("\n\t - h(x)\n\t         = $expForHx\n\t\t     = $hx\n\t\t     ≡ (${clean(h0).replace("[)(]".toRegex(), "").reversed()})")

                                    println("\n\t allora la matrice di parità è generata dal polinomio ~h(x) ≡ ${clean(h0)}")

                                    if (THEORY_ON) {
                                        println("\n\t - H = <h(x)>\n\n\t      " + hRows.joinToString("\n\t      ") { "|  ${String.format(format, *it.map { it.int }.toTypedArray())} |" })
                                        println("\n\t - G = <g(x)>\n\n\t      " + gRows.joinToString("\n\t      ") { "|  ${String.format(format, *it.map { it.int }.toTypedArray())} |" })
                                        println("\n\t - Scelgo una parola \"w\" come combinazione lineare delle $k righe")
                                    }

                                    val i0 = Random().nextInt(k.int)

                                    while (true) {
                                        val random = Random().nextInt(10) + 1
                                        val i1 = Random().nextInt(k.int)
                                        if (random == q.toInt()) continue
                                        if (i0 == i1) continue
                                        val row0 = gMatrix.getRowVector(i0)
                                        val row1 = gMatrix.getRowVector(i1)
                                        val wVector = row0.add(row1)
                                        val w = wVector.toArray().map { it.modular.value }

                                        if (THEORY_ON)
                                            println("\n\t\tw = (${w.joinToString(",")})\n\n\tottenuto come combinazione lineare delle righe $i0 e $i1, ovvero ${clean(row0)}  +  ${clean(row1)}")

                                        val zVector = wVector.map { it }

                                        val committedErrors = arrayListOf<Int>()

                                        while (committedErrors.size < errors) {
                                            val position = Random().nextInt(n.int)
                                            if (position in committedErrors) continue
                                            zVector.setEntry(position, zVector.getEntry(position) + 1)
                                            committedErrors.add(position)
                                        }

                                        if (THEORY_ON)
                                            println("\n\tViene ricevuta la parola \"z\" = " + clean(zVector) + " che non si trova nel codice C (H ⨯ z^t ≠ 0):")

                                        val zMatrix = MatrixUtils.createColumnRealMatrix(zVector.toArray())
                                        val result = hMatrix.multiply(zMatrix)

                                        val col = result.getColumn(0).fix()
                                        val max = "${col.max()}".length

                                        val base = Array(n.int) { 0 }
                                        base[0] = 1

                                        val canonical = (0 until n.int).map {
                                            val tmp = base.toList()
                                            Collections.rotate(tmp, it)
                                            tmp
                                        }.joinToString("\n\t")

                                        if (THEORY_ON)
                                            println("\n\t H * z^t =\t${col.joinToString("\n\t          \t") { "|${String.format("%${max}d", it.int)} |" }}" +
                                                    "\n\n\t H * z^t\n\t    = H * (w + α * e_i + β * e_j)^t" +
                                                    "\n\t    = H*w^t + α * H * e_i +  β * e_j" +
                                                    "\n\n\te_i, e_j sono vettori della base canonica" +
                                                    "\n\n\t" + canonical +
                                                    "\n\n\tma H*w^t = 0 poichè w si trova in C, mentre H * e_i = H_(i), H * e_j = H_(j)\n\tovvero la i-sima colonna e la j-sima colonna di H." +
                                                    "\n\n\tPertanto:\n\n\t   H * z^t = α * H_(i) + β * H_(j)" +
                                                    "\n\n\t(trova a occhio la combinazione lineare! e scrivila)" +
                                                    "\n\n\tquindi dalla parola z si può risalire alla parola w corretta.")

                                        break
                                    }
                                }
                            }
                else {


                    val res = factors(q)
                    val degExt = k - 1

                    val chars = "abcdefghilmno"
                    val ext = (0..degExt).mapIndexed { i, _ -> chars[i] + (if (i > 0) "j${if (i > 1) "^$i" else ""}" else "") }.joinToString(" + ")

                    if (!::j.isInitialized) {
                        println("\n\tEstendi il campo e aggiungi il polinomio j.\n")
                        return@compute
                    }

                    val max = j.degree()
                    val subst = -(j - pol(max + 1, max to j[max].int))

                    if (THEORY_ON)
                        println("\n\tCalcolo un'estensione del campo su GF(${res[0]}^${res.size})" +
                                "\n\n\tScelgo il polinomio $j = 0 e si impone che sia j una radice per questo polinomio in GF(${res[0]})" +
                                "\n\tGF($q) = { $ext , j^$max = $subst }" +
                                "\n\n\tDeterminiamo quindi un generatore per GF($q)* e ordiniamo secondo le potenze del generatore.")

                    combinations.clear()
                    permutations("", max)

                    var generator: PolynomialFunction = pol(0.0)
                    var generatorSteps: ArrayList<PolynomialFunction> = arrayListOf()

                    GF(res[0]) {

                        combinations.forEach {

                            val y = pol(*it.toIntArray())
                            val steps = arrayListOf(y)
                            var isGenerator = true
                            var tmp = y

                            println("\n\tVerifica per ${tmp.y}:")
                            println("\tγ^1 = ${tmp.y}")

                            (1 until q - 1).map {

                                tmp = (tmp * y).fix()
                                val step = tmp.substitute(max, subst, this@compute)
                                tmp = step
                                steps.add(step)

                                if (it < q - 2 && tmp.isModulusOne()) {
                                    isGenerator = false
                                }

                                if (it == q - 2) {
                                    isGenerator = tmp.isModulusOne() && isGenerator
                                }

                                println("\tγ^${it + 1} = ${tmp.y}")

                            }

                            if (isGenerator) {
                                println("\n\t${y.y} è un generatore in GF($q)")
                                generator = y
                                generatorSteps = steps
                                return@forEach
                            }

                        }

                        val gDeg = (n - k).int

                        println("\n\tPer costruire il polinomio g(x) di grado n - k = $gDeg,\n\tsi prendono le prime $gDeg radici ordinate secondo le potenze di ${generator.y}")

                        val gxText = generatorSteps.subList(0, gDeg).mapIndexed { i, _ -> "(x - γ^${i + 1})" }.joinToString("")
                        val gxText2 = generatorSteps.subList(0, gDeg).mapIndexed { i, value -> "(x-(${value.y}))".replace("[ ]+".toRegex(), "") }.joinToString("")

                        val hxText = generatorSteps.subList(gDeg, generatorSteps.size).mapIndexed { i, _ -> "(x - γ^${i + 1 + gDeg})" }.joinToString("")
                        val hxText2 = generatorSteps.subList(gDeg, generatorSteps.size).mapIndexed { i, value -> "(x-(${value.y}))".replace("[ ]+".toRegex(), "") }.joinToString("")

                        println("\n\tg(x) = $gxText\n\t     = $gxText2")
                        "expand ($gxText2) mod $p".wolfram("\n\t")
                        println("\n\th(x) = $hxText\n\t     = $hxText2")
                        "expand ($hxText2) mod $p".wolfram("\n\t")

                        println("\n\t -> stesso procedimento senza estensione del campo (es. words 49, d = 7 e errors = 2)")

                    }

                }
            }
        }

        if (n == 0L) {
            throw Exception("Cannot obtain Reed-Solomon parameters.")
        }

        return Triple(n, k, d)

    }

    private fun generate(pol: PolynomialFunction, height: Number, reverse: Boolean = false): Array<DoubleArray> {

        val rows = arrayListOf<DoubleArray>()
        val size = n.int
        val tilde = DoubleArray(size) { 0.0 }
        pol.coefficients.forEachIndexed { i, d -> tilde[if (reverse) size - i - 1 else i] = d }
        rows.add(tilde)

        while (rows.size < height.toInt()) {
            val row = rows.last().toList()
            Collections.rotate(row, 1)
            rows.add(row.toDoubleArray())
        }

        return rows.toTypedArray()

    }

    private fun permutations(soFar: String, iterations: Int) {
        if (iterations == 0) {

            val item = soFar.toCharArray().map { it.toString().toInt() }.toList()

            if (item.sum() == 0)
                return

            if (item[0] == 1 && item.subList(1, item.size).sum() == 0)
                return

            combinations.add(item)

        } else {
            permutations(soFar + "0", iterations - 1)
            permutations(soFar + "1", iterations - 1)
        }
    }

}
