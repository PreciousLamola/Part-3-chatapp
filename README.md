# PROG5121POE — QuickChat

QuickChat is a Java console application that continues the Part 2 project and adds the Part 3 stored-message features.

## Features

- Register and log in.
- Send, disregard, or store messages.
- View sent, disregarded, and stored messages.
- Find the longest stored message.
- Search by message ID or recipient.
- Delete a message using its hash.
- Display a full report of stored messages.
- Save and load stored messages as JSON.

## Requirements

- JDK 17 or newer
- Apache Maven

## Run

From the folder containing `pom.xml`, run:

```sh
mvn test
mvn exec:java
