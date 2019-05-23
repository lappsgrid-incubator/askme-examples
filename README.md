# AskMe : Tickets

This project provides a simple example of tracking and collecting multiple documents as they are passed through a processing pipeline.  The pipeline consists of the following classes:

1. `Manager` - sends *queries* to the first stage of the pipeline and then waits for the `Collector` to send a notification when all processing is complete.  For the purposes of this example a *query* is simply a single word: *who*, *what*, *where*, *when*, and *why*.
1. `Multiplier` - the first stage of the processing pipeline.  The `Multiplier` takes each query word and generates between 10 and 20 *documents*.
1. `Ranker` assigns a random *score* to each document.
1. `Collector` - collects all the documents for each query and when all have been recieved it sorts and prints them. When all the documents have been collected the `Collector` sends a message to the `Manager` to tell it to stop all running threads and terminate the program.

## Tickets

When the `Multiplier` receives a query from the `Manager` it assigns a random ID value to the query and then numbers each of the documents it generates.  This allows the `Collector` to determine  when all of the documents for a query have been collected.  This information is stored in the `Ticket` class.

```groovy
class Ticket {
    String id
    int n
    int size
}
```
