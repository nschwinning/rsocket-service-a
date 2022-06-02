## Overview

RSocket client that can be used to fetch data from an RSocket Server. 

The server has a Kafka Listener registered that listens to topic "quotes" for updates about quotes. Whenever an 
event comes in RSocket is being used to fetch the quote details from the server. The quote will be saved to the database 
using the R2DBC connector. 

Quotes can also be fetched directly from the server using RSockets. See the examples for more details.

## Example Usage

The following request will get 5 quotes directly from the server, stores them to the DB and returns them as JSON:

```
curl --location --request GET 'http://localhost:9392/quotes/5'
```

All quotes from db can be retrieved as follows:

```
curl --location --request GET 'http://localhost:9392/quotes'
```

## Docker Compose

To setup the database and Kafka use the Docker Compose file in the docker-compose folder.

## Database

The service uses R2DBC database connector. The database can be configured using the following properties:

```
spring.r2dbc.url=r2dbc:postgresql://localhost:5432/smexnet?schema=reactive
spring.r2dbc.username=smexnet
spring.r2dbc.password=smexnet
```

### Flyway

The service uses Flyway to setup the database. 

### Manual setup

R2DBC will not setup the database for you from the ORM, so if you have to do it manually use the following script:

``` 
SET search_path = "reactive";

CREATE TABLE quote
(
    id                SERIAL	      PRIMARY KEY,
    created_at        timestamp    	  not null,
    message  		  varchar(255)    not null,
    quote_id          int4            not null
);

grant select on quote to smexnet;
grant insert on quote to smexnet;
grant delete on quote to smexnet;

grant select on quote_id_seq to smexnet;
grant usage on quote_id_seq to smexnet;
grant update on quote_id_seq to smexnet;
```

## Useful Links

- https://github.com/dsyer/rsocket-test-server
- https://spring.io/blog/2021/06/02/wiremock-for-rsocket
- https://spring.io/blog/2020/05/25/getting-started-with-rsocket-testing-spring-boot-responders
- https://spring.io/blog/2020/06/17/getting-started-with-rsocket-spring-security
- https://github.com/spring-tips/rsocket-security