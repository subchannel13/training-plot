package hr.kravarscan.evolution.sample1

import kotlin.math.absoluteValue

class SimonSays(private val x: Double) {
    var error = Double.NaN
        private set

    fun question() = this.x

    fun answer(num: Double) {
        this.error = (num - this.x).absoluteValue
    }
}