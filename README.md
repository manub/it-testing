# Integration Testing using sbt, Scalatest and Docker

This is the code accompanying my talk at Scala eXchange 2017, and it's a simple HTTP service that can save a user and retrieve the list of all saved users.

Tests can be run by using `sbt it:test`

You can run the application using `sbt run` (make sure you start Postgres accordingly by using `./start-postgres.sh`)

- To get all the users you can invoke `GET localhost:8080/users`.
- To save a user, `POST localhost:8080/users` with a JSON payload such as:

```json
{
  "username": "manub",
  "firstName": "Emanuele",
  "lastName": "Blanco"
}
``` 
