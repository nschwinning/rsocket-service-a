CREATE TABLE quote
(
    id                SERIAL	      PRIMARY KEY,
    created_at        timestamp    	  not null,
    message  		  varchar(255)    not null,
    quote_id          int4            not null
);