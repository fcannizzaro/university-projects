package crypto.algs

import crypto.isPrime
import crypto.model.GF

/**
 * Created by Francesco Saverio Cannizzaro (fcannizzaro)
 */
object Firm {

    class DSA(block: DSA.() -> Unit) {

        var n: Long = 0
        var gamma: Long = 0

        init {
            println("\n------------------------------------------------")
            println("             ESERCIZIO Firma DSA")
            println("------------------------------------------------")
            block(this)
        }

        fun alpha(a: Long): Long {
            var res = 0L
            GF(n) { res = gamma.modular pow a }
            return res
        }

        fun firm(message: Long, k: Long = -1, a: Long = -1, alpha: Long = -1): Pair<Long, Long> {

            println("\n\t[FIRMA]")

            var _k = k

            if(_k < 0){
                var i = 2L
                while (mcd(i, n-1) != 1L) i++
                _k = i
                println("\n\tCALCOLO \"k\" coprimo con ${n-1} = $_k")
            }

            var _a = a

            if (alpha > 0 && a < 0) {
                _a = logDiscretoTentativi(gamma, alpha, n)
                println("\n\tCALCOLO \"a tentativi\" a da alpha ($alpha)\n\t       = gamma^a = alpha mod $n\n\t       = $gamma^$_a = $alpha mod $n\n\t       = $_a")
            }

            var S = 0L
            var r = 0L
            GF(n) {
                println("\n\tCALCOLO r\n\t       = γ^k mod $p\n\t       = $gamma^$_k mod $p\n")
                r = squareAndMultiply(gamma.modular.value, _k, p)
                println("\t    r = $r")
                var reversed = 0L
                GF(n - 1) {
                    val m = message.modular
                    reversed = (1.modular / _k).value
                    S = (reversed * (m + _a * r)).full()
                    println("\n\t CALCOLO S (FIRMA)\n\t       = k^-1 * (message + a*r) mod $p\n\t       = 1/$_k * ($message + $_a*$r) mod $p\n\t       = $reversed * ($m + $_a*$r) mod $p")
                    println("\t       = $S")
                }
            }
            return Pair(r, S)
        }

        fun verify(message: Long, r: Long, s: Long, alpha: Long) {
            GF(n) {
                println("\n\t[VERIFICA]")
                val rs = r.modular pow s
                val ar = alpha.modular pow r
                val t = rs.modular
                val res = (ar * (gamma.modular pow message)).modular.value
                val v = res - t
                println("\n\t CALCOLO la VERIFICA\n\t       = γ^message * α^r - r^S mod $p\n\t" +
                        "       = $gamma^$message * $alpha^$r - $r^$s mod $p\n\t" +
                        "       = $res * $ar - $rs mod $p\n\t       = $res - $t\n\t       = $v\n")
                println(if (v.value == 0L) "=> FIRMA VALIDA" else "=> FIRMA NON VALIDA")
            }
        }

    }

    class RSA(block: RSA.() -> Unit) {

        var e: Long = 0
        var n: Long = 0

        init {
            println("\n------------------------------------------------")
            println("             ESERCIZIO Firma RSA")
            println("------------------------------------------------")
            block(this)
        }

        fun firm(message: Long): Long {
            var result = 0L
            GF(n) {
                println("\n\t[FIRMA]")
                val f = factors(n)
                val root = Math.round(Math.sqrt(n.toDouble()))
                val p = f[0] - 1
                val q = f[1] - 1

                val min = (f.min() ?: root) - 20
                val max = root + 20

                println("\n\t - INTORNO DI  √$n = $root\n\n\t   " + (min..max).filter { it.isPrime() }.joinToString("  ") { if (it == f[0]) "[$it]" else "$it" })
                println("\n\t - FATTORIZZAZIONE DI n\n\t   $n = " + f.joinToString(" x "))
                val lambda = mcm(p, q).modular.value
                if (mcd(lambda, e) != 1L) throw Exception("e non è coprimo con λ")
                println("\n\t - DEFINISCO\n\t   P = ${f[0]}\n\t   Q = ${f[1]}")
                println("\n\t - CALCOLO\n\t    λ = mcm(p-1,q-1)=\n\t      = mcm($p,$q)\n\t      = $lambda")
                val euc = euclideEsteso(e, lambda)
                var d = euc.d
                if (d < 0) {
                    var i = 0L
                    while (d < 0) d += lambda * ++i
                    println("\n\t Dato che è \"d\" negativo possiamo ottenere la d calcolandola come d = d + lambda * i finchè d < 0.")
                }
                println("\n\t - EUCLIDE ESTESO (e,λ) = EUCLIDE ESTESO($e,$lambda) => e*d + * λ*u = 1\n\t   d = $d")
                euc.print("\n\t", "\t")
                println("\n\t - CALCOLO LA FIRMA\n\t   = message^d\n\t   = $message^$d\n")
                result = squareAndMultiply(message.modular.value, d, n)
                println("\tf = $result")
            }
            return result
        }

        fun verify(f: Long, message: Long) {
            GF(n) {
                println("\n\t[VERIFICA]")
                println("\n\t - CALCOLO\n\t  = f^e = m\n\t  = $f^$e\n")
                val decoded = squareAndMultiply(f, e, p)
                println("\t  = $decoded\n\t")
                println(if (decoded == message) " => FIRMA VALIDA" else " => FIRMA NON VALIDA")
            }
        }

    }

}