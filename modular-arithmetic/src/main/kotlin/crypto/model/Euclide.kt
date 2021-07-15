package crypto.model

/**
 * Created by Francesco Saverio Cannizzaro (fcannizzaro)
 */
data class Euclide(val r: Long, val s: Long, val t: Long, val steps: List<EEAEquation>, val d: Long = t, val u: Long = s) {

    override fun toString(): String = "[r= $r, s= $s, t= $t]"

    fun print(prefix: String = "", prefix2 : String = "" ) {
        steps.forEach {
            println(prefix + "{")
            println(prefix2 + "\t " + it.r)
            println(prefix2 + "\t " + it.s)
            println(prefix2 + "\t " + it.t)
            println(prefix + "}")
        }
    }

}