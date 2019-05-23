# AskMe : Tickets

This project provides a simple example of tracking and collecting multiple documents as they are passed through a processing pipeline.  The pipeline consists of the following classes:

1. `Manager` - sends *queries* to the first stage of the pipeline and then waits for the `Collector` to send a notification when all processing is complete.  For the purposes of this example a *query* is simply a single word: *who*, *what*, *where*, *when*, and *why*.
