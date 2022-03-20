- https://github.com/dsyer/rsocket-test-server
- https://spring.io/blog/2021/06/02/wiremock-for-rsocket
- https://spring.io/blog/2020/05/25/getting-started-with-rsocket-testing-spring-boot-responders

## DB Script

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