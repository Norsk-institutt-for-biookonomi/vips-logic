-- Adding this property when adding support for gridded weather datasources in VIPS
ALTER TABLE organization
ADD COLUMN default_grid_weather_station_data_source_id INTEGER REFERENCES weather_station_data_source(weather_station_data_source_id) DEFAULT NULL;