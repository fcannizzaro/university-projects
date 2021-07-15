package crypto.algs

import crypto.powers2
import kotlin.math.exp

/**
 * Created by Francesco Saverio Cannizzaro (fcannizzaro)
 */
fun logDiscretoTentativi(base: Long, result: Long, modulus: Long): Long {
    var exp = 1L
    val bas = base % modulus
    var tmp = base % modulus
    val end = result % modulus
    while (tmp != end) {
        exp++
        tmp = (tmp * bas) % modulus
    }
    return exp
}

fun logDiscretoTentativi(base: Long, result: Long): Long {
    var exp = 1L
    val bas = base
    var tmp = base
    val end = result
    while (tmp != end) {
        exp++
        tmp = (tmp * bas)
    }
    return exp
}

fun squareMultiplyInternal(base: Long, exponent: Long, modulus: Long): Triple<Pair<List<String>, Long>, Long, Long> {
    val near = Math.ceil(Math.log(exponent.toDouble()) / Math.log(2.0))
    val powers = powers2.subList(0, near.toInt())

    if (exponent == 1L) {
        return Triple(Pair(emptyList(), 1L), base, 0)
    }

    val res = exponent - powers.last()
    val calc = mutableListOf<Long>()

    powers.forEachIndexed { index, l ->
        if (index > 0) {
            calc.add((calc.last() * calc.last()) % modulus)
        } else {
            calc.add(base)
        }
    }

    val steps = calc.mapIndexed { i, value -> "$base^${powers[i]} = $value" }
    return Triple(Pair(steps, powers.last()), calc.last(), res)
}

fun squareAndMultiply(base: Long, exponent: Long, modulus: Long): Long {

    var result = 1L
    var res = exponent
    val set = ArrayList<String>()
    val exps = ArrayList<Long>()

    while (res > 0) {
        val calc = squareMultiplyInternal(base, res, modulus)
        set.addAll(calc.first.first.subtract(set))
        exps.add(calc.first.second)
        res = calc.third
        result = (result * calc.second) % modulus
    }

    println("\t - SQUARE AND MULTIPLY:")
    println("\t  $base^$exponent mod $modulus = " + exps.map { "$base^$it" }.joinToString(" * ") + " = $result\n\n\t => POWERS:")
    println("\t    " + set.joinToString("\n\t    ") + "\n")

    return result

}