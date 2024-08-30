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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class ExcelFileGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelFileGenerator.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // TODO Dette må fikses før deploy til prod
    private static final String VIPSWEB = "https://testvips.nibio.no";
    private static final String VIPSLOGIC = "https://logic.testvips.nibio.no";

    private enum ColumnIndex {
        ID(false, 0, 0, "observationId"),
        DATE(false, 1, 1, "timeOfObservation"),
        POI_NAME(false, 2, 2, "location"),
        OBSERVER_NAME(true, null, 3, "observer"),
        OBSERVATION_TIME_SERIES_LABEL(false, 3, 4, "observationTimeSeriesLabel"),
        ORGANISM(false, 4, 5, "organism"),
        CROP_ORGANISM(false, 5, 6, "cropOrganismId"),
        HEADING(false, 6, 7, "observationHeading"),
        DESCRIPTION(false, 7, 8, "observationText"),
        BROADCAST(false, 8, 9, "isBroadcast"),
        POSITIVE(false, 9, 10, "isPositiveRegistration"),
        INDEX_DATA(false, 10, 11, null);

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

    public static byte[] generateExcel(VipsLogicUser user, ULocale locale, LocalDateTime now, String fromStr, String toStr, List<ObservationListItem> observations) throws IOException {
        ResourceBundle rb = ResourceBundle.getBundle("no.nibio.vips.logic.i18n.vipslogictexts", locale.toLocale());
        boolean isAdmin = user != null && (user.isSuperUser() || user.isOrganizationAdmin());
        LOGGER.info("Create Excel file containing {} observations for {} user", observations.size(), isAdmin ? "admin" : "regular");
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Font font = workbook.createFont();
            font.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(font);

            // Create main sheet for all observations, with header row
            Sheet mainSheet = workbook.createSheet(rb.getString("allObservations"));
            createHeaderRow(isAdmin, mainSheet, headerStyle, rb);

            int mainSheetRowIndex = 1;
            // Add one row for each observation in list of all observations
            for (ObservationListItem item : observations) {
                createItemRow(isAdmin, mainSheet, mainSheetRowIndex++, item, rb);
            }
            autoSizeColumns(mainSheet, 0, ColumnIndex.INDEX_DATA.getIndex(isAdmin) - 1);

            // Create meta sheet for information about the download
            Sheet metaSheet = workbook.createSheet(rb.getString("downloadInfo"));
            addMetaInfo(metaSheet, user, now, fromStr, toStr, observations, headerStyle, rb);

            // Prepare list of observations for each type of pest
            Map<Integer, List<ObservationListItem>> pestObservations = getObservationsForEachPest(observations);

            // Create sheets for each individual pest type
            for (Integer pestId : pestObservations.keySet()) {
                List<ObservationListItem> observationsForPest = pestObservations.get(pestId);
                ObservationListItem firstObservationForPest = observationsForPest.get(0);
                String pestName = firstObservationForPest.getOrganismName();
                Sheet pestSheet = workbook.createSheet(sanitizeSheetName(pestId, pestName));
                Row headerRow = createHeaderRow(isAdmin, pestSheet, headerStyle, rb);

                // Add column titles for observation data
                Map<String, String> dataColumnTitles = getObservationDataColumnTitles(firstObservationForPest.getObservationDataSchema());
                int pestSheetColIndex = ColumnIndex.INDEX_DATA.getIndex(isAdmin);
                for (String key : dataColumnTitles.keySet()) {
                    Cell cell = headerRow.createCell(pestSheetColIndex++);
                    cell.setCellStyle(headerStyle);
                    cell.setCellValue(dataColumnTitles.get(key));
                }

                int pestSheetRowIndex = 1;
                for (ObservationListItem item : observationsForPest) {
                    Row row = createItemRow(isAdmin, pestSheet, pestSheetRowIndex++, item, rb);

                    if (item.getObservationData() != null) {
                        Map<String, Object> observationDataMap = objectMapper.readValue(item.getObservationData(), HashMap.class);
                        if (observationDataMap != null) {
                            pestSheetColIndex = ColumnIndex.INDEX_DATA.getIndex(isAdmin);
                            for (String key : dataColumnTitles.keySet()) {
                                pestSheetColIndex = addValueToCell(row, pestSheetColIndex, observationDataMap.get(key));
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
     * Add meta information to given sheet
     *
     * @param metaSheet    The sheet in which to add content
     * @param user         The current user
     * @param now          The current timestamp
     * @param fromStr      The start of the period for which we have observations
     * @param toStr        The end of the period for which we have observations
     * @param observations The list of observations
     * @param headerStyle  How to style the title cells
     * @param rb           Resource bundle with translations
     */
    private static void addMetaInfo(Sheet metaSheet, VipsLogicUser user, LocalDateTime now, String fromStr, String toStr, List<ObservationListItem> observations, CellStyle headerStyle, ResourceBundle rb) {
        Row userRow = metaSheet.createRow(0);
        Cell downloadedByCell = userRow.createCell(0);
        downloadedByCell.setCellStyle(headerStyle);
        downloadedByCell.setCellValue(rb.getString("downloadedBy"));
        userRow.createCell(1).setCellValue(user != null ? user.getFullName() : rb.getString("unregisteredUser"));
        Row timeRow = metaSheet.createRow(1);
        Cell downloadedTimeCell = timeRow.createCell(0);
        downloadedTimeCell.setCellStyle(headerStyle);
        downloadedTimeCell.setCellValue(rb.getString("downloadedTime"));
        timeRow.createCell(1).setCellValue(DATE_TIME_FORMATTER.format(now));
        Row fromRow = metaSheet.createRow(2);
        Cell dateFromCell = fromRow.createCell(0);
        dateFromCell.setCellStyle(headerStyle);
        dateFromCell.setCellValue(rb.getString("dateStart"));
        fromRow.createCell(1).setCellValue(fromStr);
        Row toRow = metaSheet.createRow(3);
        Cell dateToCell = toRow.createCell(0);
        dateToCell.setCellStyle(headerStyle);
        dateToCell.setCellValue(rb.getString("dateEnd"));
        toRow.createCell(1).setCellValue(toStr);
        Row countRow = metaSheet.createRow(4);
        Cell countCell = countRow.createCell(0);
        countCell.setCellStyle(headerStyle);
        countCell.setCellValue(rb.getString("observationCount"));
        countRow.createCell(1).setCellValue(observations.size());
        autoSizeColumns(metaSheet, 0, 1);
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
     * @param isAdmin     Whether the user is a logged in admin
     * @param sheet       The sheet to which a row will be added
     * @param headerStyle The style to be applied to each cell in the header row
     * @param rb          A resource bundle enabling localized messages
     * @return the newly created header row
     */
    public static Row createHeaderRow(boolean isAdmin, Sheet sheet, CellStyle headerStyle, ResourceBundle rb) {
        Row headerRow = sheet.createRow(0);
        for (ColumnIndex columnIndex : ColumnIndex.forUser(isAdmin)) {
            Cell cell = headerRow.createCell(columnIndex.getIndex(isAdmin));
            cell.setCellStyle(headerStyle);
            cell.setCellValue(columnIndex.getColumnHeading(rb));
        }
        return headerRow;
    }

    /**
     * Create row with given index, for given observation list item
     *
     * @param isAdmin  Whether the user is a logged in admin
     * @param sheet    The sheet to which a row will be added
     * @param rowIndex The index of the row
     * @param item     The item of which to add data
     * @return the newly created row
     */
    private static Row createItemRow(boolean isAdmin, Sheet sheet, int rowIndex, ObservationListItem item, ResourceBundle rb) throws JsonProcessingException {
        LocalDate localDateOfObservation = item.getTimeOfObservation().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Row row = sheet.createRow(rowIndex);
        addObservationLink(row, ColumnIndex.ID.getIndex(isAdmin), item.getObservationId());
        addValueToCell(row, ColumnIndex.DATE.getIndex(isAdmin), localDateOfObservation.format(DATE_FORMATTER));

        if (item.getLocationPointOfInterestId() != null) {
            Integer poiNameIndex = ColumnIndex.POI_NAME.getIndex(isAdmin);
            if(isAdmin) {
                addPoiLink(row, poiNameIndex, item.getLocationPointOfInterestId(), item.getLocationPointOfInterestName());
            } else {
                addValueToCell(row, poiNameIndex, item.getLocationPointOfInterestName());
            }
        }

        if (isAdmin) {
            addUserLink(row, ColumnIndex.OBSERVER_NAME.getIndex(isAdmin), item.getObserverId(), item.getObserverName());
        }
        if (item.getObservationTimeSeriesId() != null) {
            addTimeSeriesLink(row, ColumnIndex.OBSERVATION_TIME_SERIES_LABEL.getIndex(isAdmin), item.getObservationTimeSeriesId(), item.getObservationTimeSeriesLabel());
        }
        addValueToCell(row, ColumnIndex.ORGANISM.getIndex(isAdmin), item.getOrganismName());
        addValueToCell(row, ColumnIndex.CROP_ORGANISM.getIndex(isAdmin), item.getCropOrganismName());
        addValueToCell(row, ColumnIndex.HEADING.getIndex(isAdmin), item.getObservationHeading());
        addValueToCell(row, ColumnIndex.DESCRIPTION.getIndex(isAdmin), item.getObservationText());

        addValueToCell(row, ColumnIndex.BROADCAST.getIndex(isAdmin), getBooleanStringValue(rb, item.getBroadcastMessage()));
        addValueToCell(row, ColumnIndex.POSITIVE.getIndex(isAdmin), getBooleanStringValue(rb, item.getIsPositive()));
        return row;
    }

    /**
     * Add value to cell
     *
     * @param row      The row to which the value should be added
     * @param colIndex The index of the column to add the value to
     * @param value    The value
     * @return The next index value
     */
    private static Integer addValueToCell(Row row, Integer colIndex, Object value) {
        Integer index = colIndex;
        if (value == null) {
            row.createCell(index++).setCellValue("");
        } else if (value instanceof Number) {
            row.createCell(index++).setCellValue(((Number) value).intValue());
        } else {
            try {
                int intValue = Integer.parseInt(value.toString());
                row.createCell(index++).setCellValue(intValue);
            } catch (NumberFormatException e) {
                row.createCell(index++).setCellValue(value.toString());
            }
        }
        return index;
    }

    /**
     * Get a localized String representing either true or false
     *
     * @param rb    The resource bundle to get the localized string from
     * @param value Either true or false
     * @return A localized String
     */
    private static String getBooleanStringValue(ResourceBundle rb, Boolean value) {
        if (value == null) {
            return null;
        } else if (value) {
            return rb.getString("yes");
        }
        return rb.getString("no");
    }

    /**
     * Add link to observation details in column containing the observation Id
     *
     * @param row           A reference to the current row
     * @param colIndex      The index of the column in which the link should be added
     * @param observationId The id of the observation
     */
    private static void addObservationLink(Row row, Integer colIndex, Integer observationId) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(observationId);

        Workbook workbook = row.getSheet().getWorkbook();
        cell.setHyperlink(createHyperlink(workbook, VIPSWEB + "/observations/" + observationId));
        cell.setCellStyle(hyperlinkCellStyle(workbook));
    }

    /**
     * Add link to timeseries details in column with given index
     *
     * @param row             A reference to the current row
     * @param colIndex        The index of the column in which the link should be added
     * @param timeSeriesId    The id of the timeseries
     * @param timeSeriesLabel The text which should be displayed in the cell
     */
    private static void addTimeSeriesLink(Row row, Integer colIndex, Integer timeSeriesId, String timeSeriesLabel) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(timeSeriesLabel);

        Workbook workbook = row.getSheet().getWorkbook();
        cell.setHyperlink(createHyperlink(workbook, VIPSWEB + "/observations/timeseries/" + timeSeriesId));
        cell.setCellStyle(hyperlinkCellStyle(workbook));
    }

    /**
     * Add link to poi details in column with given index
     *
     * @param row      A reference to the current row
     * @param colIndex The index of the column in which the link should be added
     * @param poiId    The id of the poi
     * @param poiName  The text which should be displayed in the cell
     */
    private static void addPoiLink(Row row, Integer colIndex, Integer poiId, String poiName) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(poiName);

        Workbook workbook = row.getSheet().getWorkbook();
        cell.setHyperlink(createHyperlink(workbook, VIPSLOGIC + "/weatherStation?pointOfInterestId=" + poiId));
        cell.setCellStyle(hyperlinkCellStyle(workbook));
    }

    /**
     * Add link to user details in column with given index
     *
     * @param row      A reference to the current row
     * @param colIndex The index of the column in which the link should be added
     * @param userId   The id of the user
     * @param userName The text which should be displayed in the cell
     */
    private static void addUserLink(Row row, Integer colIndex, Integer userId, String userName) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(userName);

        Workbook workbook = row.getSheet().getWorkbook();
        cell.setHyperlink(createHyperlink(workbook, VIPSLOGIC + "/user?action=viewUser&userId=" + userId));
        cell.setCellStyle(hyperlinkCellStyle(workbook));
    }

    private static Hyperlink createHyperlink(Workbook workbook, String url) {
        CreationHelper creationHelper = workbook.getCreationHelper();
        Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.URL);
        hyperlink.setAddress(url);
        return hyperlink;
    }

    private static CellStyle hyperlinkCellStyle(Workbook workbook) {
        CellStyle hyperlinkStyle = workbook.createCellStyle();
        Font hlinkFont = workbook.createFont();
        hlinkFont.setUnderline(Font.U_SINGLE);
        hlinkFont.setColor(IndexedColors.BLUE.getIndex());
        hyperlinkStyle.setFont(hlinkFont);
        return hyperlinkStyle;
    }

}
