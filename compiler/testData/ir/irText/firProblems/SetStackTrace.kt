// FULL_JDK

fun foo(cause: Throwable?) {
    if (cause != null) {
        cause.stackTrace = cause.stackTrace
    }
}

fun bar(cause: Throwable) {
    cause.stackTrace = cause.stackTrace
}
