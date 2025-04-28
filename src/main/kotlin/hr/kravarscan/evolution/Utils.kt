package hr.kravarscan.evolution

fun Double.format(digits: Int) = "%.${digits}f".format(this)