package io.justdevit.spring.logging.writer

import io.justdevit.spring.logging.RETURN_VALUE_NAME
import io.justdevit.spring.logging.Sensitive
import io.justdevit.spring.logging.extension.hasParameters
import io.justdevit.spring.logging.extension.returnsUnit
import net.logstash.logback.argument.StructuredArguments

/**
 * Action log resolver for Logstash JSON format.
 *
 * @see ActionLogResolver
 */
class LogstashJsonActionLogResolver : ActionLogResolver {

    override fun onStart(context: OnStartLogContext) = ActionLog(
        message = "Action '${context.action}' has started.",
        arguments = if (context.method.hasParameters) context.toLogArguments() else emptyList()
    )

    override fun onFinish(context: OnFinishLogContext) = ActionLog(
        message = "Action '${context.action}' has successfully finished.",
        arguments = if (context.method.returnsUnit) emptyList() else listOf(context.returnValue.toLogAttribute())
    )

    override fun onThrow(context: OnThrowLogContext) = ActionLog(
        message = "Action '${context.action}' has thrown an exception.",
        arguments = listOf(context.exception)
    )

    private fun OnStartLogContext.toLogArguments() =
        parameters.mapIndexed { index, value -> StructuredArguments.value(method.parameters[index].name, value) }
            .filterIndexed { index, _ -> !method.parameters[index].isAnnotationPresent(Sensitive::class.java) }

    private fun Any?.toLogAttribute() =
        if (this == null || this is Unit) null else StructuredArguments.value(RETURN_VALUE_NAME, this)
}
