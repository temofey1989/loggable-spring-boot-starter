package io.justdevit.spring.logging.writer

/**
 * Represents logger function arguments.
 *
 * @see org.slf4j.Logger
 */
data class ActionLog(

    /**
     * Logging message.
     */
    val message: String,

    /**
     * Arguments of the logging message.
     */
    val arguments: List<Any?> = emptyList()

)
