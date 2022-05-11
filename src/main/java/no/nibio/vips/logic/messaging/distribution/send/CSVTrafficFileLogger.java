/*
 * Copyright (c) 2022 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the NIBIO Open Source License as published by 
 * NIBIO, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * NIBIO Open Source License for more details.
 * 
 * You should have received a copy of the NIBIO Open Source License
 * along with VIPSLogic.  If not, see <http://www.nibio.no/licenses/>.
 * 
 */

package no.nibio.vips.logic.messaging.distribution.send;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import no.nibio.vips.logic.messaging.distribution.entity.MsgReceiver;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 * Writes sent/received logs to chosen unix directory (e.g. /var/log/trafficLog)
 * Property config is System.getProperty("no.nibio.vips.logic.messaging.dist.TRAFFIC_LOG_DIRECTORY");
 * @author Lars Aksel Opsahl <lars.opsahl@nibio.no>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class CSVTrafficFileLogger {

    // Delimiter used in CSV file
    private static final String NEW_LINE_SEPARATOR = "\n";

    // received
    public static void writeCsvFilerReceived(String fileName, List<MsgReceiver> msgList) {

        // CSV file header
        // msgDeliveryAddress;
        // recipientId;
        // msgDeliverySent
        Object[] FILE_HEADER_RECEIVED = {"recipientId", "msgType", "msgDeliveryAddress"};

        FileWriter fileWriter = null;

        CSVPrinter csvFilePrinter = null;

        // Create the CSVFormat object with "\n" as a record delimiter
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

        try {

            // initialize FileWriter object
            fileWriter = new FileWriter(fileName);

            // initialize CSVPrinter object
            csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

            // Create CSV file header
            csvFilePrinter.printRecord(FILE_HEADER_RECEIVED);

            // Write a new student object list to the CSV file
            for (MsgReceiver mr : msgList) {
                List msgReceiverDataRecord = new ArrayList();
                msgReceiverDataRecord.add(mr.recipientId);
                msgReceiverDataRecord.add(mr.type);
                msgReceiverDataRecord.add(mr.msgDeliveryAddress);
                csvFilePrinter.printRecord(msgReceiverDataRecord);
            }

        } catch (Exception e) {
            System.out.println("Failed to write result to file " + fileName + ": " + e);
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
                csvFilePrinter.close();
            } catch (IOException e) {
                System.out.println("Failed to write result to file " + fileName + ": " + e);
            }
        }

    }

    public static void writeCsvFilerReceived(String fileNameSentResult, ArrayList<SingleMsgSendResult> msgSentResult) {
        // CSV file header
        // msgDeliveryAddress;
        // recipientId;
        // msgDeliverySent

        Object[] FILE_HEADER_RECEIVED = {"recipientId", "msgType", "msgDeliveryAddress", "time", "result"};

        FileWriter fileWriter = null;

        CSVPrinter csvFilePrinter = null;

        // Create the CSVFormat object with "\n" as a record delimiter
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

        try {

            // initialize FileWriter object
            fileWriter = new FileWriter(fileNameSentResult);

            // initialize CSVPrinter object
            csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

            // Create CSV file header
            csvFilePrinter.printRecord(FILE_HEADER_RECEIVED);

            // Write a new student object list to the CSV file
            for (SingleMsgSendResult mr : msgSentResult) {
                List msgReceiverDataRecord = new ArrayList();
                msgReceiverDataRecord.add(mr.msgReceivier.recipientId);
                msgReceiverDataRecord.add(mr.msgReceivier.type);
                msgReceiverDataRecord.add(mr.msgReceivier.msgDeliveryAddress);
                msgReceiverDataRecord.add(mr.delievery);
                msgReceiverDataRecord.add(mr.res);

                csvFilePrinter.printRecord(msgReceiverDataRecord);
            }

        } catch (Exception e) {
            System.out.println("Failed to write result to file " + fileNameSentResult + ": " + e);
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
                csvFilePrinter.close();
            } catch (IOException e) {
                System.out.println("Failed to write result to file " + fileNameSentResult + ": " + e);
            }
        }

    }
}
