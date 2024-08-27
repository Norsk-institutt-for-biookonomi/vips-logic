-- Adding this property when adding support for gridded weather datasources in VIPS
ALTER TABLE forecast_configuration
ADD COLUMN use_grid_weather_data BOOLEAN DEFAULT FALSE;