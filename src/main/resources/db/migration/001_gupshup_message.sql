create table gupshup_message(
	id BIGSERIAL PRIMARY KEY NOT NULL,
	phone_no VARCHAR(15) NOT NULL,
	msg_id VARCHAR(200) NOT NULL,
	updated_at timestamp without time zone,
	message VARCHAR(10000) NOT NULL,
	is_last_message boolean not null	
);


CREATE INDEX index_phone_no ON gupshup_message(phone_no);
