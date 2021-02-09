CREATE TABLE public.db_update (
	db_update_id INTEGER PRIMARY KEY,
	title VARCHAR(255),
	description TEXT,
	last_update TIMESTAMP WITH TIME ZONE
);

INSERT INTO public.db_update(db_update_id, title, description, last_update)
VALUES(1, 'Crop categories and organisms', 'Changes have occurred in the crop categories and organisms subsystem', NULL);