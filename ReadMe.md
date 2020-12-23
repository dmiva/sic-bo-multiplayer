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
### HTTP part
By default, application is run on `localhost:9000`. You can also alter this in 
At the beginning, there are no registered users, so you need to create one. 
For this, send an HTTP POST request to endpoint `localhost:9000/register` with body:

```json
{
  "username" : "MyName",
  "password" : "MyPassword"
}
```
or if you prefer `curl`
```
curl -X POST -H "Content-Type: application/json" -d '{"&type":"register", "username":"MyUsername", "password":"MyPassword"}' http://127.0.0.1:9000/register
```
### WebSocket part
After a player is registered, you can switch to WebSocket client and interact with backend.
Connect to endpoind `ws://127.0.0.1:9000/game`.
To connect with websocat, use `websocat ws://127.0.0.1:9000/game`

Available commands to player:
* Login
```json
{
  "$type": "login",
  "username": "MyName",
  "password": "MyPassword"
}
```
After login, game will start, and player is able to send commands
* Logout (to exit game)
```json
{
  "$type": "logout",
  "username": "MyUsername"
}
```
* Place Bet
Bet consists of `amount`, and `bet_type`

Example with bet `Any triple`
```json
{
  "bets": [
    {
      "amount": 10,
      "bet_type": {
        "$type": "any_triple"
      }
    }
  ],
  "$type": "place_bet"
}
```

Example with bet `Combo(3,6)`
```json
{
  "bets" : [
    {
      "amount" : 10,
      "bet_type" : {
        "$type" : "combo",
        "a" : 3,
        "b" : 6
      }
    }
  ],
  "$type" : "place_bet"
}
```

List of available bet types:
* Number 
  Where `num` field is from 1 to 6
```json
{
  "$type": "number",
  "num": 1,
}
```
* Combo
Where `a` and `b` are not equal, from 1 to 6, and `b` greater than `a`
```json
{
  "type": "combo",
  "a": 3,
  "b": 5
}
```
* Total 
  Where `num` is from 4 to 17
  
```json
{
  "$type": "total",
  "num": 4,
}
```
* Double
  Where `num` is from 1 to 6

```json
{
  "$type": "double",
  "num": 4,
}
```
* Triple
  Where `num` is from 1 to 6

```json
{
  "$type": "triple",
  "num": 4,
}
```
* Other bets - `even`, `odd`, `small`, `big`, `any_triple`
```json
{
  "$type": "even"
}
```

