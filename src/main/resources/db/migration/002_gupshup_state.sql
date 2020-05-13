create table gupshup_state(
	id BIGSERIAL PRIMARY KEY NOT NULL,
	phone_no VARCHAR(15) NOT NULL,
	state VARCHAR(2000) NOT NULL,
	updated_at timestamp without time zone
);


CREATE INDEX index_state_phone_no ON gupshup_state(phone_no);
