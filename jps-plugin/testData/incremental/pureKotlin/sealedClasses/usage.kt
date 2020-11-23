package test

fun test(base: Base) {
    val x = when (base) {
        is A -> 1
        is B -> 2
    }
}
