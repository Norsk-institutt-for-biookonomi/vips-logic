-- Adding this property when adding support for private POIs/weather datasources in VIPS
ALTER TABLE point_of_interest
ADD COLUMN is_private BOOLEAN DEFAULT FALSE;

-- Set all POIS not owned by a superuser or admin to is_private = TRUE
UPDATE point_of_interest 
SET is_private=TRUE
WHERE user_id NOT IN
(
	SELECT DISTINCT user_id FROM user_vips_logic_role
	WHERE vips_logic_role_id IN (1,2)
);