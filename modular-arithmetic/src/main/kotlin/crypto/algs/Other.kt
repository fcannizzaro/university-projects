package crypto.algs

import crypto.sub
import crypto.superscript

/**
 * Created by Francesco Saverio Cannizzaro (fcannizzaro)
 */
fun BCH() {
    println("\n\tDato un GF(9), si ricavano gli m_i(x) in GF(3), δ = 4." +
            "\n\n\t - trovare a tentativi q e m tale che q^m = 9 e sia conforme a GF(3)" +
            "\n\t - estendi il campo scegliendo un polinomio irriducibile (grado m)" +
            "\n\t - si cerca un generatore γ (ultimo valore 1, e non tutti gli altri)" +
            "\n\t - si calcolano tutte le potenze (γ^i) del generatore" +
            "\n\t - si fa un polinomio generico (di grado m) con i coefficienti = m_i(x)" +
            "\n\t - per ogni potenza γ^i si fa m_i(γ^i) e da cui si ottengono i coefficienti" +
            "\n\t - g(x) = mcm(m_c(x) ... m_(c+δ-2)) = ..." +
            "\n\t - si calcola h(x) = (x^n - 1)/(g(x))" +
            "\n\t - si flippa per ottenere ~h da cui si genera H")
}

fun HyperEllipticCanonical() {
    println("\n\tCanonical HyperElliptic" +
            "\n\n\tData una curva iperelittica H = y^2 = x(x^2-4)(x^2-1) su GF(11)" +
            "\n\n\tBisogna stabilire se D = (equivalente) P1 + P2 - 2 Ω ovvero affermare ce A - B" +
            "\n\tappartiene a Div${(0L).superscript()}(f) sottoinsieme di Princ(f), ovvero il divisore A - B è un divisore di grado 0" +
            "\n\te quindi un divisore principale." +
            "\n\n\tPer dimostrarlo quindi D - P1 - P2 + 2Ω = div(f). Si definisce la cubica f omogeneizzata\n" +
            "\n\ty = a${3.sub()}x${(3L).superscript()} + a${2.sub()}x${(2L).superscript()} + a${1.sub()}x + a${0.sub()}\n" +
            "\n\tomogeneizzata = (yt^2 - a${3.sub()}t^3 - a${2.sub()}t*x^2 - a${1.sub()}xt^2 - a${0.sub()}t^3)/(t^3)" +
            "\n\tricaviamo i coefficienti della cubica imponendo il passaggio per i punti appartenenti al supporto di D." +
            "\n\t - fare matrice dei punti, fare l'inversa e moltiplicare per il vettore delle y" +
            "\n\t - sostituire i coefficienti nella cubica" +
            "\n\t - fare l'interezione con l'iperellittica  e ottenere il polinomio #" +
            "\n\t - dividere il polinomio (# trovato)/((x-x0)(x-x1)) con x0, x1 punti già noti" +
            "\n\t - controllare che il polinomio ottenuto sia monico e in caso moltiplicare per ottenere il suo multiplo" +
            "\n\t - si trovano gli altri punti risolvendo questo polinomio" +
            "\n\n\tdiv(f) = [...] + [...] .. punti trovati" +
            "\n\tdiv(f) = D + punti rimanenti" +
            "\n\n\tD = -(punti rimanenti) + div(f)" +
            "\n\t D = punti rimanenti - (# omega) + div()")
}

fun info() {
    println("\n\tDato un codice lineare [n, k, d]_q su GF(p)" +
            "\n\n\tCorrezione Errori\n\t = (d-1)/2" +
            "\n\n\tRelazioni\n\tk = n - δg(x)" +
            "\n\n\tDimensioni Matrici" +
            "\n\t  - g(x) -> G = k ⨯ n" +
            "\n\t  - h(x) -> H = (n-k) ⨯ n")
}