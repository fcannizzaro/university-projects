package crypto.model

import crypto.PrinterFile
import crypto.exp

/**
 * Created by Francesco Saverio Cannizzaro (fcannizzaro)
 */
open class Curve(var a: Long = 0, var b: Long = 1, block: Curve.() -> Unit = {}) {

    var K = GF(0)

    init {
        block()
    }

    fun <T> run(block: Curve.() -> T): T {
        return block()
    }

    fun point(z: Long, x: Long, y: Long): P = P(z, x, y)

    inner class P(private var z: Long, X: Long, Y: Long) {

        private val x = N(X, K)
        private val y = N(Y, K)
        var label: String = ""

        override fun equals(other: Any?): Boolean = if (other is P) x == other.x && y == other.y && z == other.z else false

        operator fun unaryMinus(): P = P(z, x.value, -y.value)

        infix fun pow(exp: Long): P {
            var result: P = this
            K.compute { result = ((1 until exp).foldIndexed(this@P) { i, current, _ ->
                PrinterFile.header("$current + ${this@P}", if(i == 0) "no-header" else "")
                current + this@P
            }) }
            return result
        }

        private fun isOmega() = x.value == 0L && z == 0L

        operator fun minus(p: P): P = this + (-p)

        operator fun plus(p: P): P {
            var zn = 0L
            var xn = 0L
            var yn = 1L
            K.compute {
                if (p.isOmega() && isOmega()) {
                    PrinterFile.print("\\omega + \\omega = [$zn; $xn, $yn]".exp())
                } else if (p.isOmega()) {
                    zn = z
                    xn = x.value
                    yn = y.value
                    PrinterFile.print("P + \\omega = [$zn; $xn, $yn]".exp())
                } else if (isOmega()) {
                    zn = p.z
                    xn = p.x.value
                    yn = p.y.value
                    PrinterFile.print("\\omega + Q = [$zn; $xn, $yn]".exp())
                } else if (p.x == x) {
                    if (p.y.value != 0L && y.value != 0L) {
                        if (p.y == y) {
                            val fraction = ((3 * (x pow 2) + a) / (2L * y))
                            val fracLatex = "(\\frac{3*${if (x.value >= 0) "$x" else "($x)"}^2${if (a != 0L) "+ $a" else ""}}{2*$y})^2"
                            val fracLatex1 = "\\frac{3*${if (x.value >= 0) "$x" else "($x)"}^2${if (a != 0L) "+ $a" else ""}}{2*$y}"
                            zn = 1
                            xn = ((fraction pow 2) - 2L * x).short()
                            yn = (-y + fraction * (x - xn)).short()
                            PrinterFile.print("P + P = 2P = [1; $fracLatex-2*$x, -$y+$fracLatex1*(x_{2P}-$x)]".exp())
                            PrinterFile.print("P + P = 2P = [1; $fracLatex-2*$x, -$y+$fracLatex1*($xn-$x)]".exp())
                        }
                    }
                } else {
                    val m = (p.y - y) / (p.x - x)
                    PrinterFile.print("m = \\frac{${p.y} - $y}{${p.x} - $x}".exp())
                    PrinterFile.print("P + Q = [1; ($m)^2-$x-${p.x}, -$y-$m*(($m)^2-2*$x-${p.x})]".exp())
                    zn = 1
                    xn = ((m pow 2) - x - p.x).short()
                    yn = (-y - m * ((m pow 2) - 2L * x - p.x)).short()
                }
            }
            PrinterFile.print(" = ($zn,$xn,$yn)")
            return P(zn, xn, yn)
        }

        override fun toString(): String = (if (label.isEmpty()) "" else "$label =") + "($z,${x.value},${y.value})"

    }

    override fun toString(): String {
        return "y^2 = x^3 + ${a}x + $b"
    }

}

