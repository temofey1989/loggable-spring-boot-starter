package io.justdevit.spring.logging

import io.justdevit.spring.logging.action.ActionNameResolver
import io.justdevit.spring.logging.action.ActionNameResolverProvider
import io.justdevit.spring.logging.action.DefaultActionNameResolver
import io.justdevit.spring.logging.action.DefaultActionNameResolverProvider
import io.justdevit.spring.logging.writer.ActionLogResolver
import io.justdevit.spring.logging.writer.ConsoleActionLogResolver
import io.justdevit.spring.logging.writer.DefaultLogWriter
import io.justdevit.spring.logging.writer.DefaultLogWriterProvider
import io.justdevit.spring.logging.writer.LogWriter
import io.justdevit.spring.logging.writer.LogWriterProvider
import io.justdevit.spring.logging.writer.LogstashJsonActionLogResolver
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

@Configuration
@EnableAspectJAutoProxy
open class LoggableAutoConfiguration {

    @Bean
    open fun defaultActionNameResolver(): ActionNameResolver =
        DefaultActionNameResolver()

    @Bean
    @ConditionalOnMissingBean
    open fun actionNameResolverProvider(actionNameResolvers: List<ActionNameResolver>) =
        DefaultActionNameResolverProvider(actionNameResolvers)

    // -------------------------------------------------------------------------------------------------------------------

    @Bean(name = ["actionLogResolver", "consoleActionLogResolver"])
    @ConditionalOnProperty(ACTION_LOG_FORMAT_PARAMETER, havingValue = CONSOLE_LOG_FORMAT, matchIfMissing = true)
    open fun consoleActionLogResolver(): ActionLogResolver =
        ConsoleActionLogResolver()

    @Bean(name = ["actionLogResolver", "logstashJsonActionLogResolver"])
    @ConditionalOnProperty(ACTION_LOG_FORMAT_PARAMETER, havingValue = LOGSTASH_JSON_LOG_FORMAT)
    open fun logstashJsonActionLogResolver(): ActionLogResolver =
        LogstashJsonActionLogResolver()

    @Bean
    open fun defaultLogWriter(@Qualifier("actionLogResolver") actionLogResolver: ActionLogResolver): LogWriter =
        DefaultLogWriter(actionLogResolver)

    @Bean
    @ConditionalOnMissingBean
    open fun logWriterProvider(logWriters: List<LogWriter>): LogWriterProvider =
        DefaultLogWriterProvider(logWriters)

    // -------------------------------------------------------------------------------------------------------------------

    @Bean
    @ConditionalOnMissingBean
    open fun loggableMethodInterceptor(
        actionNameResolverProvider: ActionNameResolverProvider,
        logWriterProvider: LogWriterProvider
    ): LoggableMethodInterceptor =
        DefaultLoggableMethodInterceptor(
            actionNameResolverProvider = actionNameResolverProvider,
            logWriterProvider = logWriterProvider
        )

    @Bean
    open fun loggableBeanPostProcessor(interceptor: LoggableMethodInterceptor) =
        LoggableBeanPostProcessor(interceptor)

}
