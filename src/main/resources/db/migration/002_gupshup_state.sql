create table gupshup_state(
	id BIGSERIAL PRIMARY KEY NOT NULL,
	state_form_id BIGSERIAL NOT NULL,
	phone_no VARCHAR(15) NOT NULL,
	state text ,
	previous_path VARCHAR(100) ,
	updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


CREATE INDEX index_state_phone_no ON gupshup_state(phone_no);
