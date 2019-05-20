package org.lappsgrid.eager.askme.prototype.core

/**
 * Ticket objects are used to group documents for each query.  Every query will recieve a unique ID (UUID) and each
 * document in a query will be numbered.
 */
class Ticket {
    String id
    int n
    int size

    Ticket() { }
    Ticket(Object object) {
        if (object == null) {
            return
        }

        // TODO Should check that object is instanceof Map.
        //  Although if object is not a Map the best we can do is throw a ClassCastException, which is exactly what
        //  happens currently.
        Map map = (Map) object
        this.id = map.id
        this.n = map.n
        this.size = map['size']
    }
}
