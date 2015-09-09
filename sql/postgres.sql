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


CREATE TABLE poll(
       poll_id serial PRIMARY KEY,
       poll_title VARCHAR (1024) NOT NULL,
       poll_hash_tag VARCHAR (1024) NOT NULL,
       poll_description text,
       poll_created_date TIMESTAMP NOT NULL,
       poll_count integer DEFAULT 0,
       poll_total integer NOT NULL DEFAULT 0
       );


CREATE TABLE user_poll(
       user_poll_id serial PRIMARY KEY,
       poll_id integer NOT NULL,
       user_account_id integer NOT NULL,
       user_poll_date TIMESTAMP NOT NULL DEFAULT now(),
       user_poll_vote integer NOT NULL,
       CHECK (user_poll_vote=1 OR user_poll_vote=-1),
       CONSTRAINT user_poll_poll_id_fkey FOREIGN KEY (poll_id)
       REFERENCES poll (poll_id) MATCH SIMPLE 
       ON DELETE CASCADE,
       CONSTRAINT user_poll_user_account_id_fkey FOREIGN KEY (user_account_id)
       REFERENCES user_account (user_account_id) MATCH SIMPLE 
       ON DELETE CASCADE
       );

CREATE TABLE user_poll_log(
       user_poll_log_id serial PRIMARY KEY,
       user_account_id integer NOT NULL,
       next_poll_time TIMESTAMP NOT NULL,
       CONSTRAINT user_poll_log_user_account_id_fkey FOREIGN KEY (user_account_id)
       REFERENCES user_account (user_account_id) MATCH SIMPLE 
       ON DELETE CASCADE
       );


CREATE TABLE facebook_account(
       facebook_account_id serial PRIMARY KEY,
       user_account_id integer,
       facebook_id varchar(20) NOT NULL,
       facebook_email varchar(256),
       facebook_name varchar(256),
       facebook_access_token varchar(1024)
);


CREATE TABLE twitter_account(
       twitter_account_id serial PRIMARY KEY,
       user_account_id integer,
       twitter_id varchar(20) NOT NULL,
       
);


