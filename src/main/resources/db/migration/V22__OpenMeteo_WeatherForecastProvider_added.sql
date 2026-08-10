/* 
 * Copyright (c) 2026 NIBIO <http://www.nibio.no/>. 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */
/**
 * Author:  Tor-Einar Skog <tor-einar.skog@nibio.no>
 * Created: 2026-08-10
 */

INSERT INTO public.weather_forecast_provider(weather_forecast_provider_id,name,info_url,description)
VALUES(3,'Open-Meteo','https://open-meteo.com','The Open-Meteo forecast service');