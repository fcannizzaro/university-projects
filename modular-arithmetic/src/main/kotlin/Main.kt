import crypto.PrinterFile
import crypto.algs.*
import crypto.model.Curve
import crypto.model.GF
import crypto.pol

fun main(args: Array<String>) {

    isGenerator("x^2+1".pol, "x^6-1".pol, 7, 49)

    // logs()

    // RSA

    /*
    Firm.RSA {
        n = 483047
        e = 13
        val m = 101L
        val f = firm(m)
        verify(f, m)
    }

    */

    /*
    CyclicCode {
        gx(1, 0, 0, 1, 0, 0, 1)
        gf(11)
   }

    // Verifica un generatore
    isGenerator("x+5".pol, "x^2-5".pol, 13, 169)

    info()

    BCH()

    HyperEllipticCanonical()

    // Hamming
    Hamming {
        code(15, 11, 3, 2)
        gx = pol(1, 1, 0, 0, 1)
    }

    // Reed Solomon
    ReedSolomon {
        words = 49
        d = 5
        errors = 2
    }

    // Reed Solomon con estensione
    ReedSolomon {
        words = 81
        d = 7
        errors = 3
        j = pol(1, 0, 1)
    }

    // Codice Minimo
    CyclicCode {
        gx(1, 0, 0, 1, 0, 0, 1)
        gf(11)
    }

    // Iper Ellittica
    HyperElliptic {

        GF = 11
        CURVE = "x*(x^2-4)(x^2-1)"

        points(
                point(4, -4),
                point(8, -1),
                point(9, 0),
                point(0, 0)
        )

        step1(0, 0, 10, 5)
        step2("3x^6 + x^5 + x^4 - x^5 - 6x^3 - 4x")
        step3("3x^2 + 8x + 2")
        step4()
        step5(5, 8)

    }

    // DSA
    Firm.DSA {
        n = 457
        gamma = 13
        val m = 100L
        val alpha = 284L
        val (r, s) = firm(m, k = 23, alpha = alpha)
        verify(m, r, s, alpha)
    }

    // RSA
    Firm.RSA {
        n = 483047
        e = 13
        val m = 101L
        val f = firm(m)
        verify(f, m)
    }

    // Euclide Esteso
    val result = euclideEsteso(11, 1254)
    println(result)
    println(result.print())

    // El Gamal
    ElGamal {
        gf(37)
        curve(0, 1)
        alpha = point(1, 21, 7)
        beta = point(1, -1, 0)
        gamma = point(1, 4, 19)
        println("\nmessage" + decrypt(point(1, 3, 19)))
    }

    // El Gamal Alternativa

    Curve(0, 1) {
        K = GF(37)
        val gamma = point(1, 4, 19)
        val alpha = point(1, 21, 7)
        val beta = point(1, 7, 23)
        val a = privateKey(gamma, alpha)
        val b = privateKey(gamma, beta)
        val k = beta pow a
        val m = point(1, 3, 19) - k
    }

    // Somma di Punti a Mano
    Curve(0, 1) {

        K = GF(37)

        val alpha = point(1, 21, 7)

        PrinterFile.header("2 * alpha = alpha + alpha")
        val a2 = alpha + alpha

        PrinterFile.header("4  alpha  = 2  (2*alpha)")
        val a4 = a2 + a2

        PrinterFile.header("5 * alpha  = 4 alpha  + alpha")
        val a5 = a4 + alpha

        println(a5)

    }

    /// ro di pollard con tabella
    roPollardFactorization(5)

    // fattori
    factors(5)

    */

}


