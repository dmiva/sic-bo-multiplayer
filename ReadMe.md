# Sic Bo multiplayer
This is a course project in a Evolution Scala bootcamp. It has no UI, only backend  part is implemented. Communication with backend is possible using websocket clients such as websocat or any other client like Chrome extension WebSocket King client.

## Tech stack
* Akka
* Akka HTTP (WebSockets)
* Akka Persistence  
* Circe
* Slick


### Requirements
* PostgreSQL 
  
Upon starting, please manually create a database with name `sicbo`.
Database settings can be modified in [application.conf](src/main/resources/application.conf), by default database is run on `localhost` at port `5432` with user `postgres` and password `password`


### Build
To build, use `sbt run`

## How to interact

