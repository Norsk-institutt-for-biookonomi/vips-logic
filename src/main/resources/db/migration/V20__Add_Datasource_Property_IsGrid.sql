-- Adding this property when adding support for gridded weather datasources in VIPS
ALTER TABLE weather_station_data_source
ADD COLUMN is_grid BOOLEAN DEFAULT FALSE;