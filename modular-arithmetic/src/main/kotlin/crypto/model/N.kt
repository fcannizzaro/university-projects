package crypto.model

/**
 * Created by Francesco Saverio Cannizzaro (fcannizzaro)
 */
class N(value: Long, private val K: GF) {

    val value: Long = (if (value > 0) value else K.p + value) % K.p

    override fun equals(other: Any?): Boolean {

        if (other is N) {
            return other.short() == short()
        }

        if (other is Long) {
            return N(other, K) == this
        }

        return false

    }

    operator fun plus(other: Long) = N(value + other, K)

    operator fun minus(other: Long) = N(value - other, K)

    operator fun times(other: Long) = N(value * other, K)

    operator fun div(other: Long) = N(K.div(value, other), K)

    operator fun unaryMinus() = this * -1

    operator fun plus(other: N) = N(value + other.value, K)

    operator fun minus(other: N) = N(value - other.value, K)

    operator fun times(other: N) = N(value * other.value, K)

    operator fun div(other: N) = N(K.div(value, other.value), K)

    fun inverse(): N = N(1, K).div(this)

    infix fun pow(exp: Long): Long = K.pow(value, exp)

    infix fun pow(exp: N): N = N(K.pow(value, exp.value), K)

    fun short() = if (value > K.p / 2) -(K.p - value) else value

    fun full() = if (value < 0) K.p + value else value

    override fun toString(): String = "$value"

    fun toDouble(): Double = value.toDouble()

}