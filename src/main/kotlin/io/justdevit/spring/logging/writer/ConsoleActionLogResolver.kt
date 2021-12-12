package io.justdevit.spring.logging.writer

import io.justdevit.spring.logging.Sensitive
import io.justdevit.spring.logging.extension.hasParameters
import io.justdevit.spring.logging.extension.returnsUnit

/**
 * Action log resolver for stdout.
 *
 * @see ActionLogResolver
 */
class ConsoleActionLogResolver : ActionLogResolver {

    override fun onStart(context: OnStartLogContext) = ActionLog(
        message = "Action '${context.action}' has started.${if (context.method.hasParameters) " Parameters: [{}]" else ""}",
        arguments = if (context.method.hasParameters) listOf(context.stringifyParameters()) else emptyList()
    )

    override fun onFinish(context: OnFinishLogContext) = ActionLog(
        message = "Action '${context.action}' has successfully finished.${if (context.method.returnsUnit) "" else " Return value: [{}]"}",
        arguments = if (context.method.returnsUnit) emptyList() else listOf(context.returnValue)
    )

    override fun onThrow(context: OnThrowLogContext) = ActionLog(
        message = "Action '${context.action}' has thrown an exception.",
        arguments = listOf(context.exception)
    )

    private fun OnStartLogContext.stringifyParameters() =
        parameters.mapIndexed { index, value -> Pair(method.parameters[index].name, value) }
            .filterIndexed { index, _ -> !method.parameters[index].isAnnotationPresent(Sensitive::class.java) }
            .joinToString { "${it.first}=${it.second}" }
}
