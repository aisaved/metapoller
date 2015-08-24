/*user tables*/
CREATE TABLE user_account(
       user_account_id serial PRIMARY KEY,
       username VARCHAR (355) UNIQUE NOT NULL,
       password VARCHAR (1024) NOT NULL,
       email VARCHAR (355) UNIQUE NOT NULL,
       created_on TIMESTAMP NOT NULL,
       is_admin BOOLEAN NOT NULL default false,
       active BOOLEAN NOT NULL default false,
       last_login TIMESTAMP);

CREATE TABLE user_registration(
       user_registration_id serial PRIMARY KEY,
       user_account_id integer NOT NULL,
       registration_key VARCHAR (255),
       activated BOOLEAN NOT NULL default false,
       CONSTRAINT user_registration_user_account_id_fkey FOREIGN KEY (user_account_id)
       REFERENCES user_account (user_account_id) MATCH SIMPLE 
       ON DELETE CASCADE
       );


CREATE TABLE user_profile(
       user_profile_id serial PRIMARY KEY,
       user_account_id integer NOT NULL,
       first_name VARCHAR (100),
       middle_name VARCHAR (100),
       last_name VARCHAR (100),
       address_line_1 VARCHAR (255),
       address_line_2 VARCHAR (255),
       city VARCHAR (255),
       state_province VARCHAR (255),
       country_code VARCHAR (3),
       CONSTRAINT user_profile_user_account_id_fkey FOREIGN KEY (user_account_id)
       REFERENCES user_account (user_account_id) MATCH SIMPLE 
       ON DELETE CASCADE
       );

CREATE TABLE user_session(
       user_session_id serial PRIMARY KEY,
       user_account_id integer NOT NULL,
       auth_token VARCHAR(64),
       session_expiry TIMESTAMP NOT NULL,
       CONSTRAINT user_session_user_account_id_fkey FOREIGN KEY (user_account_id)
       REFERENCES user_account (user_account_id) MATCH SIMPLE 
       ON DELETE CASCADE
       );


CREATE TABLE user_password_reset(
       user_password_reset_id serial PRIMARY KEY,
       user_account_id integer NOT NULL,
       password_reset_key VARCHAR(32),
       key_expiry TIMESTAMP NOT NULL,
       CONSTRAINT user_password_reset_user_account_id_fkey FOREIGN KEY (user_account_id)
       REFERENCES user_account (user_account_id) MATCH SIMPLE 
       ON DELETE CASCADE
       );


/*symbols*/
CREATE TABLE symbols (
       symbol_id serial PRIMARY KEY,
       symbol varchar(20),
       name varchar(100),
       sector varchar(250),
       industry varchar(250),
       market_cap decimal (16,2));

CREATE INDEX symbol_name_index ON symbols(symbol);


/*API*/
CREATE TABLE accrue_app(
       app_id serial PRIMARY KEY,
       app_key varchar(1024) NOT NULL,
       app_secret varchar(1024),
       app_title varchar(1024),
       app_description text       
       );

CREATE TABLE accrue_app_token(
       app_token_id serial PRIMARY KEY,
       app_id integer,
       app_auth_token varchar(1024),
       app_auth_token_expiry TIMESTAMP,
       CONSTRAINT accrue_app_token_accrue_app_id_fkey FOREIGN KEY (app_id)
       REFERENCES accrue_app (app_id) MATCH SIMPLE
       ON DELETE CASCADE
       );

/*events*/
