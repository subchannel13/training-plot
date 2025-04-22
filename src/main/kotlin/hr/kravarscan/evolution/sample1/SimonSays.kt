package hr.kravarscan.evolution.sample1

import kotlin.math.absoluteValue

class SimonSays(private val x: Int) {
    var error = Int.MAX_VALUE
        private set

    fun question() = this.x

    fun answer(num: Int) {
        this.error = (num - this.x).absoluteValue
    }
}