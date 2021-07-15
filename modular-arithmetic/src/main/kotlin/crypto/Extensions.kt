package crypto

import crypto.model.GF
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction
import java.io.UnsupportedEncodingException

/**
 * Created by Francesco Saverio Cannizzaro (fcannizzaro)
 */

var THEORY_ON = true

fun logs() {
    THEORY_ON = false
}

val regexPol = "([+-]?[^-+]+)".toPattern()

val String.pol: PolynomialFunction
    get() {

        val m = regexPol.matcher(this)
        val terms = arrayListOf<Triple<Int, Int, Boolean>>()

        while (m.find()) {

            val it = m.group(1)

            var coeff = 1
            var exponent = 1
            var symbol = false

            if ("^" in it) {
                val (_, exp) = it.split("^")
                exponent = exp.toInt()
            }

            if ("x" in it) {
                symbol = true
                val tmp = it.replace("x.*".toRegex(), "")
                if (tmp.isNotEmpty()) coeff = tmp.toInt()
            } else {
                val tmp = it.replace("\\^.*".toRegex(), "")
                if (tmp.isNotEmpty()) coeff = tmp.toInt()
            }

            terms.add(Triple(coeff, exponent, symbol))

        }

        val deg = terms.maxBy { it.third }?.second ?: 0
        val tints = IntArray(deg + 1) { 0 }

        terms.forEach { (base, exp, symbol) ->
            if (symbol) {
                tints[exp] += base
            } else {
                tints[0] += Math.pow(base.double, exp.double).toInt()
            }
        }

        return pol(*tints)

    }

//val POW_SQUARE_MULTIPLY = false

val powers2 = (0..20).map { Math.pow(2.toDouble(), it.toDouble()).toLong() }

fun Long.superscript(): String {
    var str = this.toString().replace("0", "⁰")
    str = str.replace("0", "⁰")
    str = str.replace("1", "¹")
    str = str.replace("2", "²")
    str = str.replace("3", "³")
    str = str.replace("4", "⁴")
    str = str.replace("5", "⁵")
    str = str.replace("6", "⁶")
    str = str.replace("7", "⁷")
    str = str.replace("8", "⁸")
    str = str.replace("9", "⁹")
    return str
}

fun Long.sub(): String {
    var str = this.toString().replace("0", "₀")
    str = str.replace("1", "₁")
    str = str.replace("2", "₂")
    str = str.replace("3", "₃")
    str = str.replace("4", "₄")
    str = str.replace("5", "₅")
    str = str.replace("6", "₆")
    str = str.replace("7", "₇")
    str = str.replace("8", "₈")
    str = str.replace("9", "₉")
    return str
}

fun Int.sub(): String = toLong().sub()

fun String.exp() = this.replace("--", "+")

fun Long.isPrime(): Boolean {

    if (this % 2 == 0L) return false

    var i = 3

    while (i * i <= this) {
        if (this % i == 0L)
            return false
        i += 2
    }

    return true
}

fun table(data: List<Triple<Long, Long, Long>>) {
    val format = "\n%4s %12d %12d %12d%n"
    println("\n\n              x_i         x_2i            d   ")
    data.forEachIndexed { index, it ->
        System.out.format(format, "x" + index.sub(), it.first, it.second, it.third)
    }
}

const val ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.!~'"

fun String.urlencode(): String {
    val l = this.length
    val o = StringBuilder(l * 3)
    try {
        for (i in 0 until l) {
            val e = this.substring(i, i + 1)
            if (ALLOWED_CHARS.indexOf(e) == -1) {
                val b = e.toByteArray(charset("utf-8"))
                o.append(getHex(b))
                continue
            }
            o.append(e)
        }
        return o.toString()
    } catch (e: UnsupportedEncodingException) {
        e.printStackTrace()
    }
    return this
}

private fun getHex(buf: ByteArray): String {
    val o = StringBuilder(buf.size * 3)
    for (i in buf.indices) {
        val n = buf[i].toInt() and 0xff
        o.append("%")
        if (n < 0x10) {
            o.append("0")
        }
        o.append(java.lang.Long.toString(n.toLong(), 16).toUpperCase())
    }
    return o.toString()
}

fun String.wolfram(prefix: String = "", post: String = "") {
    println(prefix + "https://www.wolframalpha.com/input/?i=" + this.urlencode() + post)
}

val Long.int: Int
    get() = toInt()

val Double.int: Int
    get() = toInt()

val Int.double: Double
    get() = toDouble()

val Double.long: Long
    get() = toLong()

fun PolynomialFunction.isOne() = this.coefficients.size == 1 && this.coefficients[0] == 1.0
fun PolynomialFunction.isZero() = this.coefficients.size == 1 && this.coefficients[0] == 0.0

operator fun PolynomialFunction.set(index: Int, value: Int) {
    this.coefficients[index] = value.double
}

operator fun PolynomialFunction.get(index: Int) = this.coefficients[index]

operator fun PolynomialFunction.times(other: PolynomialFunction): PolynomialFunction = multiply(other)
operator fun PolynomialFunction.plus(other: PolynomialFunction): PolynomialFunction = add(other)
operator fun PolynomialFunction.minus(other: PolynomialFunction): PolynomialFunction = subtract(other)
operator fun PolynomialFunction.unaryMinus(): PolynomialFunction = negate()

val PolynomialFunction.size: Int
    get() = coefficients.size

val PolynomialFunction.str: String
    get() = toString().replace("x", "α")

val PolynomialFunction.y: String
    get() = toString().replace("x", "j")

fun PolynomialFunction.div(div: PolynomialFunction, K: GF): PolynomialFunction {
    var result = pol(0.double)
    var last = pol(0.double)
    var num = this
    while (!num.isZero()) {
        val value = num.degree() - div.degree()
        var flag = false
        K.compute {
            val term = num[num.degree()] / div[div.degree()]
            if (value >= 0) {
                last = pol(value + 1, value to term.int)
                result += last
            } else {
                flag = true
            }
        }
        if (last.isZero() || flag) break
        num -= last * div
    }
    return result
}

fun pol(vararg x: Double) = PolynomialFunction(x)

fun pol(vararg x: Int) = PolynomialFunction(x.map { it.double }.toDoubleArray())

fun pol(size: Int, vararg pairs: Pair<Int, Int>): PolynomialFunction {
    val map = pairs.toMap()
    return PolynomialFunction(DoubleArray(size) { i -> map[i]?.double ?: 0.0 })
}

fun PolynomialFunction.swap(): PolynomialFunction {
    val data = DoubleArray(degree() + 1) { 0.0 }
    this.coefficients.forEachIndexed { i, d -> data[degree() - i - 1] = d }
    return pol(*data)
}

fun PolynomialFunction.substitute(grade: Int, other: PolynomialFunction, K: GF): PolynomialFunction {
    var tmp = this
    K.compute {
        if (degree() >= grade) {
            val term = tmp[grade].toInt()
            if (term != 0) {
                tmp -= pol(grade + 1, grade to term).fix()
                tmp += (pol(term.double) * other).fix()
            }
        }
    }
    return tmp
}

operator fun String.times(repeats: Int) = (0 until repeats).joinToString("") { this }