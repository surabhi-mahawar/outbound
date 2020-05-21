create table bot_form(
	id BIGSERIAL PRIMARY KEY NOT NULL,
	form_id BIGSERIAL PRIMARY KEY NOT NULL,
	name VARCHAR(15) NOT NULL,
	welcome_message text ,	
	wrong_default_message VARCHAR(100);	
);


CREATE INDEX index_form_name ON bot_form(name);
