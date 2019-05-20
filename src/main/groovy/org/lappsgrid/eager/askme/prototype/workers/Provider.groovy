package org.lappsgrid.eager.askme.prototype.workers

import groovy.util.logging.Slf4j
import org.lappsgrid.eager.askme.prototype.core.Ticket
import org.lappsgrid.eager.askme.prototype.core.Packet
import org.lappsgrid.eager.askme.prototype.core.Settings
import org.lappsgrid.rabbitmq.*
import org.lappsgrid.rabbitmq.topic.MailBox
import org.lappsgrid.rabbitmq.topic.PostOffice
import org.lappsgrid.serialization.Serializer

/**
 * The Provider service accepts incoming queries and generates between 10 and 20 documents for each.
 */
@Slf4j("logger")
class Provider extends Worker {
    static final String BOX = 'provider'


    void run() {
        this.name = BOX
        logger.info "Staring the document provider"
        Random random = new Random()
        PostOffice post = new PostOffice(Settings.EXCHANGE, Settings.HOST)

        MailBox box = new MailBox(Settings.EXCHANGE, BOX, Settings.HOST) {
            void recv(String json) {
                // Generate the ID for this query.
                String id = UUID.randomUUID().toString()
                Message message = Serializer.parse(json, Message)
                Packet packet = new Packet(message.body)
                // The number of documents to generate for this query.
                int size = random.nextInt(10) + 10
                for (int i = 0; i < size; ++i) {
                    // Generate a ticket for the document.
                    Ticket ticket = new Ticket()
                    ticket.id = id
                    ticket.size = size
                    ticket.n = i + 1
                    packet.ticket = ticket
                    packet.document = packet.query + " document " + i
                    logger.trace "Posting ${packet.document}"
                    message = new Message().body(packet).route(Ranker.BOX)
                    post.send(message)
                }

            }
        }

        // Block until the manager releases the lock
        block()

        logger.debug "Provider shutting down."
        box.close()
        post.close()
        logger.info "Provider terminated."
    }

}
