package io.justdevit.spring.logging.extension

import java.lang.reflect.Method

val Method.hasParameters: Boolean
    get() = parameterCount > 0

val Method.returnsUnit: Boolean
    get() = returnType == Void.TYPE
