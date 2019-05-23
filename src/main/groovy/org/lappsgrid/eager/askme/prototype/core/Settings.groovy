package org.lappsgrid.eager.askme.prototype.core

/**
 * Common settings that never change during the lifetime of a program.
 */
class Settings {
    /** Queries that will be sent through the pipeline. */
    static final String[] DATA = ['who', 'what', 'where', 'when', 'why']
    /** Message exchange on the RabbitMQ server. */
    static final String EXCHANGE = 'askme.prototype'
    /** Hostname of the RabbitMQ server. */
    static final String HOST = 'rabbitmq.lappsgrid.org'
//    static final String HOST = "localhost"
}
