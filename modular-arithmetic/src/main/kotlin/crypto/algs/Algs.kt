package crypto.algs

import crypto.PrinterFile
import crypto.model.Curve
import crypto.model.N
import crypto.superscript
import crypto.table

/*
*    Francesco Saverio Cannizzaro (fcannizzaro)
*/

fun mcd(a: Long, b: Long): Long = euclideEsteso(Math.abs(a), Math.abs(b)).r

fun mcm(a: Long, b: Long): Long = Math.abs(a * b) / mcd(a, b)

fun Curve.sharedKey(private: Long, public: Curve.P): Curve.P {
    PrinterFile.header("$public ^ $private = $private * $public", "no-space")
    return public pow private
}

fun Curve.privateKey(gamma: Curve.P, publicKey1: Curve.P, publicKey2: Curve.P): Pair<Long, Curve.P> {
    val a = privateKey(gamma, publicKey1, "ᵃ", "alpha")
    val b = privateKey(gamma, publicKey2, "ᵇ", "beta")
    return if (a > b) {
        println("\n - PRIVATE $b, PUBLIC $publicKey2")
        Pair(b, publicKey1)
    } else {
        println("\n - PRIVATE $a, PUBLIC $publicKey1")
        Pair(a, publicKey2)
    }
}

fun Curve.privateKey(gamma: Curve.P, public: Curve.P, pv: String = "", pb: String = ""): Long {

    var text = ""
    var i = 1L

    run {

        var p: Curve.P = gamma

        while (p != public) {
            PrinterFile.header("${i+1}γ = ${i}γ + γ")
            PrinterFile.print("$p + $gamma")
            text += "\n$i) gamma${i.superscript()} = $p"
            p += gamma
            i++
        }

        text += "\n$i) gamma${i.superscript()} = $p"

    }

    if(pv.isNotEmpty()){
        print("\n ---- gamma$pv = $pb ----")
        println(text)
        println(" ------ END STEPS ------")
    }

    return i

}

fun Curve.points(): List<Triple<N, N, N>> {

    val mapped = hashMapOf<Long, N>()
    val list = arrayListOf<Triple<N, N, N>>()

    this.K.compute {

        (0 until this.p).forEach {
            val x = it.modular
            mapped[x.pow(2).modular.value] = x
        }

        (0 until this.p).forEach {
            val x = it.modular
            val y2 = x.pow(3) + (a * x) + b
            val y = mapped[y2.value]

            if (y2.value in mapped) {
                list += Triple(x, y2, y!!)
            }

        }

    }

    return list

}

fun roPollardFactorization(n: Long) {
    val ro = RoPollard.full(n, true)
    ro.first.forEach {
        table(it.map { Triple(it.xi.value, it.x2i.value, it.d) })
    }
}

fun factors(n: Long): List<Long> = RoPollard.all(n, false)