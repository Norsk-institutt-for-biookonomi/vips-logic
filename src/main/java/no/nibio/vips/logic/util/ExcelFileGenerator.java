package no.nibio.vips.logic.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.util.ULocale;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.entity.rest.ObservationListItem;
import no.nibio.vips.observationdata.ObservationDataSchema;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class ExcelFileGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final int COL_INDEX_DATE = 0;
    private static final int COL_INDEX_LOCATION = 1;
    private static final int COL_INDEX_OBSERVER_ID = 2;
    private static final int COL_INDEX_OBSERVER_NAME = 3;
    private static final int COL_INDEX_OBSERVATION_TIME_SERIES_ID = 4;
    private static final int COL_INDEX_OBSERVATION_TIME_SERIES_LABEL = 5;
    private static final int COL_INDEX_ORGANISM = 6;
    private static final int COL_INDEX_CROP_ORGANISM = 7;
    private static final int COL_INDEX_HEADING = 8;
    private static final int COL_INDEX_DESCRIPTION = 9;
    private static final int COL_START_INDEX_DATA = 10;

    public static byte[] generateExcel(VipsLogicUser user, ULocale locale, List<ObservationListItem> observations) throws IOException {
        ResourceBundle rb = ResourceBundle.getBundle("no.nibio.vips.logic.i18n.vipslogictexts", locale.toLocale());

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Create main mainSheet for all observations, with header row
            Sheet mainSheet = workbook.createSheet(rb.getString("allObservations"));
            createHeaderRow(mainSheet, rb);

            int mainSheetRowIndex = 1;
            // Add one row for each observation in list of all observations
            for (ObservationListItem item : observations) {
                createItemRow(mainSheet, mainSheetRowIndex++, item);
            }
            autoSizeColumns(mainSheet, 0, COL_INDEX_DESCRIPTION);

            // Prepare list of observations for each type of pest
            Map<Integer, List<ObservationListItem>> pestObservations = getObservationsForEachPest(observations);

            // Create sheets for each individual pest type
            for (Integer pestId : pestObservations.keySet()) {
                List<ObservationListItem> observationsForPest = pestObservations.get(pestId);
                ObservationListItem firstObservationForPest = observationsForPest.get(0);
                String pestName = firstObservationForPest.getOrganismName();
                Sheet pestSheet = workbook.createSheet(pestName);
                Row headerRow = createHeaderRow(pestSheet, rb);

                // Add column titles for observation data
                Map<String, String> dataColumnTitles = getObservationDataColumnTitles(firstObservationForPest.getObservationDataSchema());
                int pestSheetColIndex = COL_START_INDEX_DATA;
                for (String key : dataColumnTitles.keySet()) {
                    headerRow.createCell(pestSheetColIndex++).setCellValue(dataColumnTitles.get(key));
                }

                int pestSheetRowIndex = 1;
                for (ObservationListItem item : observationsForPest) {
                    Row row = createItemRow(pestSheet, pestSheetRowIndex++, item);

                    if (item.getObservationData() != null) {
                        Map<String, Object> observationDataMap = objectMapper.readValue(item.getObservationData(), HashMap.class);
                        if (observationDataMap != null) {
                            pestSheetColIndex = COL_START_INDEX_DATA;
                            for (String key : dataColumnTitles.keySet()) {
                                Object value = observationDataMap.get(key);
                                if (value instanceof Number) {
                                    row.createCell(pestSheetColIndex++).setCellValue(((Number) value).intValue());
                                } else {
                                    row.createCell(pestSheetColIndex++).setCellValue(value != null ? value.toString() : "");
                                }
                            }
                        }
                    }
                }
                autoSizeColumns(pestSheet, COL_INDEX_DATE, COL_START_INDEX_DATA + dataColumnTitles.size());
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Auto-size columns of given sheet, from startIndex to endIndex, to ensure that content fits
     *
     * @param sheet      The sheet of which to auto-size columns
     * @param startIndex The index of the first column to auto-size
     * @param endIndex   The index of the last column to auto-size
     */
    private static void autoSizeColumns(Sheet sheet, int startIndex, int endIndex) {
        for (int i = startIndex; i <= endIndex; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Create map with data property name as key, and corresponding localized name as value
     *
     * @param observationDataSchema The observation data schema which contains localized names and other info
     * @return result map
     */
    private static Map<String, String> getObservationDataColumnTitles(ObservationDataSchema observationDataSchema) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(observationDataSchema.getDataSchema());
        JsonNode propertiesNode = rootNode.path("properties");
        Map<String, String> resultMap = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = propertiesNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            String value = field.getValue().path("title").asText();
            resultMap.put(key, value);
        }
        return resultMap;
    }

    /**
     * Find the name of the point of interest in given geoInfo string
     *
     * @param geoInfo The geoInfo which might contain the name of the point of interest
     * @return the point of interest name, or empty string
     */
    private static String getPointOfInterestName(String geoInfo) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(geoInfo);
        JsonNode featuresNode = rootNode.path("features");
        if (featuresNode.isArray() && !featuresNode.isEmpty()) {
            JsonNode firstFeature = featuresNode.get(0);
            JsonNode propertiesNode = firstFeature.path("properties");
            return propertiesNode.path("pointOfInterestName").asText();
        }
        return "";
    }

    /**
     * Create map with pestId as key, and list of corresponding observations as value
     *
     * @param observations The original list of observations
     * @return result map
     */
    private static Map<Integer, List<ObservationListItem>> getObservationsForEachPest(List<ObservationListItem> observations) {
        Map<Integer, List<ObservationListItem>> pestObservations = new HashMap<>();
        for (ObservationListItem observation : observations) {
            Integer pestId = observation.getOrganismId();
            if (!pestObservations.containsKey(pestId)) {
                List<ObservationListItem> observationList = new ArrayList<>();
                observationList.add(observation);
                pestObservations.put(pestId, observationList);
            } else {
                pestObservations.get(pestId).add(observation);
            }
        }
        return pestObservations;
    }

    /**
     * Create first row of given sheet, with standard set of column titles
     *
     * @param sheet The sheet to which a row will be added
     * @param rb    A resource bundle enabling localized messages
     * @return the newly created header row
     */
    public static Row createHeaderRow(Sheet sheet, ResourceBundle rb) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(COL_INDEX_DATE).setCellValue(rb.getString("timeOfObservation"));
        headerRow.createCell(COL_INDEX_LOCATION).setCellValue(rb.getString("location"));
        headerRow.createCell(COL_INDEX_OBSERVER_ID).setCellValue(rb.getString("observerId"));
        headerRow.createCell(COL_INDEX_OBSERVER_NAME).setCellValue(rb.getString("observer"));
        headerRow.createCell(COL_INDEX_OBSERVATION_TIME_SERIES_ID).setCellValue(rb.getString("observationTimeSeriesId"));
        headerRow.createCell(COL_INDEX_OBSERVATION_TIME_SERIES_LABEL).setCellValue(rb.getString("observationTimeSeriesLabel"));
        headerRow.createCell(COL_INDEX_ORGANISM).setCellValue(rb.getString("organism"));
        headerRow.createCell(COL_INDEX_CROP_ORGANISM).setCellValue(rb.getString("cropOrganismId"));
        headerRow.createCell(COL_INDEX_HEADING).setCellValue(rb.getString("observationHeading"));
        headerRow.createCell(COL_INDEX_DESCRIPTION).setCellValue(rb.getString("observationText"));
        return headerRow;
    }

    /**
     * Create row with given index, for given observation list item
     *
     * @param sheet    The sheet to which a row will be added
     * @param rowIndex The index of the row
     * @param item     The item of which to add data
     * @return the newly created row
     */
    private static Row createItemRow(Sheet sheet, int rowIndex, ObservationListItem item) throws JsonProcessingException {
        LocalDate localDateOfObservation = item.getTimeOfObservation().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String pointOfInterestName = getPointOfInterestName(item.getGeoInfo());

        Row row = sheet.createRow(rowIndex);
        row.createCell(COL_INDEX_DATE).setCellValue(localDateOfObservation.format(DATE_FORMATTER));
        row.createCell(COL_INDEX_LOCATION).setCellValue(pointOfInterestName);
        row.createCell(COL_INDEX_OBSERVER_ID).setCellValue(item.getObserverId());
        row.createCell(COL_INDEX_OBSERVER_NAME).setCellValue(item.getObserverName());

        if(item.getObservationTimeSeriesId() != null) {
            row.createCell(COL_INDEX_OBSERVATION_TIME_SERIES_ID).setCellValue(item.getObservationTimeSeriesId());
            row.createCell(COL_INDEX_OBSERVATION_TIME_SERIES_LABEL).setCellValue(item.getObservationTimeSeriesLabel());
        }

        row.createCell(COL_INDEX_ORGANISM).setCellValue(item.getOrganismName());
        row.createCell(COL_INDEX_CROP_ORGANISM).setCellValue(item.getCropOrganismName());
        row.createCell(COL_INDEX_HEADING).setCellValue(item.getObservationHeading());
        row.createCell(COL_INDEX_DESCRIPTION).setCellValue(item.getObservationText());
        return row;
    }

}
