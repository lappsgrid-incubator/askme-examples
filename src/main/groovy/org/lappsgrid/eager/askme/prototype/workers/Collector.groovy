package org.lappsgrid.eager.askme.prototype.workers

import groovy.util.logging.Slf4j
import org.lappsgrid.eager.askme.prototype.Manager
import org.lappsgrid.eager.askme.prototype.core.Packet
import org.lappsgrid.eager.askme.prototype.core.Settings
import org.lappsgrid.rabbitmq.Message
import org.lappsgrid.rabbitmq.topic.MailBox
import org.lappsgrid.rabbitmq.topic.PostOffice
import org.lappsgrid.serialization.Serializer

/**
 *  The Collector accepts the ranked documents and collects them until all parts have been recieved. When all the
 *  documents for a query have been recieved they are sorted and printed.
 */
@Slf4j("logger")
class Collector extends Worker {
    static final String BOX = 'collector'

    void run() {
        this.name = BOX
        logger.info "Starting $BOX"
        PostOffice post = new PostOffice(Settings.EXCHANGE, Settings.HOST)
        Map tickets = [:]
        int completed = 0;

        MailBox box = new MailBox(Settings.EXCHANGE, BOX, Settings.HOST) {
            void recv(String json) {
                Message message = Serializer.parse(json, Message)
                Packet packet = new Packet(message.body)
                String ticket = packet.ticket.id
                logger.trace("Collecting {}: {}", packet.ticket.n, packet.document)
                List packets = tickets[ticket]
                if (packets == null) {
                    packets = []
                    tickets[ticket] = packets
                }
                packets.add(packet)
                if (packets.size() == packet.ticket.size) {
                    // we have received all the packets for this ticket.
                    ++completed
                    println "Complete ${completed}: ${packet.query} ${packets.size()} ${packet.ticket.id}"
                    packets.sort { a,b -> b.score <=> a.score }.each { Packet p ->
                        println p
                    }
                    println()
                }

                if (completed == Settings.DATA.size()) {
                    logger.info "Processing complete. Terminate the pipeline."
                    message = new Message().body("quit").route(Manager.BOX)
                    post.send(message)
                }
            }
        }

        block()
        logger.debug "Closing the collector"
        box.close()
        post.close()
        logger.info "Collector terminated."
    }
}
