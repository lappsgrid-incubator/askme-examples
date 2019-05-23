package org.lappsgrid.eager.askme.prototype

import groovy.util.logging.Slf4j
import org.lappsgrid.eager.askme.prototype.core.Packet
import org.lappsgrid.eager.askme.prototype.core.Settings
import org.lappsgrid.eager.askme.prototype.workers.Collector
import org.lappsgrid.eager.askme.prototype.workers.Multiplier
import org.lappsgrid.eager.askme.prototype.workers.Ranker
import org.lappsgrid.eager.askme.prototype.workers.Worker
import org.lappsgrid.rabbitmq.Message
import org.lappsgrid.rabbitmq.topic.MailBox
import org.lappsgrid.rabbitmq.topic.PostOffice

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 *  The Manager class is responsible for sending queries to the first stage of the pipeline and shutting down all
 *  threads when processing is complete.
 */
@Slf4j("logger")
class Manager {
    static final String BOX = 'manager'

    void dispatch(PostOffice post) {
        logger.info("Dispatching queries.")
        Settings.DATA.each { String word ->
            logger.debug("Sending {}", word)
            Packet packet = new Packet()
            packet.query = word

            Message message = new Message().body(packet).route(Multiplier.BOX)
            post.send(message)
            sleep(500)
        }
        logger.debug("Dispatched all queries")
    }

    void run() {
        logger.info("Running Manager")
        Object lock = new Object()
        PostOffice post = new PostOffice(Settings.EXCHANGE, Settings.HOST)
        CountDownLatch latch = new CountDownLatch(1)
        List<Worker> workers = [new Multiplier(), new Ranker(), new Collector() ]
        workers*.start()

        // The Collector will send a message to this mailbox when it has collected all documents for all queries.
        MailBox box = new MailBox(Settings.EXCHANGE, BOX, Settings.HOST) {
            void recv(String message) {
                logger.debug("Manager received: {}", message)
                latch.countDown()
            }
        }
        // This delay seem to be necessary or RabbitMQ may lose the first message(s)...
        sleep(500)

        // Send all the queries.
        dispatch(post)

        // Typically we would want to check the return value of the await() call to determine in the latch reached
        // zero (true) or if the timeout limit was reached (false).
        latch.await(5, TimeUnit.SECONDS)

        logger.debug "Stopping the workers."
        workers*.stop()
        post.close()
        box.close()
        sleep(200)
        logger.info "Manager terminating."
    }

    static void main(String[] args) {
        new Manager().run()
    }
}
