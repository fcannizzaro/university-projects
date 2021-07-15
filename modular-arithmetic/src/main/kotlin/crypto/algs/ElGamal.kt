package crypto.algs

import crypto.PrinterFile
import crypto.model.Curve
import crypto.model.GF

/*
*    Francesco Saverio Cannizzaro (fcannizzaro)
*/
class ElGamal(val block: ElGamal.() -> Unit) : Curve(0, 0) {

    lateinit var gamma: P
    lateinit var alpha: P
    lateinit var beta: P

    init {
        println("\n------------------------------------------------")
        println("             ESERCIZIO ElGamal")
        println("------------------------------------------------")
        block(this)
    }

    fun gf(p: Long) {
        this.K = GF(p)
    }

    fun curve(a: Long, b: Long) {
        this.a = a
        this.b = b
    }

    private fun exec(): P {
        return run {
            val (private, public) = privateKey(gamma, alpha, beta)
            val key = sharedKey(private, public)
            println("\n - K = $key")
            return@run key
        }
    }

    fun encrypt(c: P): P {
        val ex = exec()
        PrinterFile.header("Encrypt Message \"c + k\"")
        return c + ex
    }

    fun decrypt(c: P): P {
        val ex = exec()
        PrinterFile.header("Decrypt Message \"c - k\"")
        return c - ex
    }

}