package org.lappsgrid.eager.askme.prototype

import groovy.util.logging.Slf4j
import org.lappsgrid.eager.askme.prototype.core.Packet
import org.lappsgrid.eager.askme.prototype.core.Settings
import org.lappsgrid.eager.askme.prototype.workers.Collector
import org.lappsgrid.eager.askme.prototype.workers.Provider
import org.lappsgrid.eager.askme.prototype.workers.Ranker
import org.lappsgrid.eager.askme.prototype.workers.Worker
import org.lappsgrid.rabbitmq.Message
import org.lappsgrid.rabbitmq.topic.MailBox
import org.lappsgrid.rabbitmq.topic.PostOffice

/**
 *  The Manager class is responsible for sending queries to the first stage of the pipeline and shutting down all
 *  threads when processing is complete.
 */
@Slf4j("logger")
class Manager {
    static final String BOX = 'manager'

    void dispatch(PostOffice post) {
        ['who', 'what', 'where', 'when', 'why'].each { String word ->
            logger.debug "Sending $word"
            Packet packet = new Packet()
            packet.query = word

            Message message = new Message().body(packet).route(Provider.BOX)
            post.send(message)
        }
    }

    void run() {
        Object lock = new Object()
        PostOffice post = new PostOffice(Settings.EXCHANGE, Settings.HOST)

        List<Worker> workers = [new Provider(), new Ranker(), new Collector() ]
        workers*.start()

        // The Collector will send a message to this mailbox when it has collected all documents for all queries.
        MailBox box = new MailBox(Settings.EXCHANGE, BOX, Settings.HOST) {
            void recv(String message) {
                logger.info "Manager received: $message"
                synchronized (lock) {
                    logger.trace "Notify the lock."
                    lock.notify()
                    logger.trace "Done."
                }
            }
        }

        // Send all the queries.
        dispatch(post)

        // Wait for our lock to be released.
        synchronized (lock) {
            logger.trace "Waiting on the lock"
            lock.wait()
            logger.trace "Done waiting."
        }

        logger.debug "Stopping the workers."
        workers*.stop()
        post.close()
        box.close()
        sleep(200)
        logger.info "Manager terminating."
    }

    static void main(String[] args) {
        System.setProperty('RABBIT_USERNAME', 'uploader')
        System.setProperty('RABBIT_PASSWORD', 'mjBh}oEP1IF.MAWb')
        new Manager().run()
    }
}
