/*CREATE KEYSPACE accrue2 WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };*/

CREATE TABLE ohlc (ohlc_id text, symbol text, time timestamp, open decimal, high decimal, low decimal, close decimal, volume bigint, interval decimal, PRIMARY KEY(ohlc_id, time));

CREATE TABLE symbols(symbol text,description text,exchange text,listed_market text,security_type text, PRIMARY KEY (symbol));
