package crypto.algs

import crypto.*
import crypto.model.GF
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction

/**
 * Created by Francesco Saverio Cannizzaro (fcannizzaro)
 */
fun isGenerator(pol: PolynomialFunction, irriducible: PolynomialFunction, p: Long, extP: Long = p): Boolean {

    var isGenerator = true

    println(" > polinomio irriducibile $irriducible")

    GF(p) {

        val max = irriducible.degree()
        val subst = -(irriducible - pol(max + 1, max to irriducible[max].int))

        println(" > sostituisco j^$max =  $subst")

        val y = pol
        var tmp = y

        val pM = extP - 1
        val divisors = (1 until extP).filter { pM % it == 0L }

        println(" > divisori di $pM $divisors\n")

        (2 until extP).forEach {

            tmp = (tmp * y).substitute(max, subst, this).fix()

            println("(${y.y})^$it = ${tmp.y}")

            if (it in divisors) {
                if (tmp.isModulusOne()) {
                    if (it < extP - 1) {
                        isGenerator = false
                        return@GF
                    }
                }
            }
        }
    }

    println("\n$pol è un generatore\n")

    return isGenerator

}