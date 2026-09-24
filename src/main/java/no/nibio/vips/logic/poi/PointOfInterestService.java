package no.nibio.vips.logic.poi;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import java.util.ResourceBundle;

public interface PointOfInterestService {

    Kml getWeatherStationsKml(
            Integer organizationId,
            Integer excludeWeatherStationId,
            Integer highlightWeatherStationId,
            String serverName,
            ResourceBundle i18nBundle);

    Kml getPoisKml(
            Integer organizationId,
            Integer excludePoiId,
            Integer highlightPoiId,
            String serverName,
            ResourceBundle i18nBundle);

    Kml getPoisForOrganization(
            Integer organizationId,
            Integer excludePoiId,
            Integer highlightPoiId,
            String serverName,
            ResourceBundle i18nBundle,
            Integer pointOfInterestTypeId);
}
