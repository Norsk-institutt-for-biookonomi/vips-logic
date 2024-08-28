package no.nibio.vips.logic.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.util.ULocale;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.entity.rest.ObservationListItem;
import no.nibio.vips.observationdata.ObservationDataSchema;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class ExcelFileGenerator {

    private static Logger LOGGER = LoggerFactory.getLogger(ExcelFileGenerator.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private enum ColumnIndex {
        ID(false, 0, 0, "observationId"),
        DATE(false, 1, 1, "timeOfObservation"),
        POI_NAME(false, 2, 2, "location"),
        OBSERVER_ID(true, null, 3, "observerId"),
        OBSERVER_NAME(true, null, 4, "observer"),
        OBSERVATION_TIME_SERIES_ID(false, 3, 5, "observationTimeSeriesId"),
        OBSERVATION_TIME_SERIES_LABEL(false, 4, 6, "observationTimeSeriesLabel"),
        ORGANISM(false, 5, 7, "organism"),
        CROP_ORGANISM(false, 6, 8, "cropOrganismId"),
        HEADING(false, 7, 9, "observationHeading"),
        DESCRIPTION(false, 8, 10, "observationText"),
        BROADCAST(false, 9, 11, "isBroadcast"),
        POSITIVE(false, 10, 12, "isPositiveRegistration"),
        INDEX_DATA(false, 11, 13, null);

        private final boolean isSensitive;
        private final Integer openIndex;
        private final Integer adminIndex;
        private final String rbKey;

        ColumnIndex(boolean isSensitive, Integer openIndex, Integer adminIndex, String rbKey) {
            this.isSensitive = isSensitive;
            this.openIndex = openIndex;
            this.adminIndex = adminIndex;
            this.rbKey = rbKey;
        }

        public static List<ColumnIndex> forUser(boolean isAdmin) {
            if (!isAdmin) {
                return Arrays.stream(ColumnIndex.values()).filter(columnIndex -> !columnIndex.isSensitive).toList();
            }
            return Arrays.stream(ColumnIndex.values()).toList();
        }

        public String getColumnHeading(ResourceBundle rb) {
            return rbKey != null && !rbKey.isBlank() ? rb.getString(rbKey) : "";
        }

        public Integer getIndex(boolean admin) {
            if (admin) {
                return adminIndex;
            }
            return openIndex;
        }
    }

    public static byte[] generateExcel(VipsLogicUser user, ULocale locale, List<ObservationListItem> observations) throws IOException {
        ResourceBundle rb = ResourceBundle.getBundle("no.nibio.vips.logic.i18n.vipslogictexts", locale.toLocale());
        boolean isAdmin = user != null && (user.isSuperUser() || user.isOrganizationAdmin());
        LOGGER.info("Create Excel file containing {} observations for {} user", observations.size(),  isAdmin ? "admin": "regular");
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Create main mainSheet for all observations, with header row
            Sheet mainSheet = workbook.createSheet(rb.getString("allObservations"));
            createHeaderRow(isAdmin, mainSheet, rb);

            int mainSheetRowIndex = 1;
            // Add one row for each observation in list of all observations
            for (ObservationListItem item : observations) {
                createItemRow(isAdmin, workbook, mainSheet, mainSheetRowIndex++, item, rb);
            }
            autoSizeColumns(mainSheet, 0, ColumnIndex.INDEX_DATA.getIndex(isAdmin) - 1);

            // Prepare list of observations for each type of pest
            Map<Integer, List<ObservationListItem>> pestObservations = getObservationsForEachPest(observations);

            // Create sheets for each individual pest type
            for (Integer pestId : pestObservations.keySet()) {
                List<ObservationListItem> observationsForPest = pestObservations.get(pestId);
                ObservationListItem firstObservationForPest = observationsForPest.get(0);
                String pestName = firstObservationForPest.getOrganismName();
                Sheet pestSheet = workbook.createSheet(sanitizeSheetName(pestId, pestName));
                Row headerRow = createHeaderRow(isAdmin, pestSheet, rb);

                // Add column titles for observation data
                Map<String, String> dataColumnTitles = getObservationDataColumnTitles(firstObservationForPest.getObservationDataSchema());
                int pestSheetColIndex = ColumnIndex.INDEX_DATA.getIndex(isAdmin);
                for (String key : dataColumnTitles.keySet()) {
                    headerRow.createCell(pestSheetColIndex++).setCellValue(dataColumnTitles.get(key));
                }

                int pestSheetRowIndex = 1;
                for (ObservationListItem item : observationsForPest) {
                    Row row = createItemRow(isAdmin, workbook, pestSheet, pestSheetRowIndex++, item, rb);

                    if (item.getObservationData() != null) {
                        Map<String, Object> observationDataMap = objectMapper.readValue(item.getObservationData(), HashMap.class);
                        if (observationDataMap != null) {
                            pestSheetColIndex = ColumnIndex.INDEX_DATA.getIndex(isAdmin);
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
                autoSizeColumns(pestSheet, ColumnIndex.ID.getIndex(isAdmin), ColumnIndex.INDEX_DATA.getIndex(isAdmin) + dataColumnTitles.size());
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Create sheet name without invalid characters and within size limits
     *
     * @param pestId   The id of the pest
     * @param pestName The name of the pest
     * @return a sanitized string to be used as sheet name
     */
    private static String sanitizeSheetName(Integer pestId, String pestName) {
        if (pestName == null || pestName.isBlank()) {
            return "Id=" + pestId;
        }
        return pestName.replaceAll("[\\[\\]\\*/:?\\\\]", "_").substring(0, Math.min(pestName.length(), 31));
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
     * Create first row of given sheet, with column titles dependent on user privileges
     *
     * @param isAdmin Whether the user is a logged in admin
     * @param sheet   The sheet to which a row will be added
     * @param rb      A resource bundle enabling localized messages
     * @return the newly created header row
     */
    public static Row createHeaderRow(boolean isAdmin, Sheet sheet, ResourceBundle rb) {
        Row headerRow = sheet.createRow(0);
        for (ColumnIndex columnIndex : ColumnIndex.forUser(isAdmin)) {
            headerRow.createCell(columnIndex.getIndex(isAdmin)).setCellValue(columnIndex.getColumnHeading(rb));
        }
        return headerRow;
    }

    /**
     * Create row with given index, for given observation list item
     *
     * @param isAdmin  Whether the user is a logged in admin
     * @param workbook The current workbook
     * @param sheet    The sheet to which a row will be added
     * @param rowIndex The index of the row
     * @param item     The item of which to add data
     * @return the newly created row
     */
    private static Row createItemRow(boolean isAdmin, XSSFWorkbook workbook, Sheet sheet, int rowIndex, ObservationListItem item, ResourceBundle rb) throws JsonProcessingException {
        LocalDate localDateOfObservation = item.getTimeOfObservation().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Row row = sheet.createRow(rowIndex);
        addObservationLinkToFirstCol(isAdmin, workbook, row, item.getObservationId());
        row.createCell(ColumnIndex.DATE.getIndex(isAdmin)).setCellValue(localDateOfObservation.format(DATE_FORMATTER));
        row.createCell(ColumnIndex.POI_NAME.getIndex(isAdmin)).setCellValue(item.getLocationPointOfInterestName());

        if (isAdmin) {
            row.createCell(ColumnIndex.OBSERVER_ID.getIndex(isAdmin)).setCellValue(item.getObserverId());
            row.createCell(ColumnIndex.OBSERVER_NAME.getIndex(isAdmin)).setCellValue(item.getObserverName());
        }
        if(item.getObservationTimeSeriesId() != null) {
            row.createCell(ColumnIndex.OBSERVATION_TIME_SERIES_ID.getIndex(isAdmin)).setCellValue(item.getObservationTimeSeriesId());
            row.createCell(ColumnIndex.OBSERVATION_TIME_SERIES_LABEL.getIndex(isAdmin)).setCellValue(item.getObservationTimeSeriesLabel());
        }
        row.createCell(ColumnIndex.ORGANISM.getIndex(isAdmin)).setCellValue(item.getOrganismName());
        row.createCell(ColumnIndex.CROP_ORGANISM.getIndex(isAdmin)).setCellValue(item.getCropOrganismName());
        row.createCell(ColumnIndex.HEADING.getIndex(isAdmin)).setCellValue(item.getObservationHeading());
        row.createCell(ColumnIndex.DESCRIPTION.getIndex(isAdmin)).setCellValue(item.getObservationText());

        row.createCell(ColumnIndex.BROADCAST.getIndex(isAdmin)).setCellValue(getBooleanStringValue(rb, item.getBroadcastMessage()));
        row.createCell(ColumnIndex.POSITIVE.getIndex(isAdmin)).setCellValue(getBooleanStringValue(rb, item.getIsPositive()));
        return row;
    }

    private static String getBooleanStringValue(ResourceBundle rb, Boolean value) {
        if (value == null) {
            return null;
        } else if (value) {
            return rb.getString("yes");
        }
        return rb.getString("no");
    }

    private static void addObservationLinkToFirstCol(boolean isAdmin, Workbook workbook, Row row, Integer observationId) {
        Cell cell = row.createCell(ColumnIndex.ID.getIndex(isAdmin));
        cell.setCellValue(observationId);

        CreationHelper creationHelper = workbook.getCreationHelper();
        Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.URL);
        //hyperlink.setAddress("https://www.vips-landbruk.no/observations/" + observationId);
        // TODO Dette må fikses før deploy til prod
        hyperlink.setAddress("https://testvips.nibio.no/observations/" + observationId);
        cell.setHyperlink(hyperlink);

        CellStyle hlinkStyle = workbook.createCellStyle();
        Font hlinkFont = workbook.createFont();
        hlinkFont.setUnderline(Font.U_SINGLE);
        hlinkFont.setColor(IndexedColors.BLUE.getIndex());
        hlinkStyle.setFont(hlinkFont);
        cell.setCellStyle(hlinkStyle);
    }

}
