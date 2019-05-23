package org.lappsgrid.eager.askme.prototype.workers

import groovy.util.logging.Slf4j
import org.lappsgrid.eager.askme.prototype.core.Packet
import org.lappsgrid.eager.askme.prototype.core.Settings
import org.lappsgrid.rabbitmq.Message
import org.lappsgrid.rabbitmq.topic.MailBox
import org.lappsgrid.rabbitmq.topic.PostOffice
import org.lappsgrid.serialization.Serializer

/**
 * The Ranker accepts incoming documents and assigns a random score to each one.
 */
@Slf4j("logger")
class Ranker extends Worker {
    static final String BOX = 'ranker'

    void run() {
        this.name = BOX
        logger.info "Starting the ranker."
        PostOffice post = new PostOffice(Settings.EXCHANGE, Settings.HOST)
        Random random = new Random()
        MailBox box = new MailBox(Settings.EXCHANGE, BOX, Settings.HOST) {
            @Override
            void recv(String json) {
                Message message = Serializer.parse(json, Message)
                Packet packet = new Packet(message.body)
                logger.debug("Ranking {}", packet.document)
                packet.score = random.nextInt(100)

                message = new Message().body(packet).route(Collector.BOX)
                post.send(message)
            }
        }

        block()
        logger.debug "Ranker closing."
        box.close()
        post.close()
        logger.info "Ranker terminated."
    }
}
