package org.lappsgrid.eager.askme.prototype.core
/**
 *  Data object that is passed between the services.  The Ticket is used to keep track of all the
 *  separated documents that belong to a particular query.
 */
class Packet {
    Ticket ticket
    String query
    String document
    int score

    Packet() { }

    Packet(Object object) {
        Map map = (Map) object
        this.ticket = new Ticket(map.ticket)
        this.query = map.query
        this.document = map.document
        this.score = map.score
    }

    String toString() {
        return "${ticket.n}/${ticket.size} $document $score".toString()
    }
}
