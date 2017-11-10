#!/usr/bin/env bash

docker run --name postgres -e POSTGRES_PASSWORD=password -e POSTGRES_USER=user -e POSTGRES_DB=mydb -p 5432:5432 -d postgres