package no.nibio.vips.logic.util;

import com.ibm.icu.util.ULocale;
import no.nibio.vips.logic.entity.rest.ObservationListItem;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ExcelFileGenerator {
    public static byte[] generateExcel(List<ObservationListItem> filteredObservationListItems, ULocale locale) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            ResourceBundle rb = ResourceBundle.getBundle("no.nibio.vips.logic.i18n.vipslogictexts", locale.toLocale());
            // Prøvde å bruke i18n, fikk Invalid char (/) found at index (13) in sheet name 'Observasjoner/f?rstefunn'
            Sheet sheet = workbook.createSheet("Observations");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue(rb.getString("timeOfObservation"));
            headerRow.createCell(1).setCellValue(rb.getString("organism"));
            headerRow.createCell(2).setCellValue(rb.getString("cropOrganismId"));
            headerRow.createCell(3).setCellValue(rb.getString("observationHeading"));

            int rowNum = 1;
            for (ObservationListItem item : filteredObservationListItems) {
                // TODO Better way of getting current timezone?
                LocalDate localDateOfObservation = item.getTimeOfObservation().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(localDateOfObservation.format(formatter));
                row.createCell(1).setCellValue(item.getOrganismName());
                row.createCell(2).setCellValue(item.getCropOrganismName());
                row.createCell(3).setCellValue(item.getObservationHeading());

            }

            // Auto-size columns to fit content
            for (int i = 0; i <= 3; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
