# Sic Bo Multiplayer
This is a course project in an [Evolution Scala bootcamp](https://github.com/evolution-gaming/scala-bootcamp). It has no UI, only backend  part is implemented. Communication with backend is possible using websocket clients such as websocat or any other client like Chrome extension WebSocket King client.

## Tech stack
* Akka
* Akka HTTP (HTTP & WebSockets)
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
At the beginning, there are no registered users, so you need to create one. To do this, you need to send a HTTP POST request with this JSON body:

```dtd
{
  "username" : "MyName",
  "password" : "MyPassword"
}
```
or  use 
```
curl -X POST -H "Content-Type: application/json" -d '{"&type":"register", "username":"MyUsername", "password":"MyPassword"}' http://127.0.0.1:9000/register
```
