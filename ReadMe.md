# Sic Bo Multiplayer
This is a course project in an [Evolution Scala bootcamp](https://github.com/evolution-gaming/scala-bootcamp). It doesn't have UI, only backend part is implemented. Communication with backend is possible using websocket clients such as `websocat` or special Chrome extensions like [WebSocket King client](https://chrome.google.com/webstore/detail/websocket-king-client/cbcbkhdmedgianpaifchdaddpnmgnknn?hl=en).

For the game rules, check [Wikipedia](https://en.wikipedia.org/wiki/Sic_bo).

## Technical stack
* Akka 
* Akka HTTP (HTTP & WebSockets) 
* Akka Persistence 
* Circe 
* Slick 

### Prerequisites
* PostgreSQL
  
Before starting the project, please manually create a database with name `sicbo`.

Database settings can be modified in [application.conf](src/main/resources/application.conf), by default database is run on `localhost` at port `5432` with user `postgres` and password `password`


### Build
To build, use `sbt run`

### Implemented things
* Every client is an actor.
* Game state is persisted using Persistent Actor and game will recover from same game phase after restart of JVM.
* Player's balance is updating in database after each game, and after player quits the game.
* Betting is possible in game phase `placing_bets` and if player has balance above 0.
* Payout table as in game Super Sic Bo.

## How to interact
### HTTP part
By default, application is run on `127.0.0.1:9000`. You can change this in [application.conf](src/main/resources/application.conf).

At the beginning, there are no registered users, so you need to create one. 
For this, send an HTTP POST request with header `Content-Type: application/json` to endpoint `http://127.0.0.1:9000/register` with body:
```json
{
  "$type" : "register",
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
Connect to endpoind `ws://127.0.0.1:9000/game`. I recommend using Chrome extension for WebSocket part.

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

Placing bet is allowed only during the game phase `placing_bets`

Bet consists of two fields: `amount`, and `bet_type`

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
* Other 
  
`even`, `odd`, `small`, `big`, `any_triple`
```json
{
  "$type": "even"
}
```

Throughout the game, you will be receiving messages from server about the change of game phase
```json
{
  "new_phase": "rolling_dice",
  "$type": "game_phase_changed"
}
```
and the game result itself. Example of such output:
```json
{
  "dice_outcome": {
    "a": 4,
    "b": 3,
    "c": 1
  },
  "winning_bet_types": [
    {
      "$type": "small"
    },
    {
      "$type": "even"
    },
    {
      "num": 1,
      "$type": "number"
    },
    {
      "num": 3,
      "$type": "number"
    },
    {
      "num": 4,
      "$type": "number"
    },
    {
      "num": 8,
      "$type": "total"
    },
    {
      "a": 1,
      "b": 3,
      "$type": "combo"
    },
    {
      "a": 1,
      "b": 4,
      "$type": "combo"
    },
    {
      "a": 3,
      "b": 4,
      "$type": "combo"
    }
  ],
  "total_bet": 0,
  "total_win": 0,
  "balance": {
    "amount": 0
  },
  "username": "MyUsername",
  "$type": "game_result"
}
```

### Few notes
Currently, there are few failing tests that do not affect the logic of backend server. These tests are just a bit incorrectly written or do not reflect the latest changes that I made during the last day.