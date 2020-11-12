class SomeBuilder {
    val list: MutableList<String> = mutableListOf()
}

interface SomeType {
    val list: List<String>
}

fun someBuild(init: SomeBuilder.() -> Unit) {
    SomeBuilder().apply(init)
}

fun foo(type: SomeType?) {
    someBuild {
        type.apply {
            // Should be resolved to SomeBuilder.list, not to SomeType.list
            <!VARIABLE_EXPECTED!>list<!> += listOf("Alpha", "Omega")
        }
    }
}

