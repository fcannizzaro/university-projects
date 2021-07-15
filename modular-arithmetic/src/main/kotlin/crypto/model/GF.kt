package crypto.model

import crypto.algs.euclideEsteso
import crypto.pol
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction

/**
 * Created by Francesco Saverio Cannizzaro (fcannizzaro)
 */
class GF(val p: Long, block: GF.() -> Unit = {}) {

    val Long.modular: N
        get() = toM()

    val Int.modular: N
        get() = toM()

    val Double.modular: N
        get() = toM()

    init {
        block()
    }

    fun compute(block: GF.() -> Unit) = block()

    /* math.N Operators */

    operator fun Long.plus(other: N) = N(this + other.value, this@GF)

    operator fun Long.minus(other: N) = N(this - other.value, this@GF)

    operator fun Long.times(other: N) = N(this * other.value, this@GF)

    @Throws(IllegalStateException::class)
    operator fun Long.div(other: N) = N(div(this, other.value), this@GF)

    /* Utils */

    @Throws(IllegalStateException::class)
    private fun invert(den: Long): Long {

        val (r, s, t) = euclideEsteso(p, den)

        val res = when {
            (den * s) % p == 1L -> s
            else -> t
        }

        if (r % p != 1L) {
            throw IllegalStateException("$den is not invertible")
        }

        return res

    }

    fun pow(base: Long, exp: Long) = (1 until exp).fold(base, { res, _ -> (res * base) % p })

    @Throws(IllegalStateException::class)
    fun div(num: Long, den: Long) = num * invert(den)

    fun isGenerator(lambda: Long) = (1 until p).indexOfFirst { N(lambda, this@GF) pow it == 1L }.toLong() == p - 2L

    fun Long.toM() = N(this, this@GF)

    fun Int.toM() = N(this.toLong(), this@GF)

    fun Double.toM() = N(this.toLong(), this@GF)

    fun PolynomialFunction.fix(): PolynomialFunction = pol(*coefficients.map { it.modular.short().toDouble() }.toDoubleArray())

    fun PolynomialFunction.isModulusOne() = this.coefficients.size == 1 && this.coefficients[0].modular == 1.modular

    fun DoubleArray.fix(): DoubleArray = map { it.modular.short().toDouble() }.toDoubleArray()

    override fun toString(): String = "GF($p)"

}
