package com.gsc.gsc.utilities;

import com.gsc.gsc.model.JobCard;

import java.sql.Timestamp;
import java.util.Random;

public class UniqueCodeGenerator {


    private static final String LETTER = "J";  // Assuming the letter part is fixed
    private static final int MAX_NUMBER = 999999;


    public static String generateNewJobCardCode(String lastJobCardCode) {
        // Fetch the latest job card from the database

        // If no job card exists, start from "A01"
        if (lastJobCardCode == null || lastJobCardCode.isEmpty()) {
            return LETTER + "01";  // Initial case, starting from A01
        }

        // Extract the numeric part of the last job card code (e.g., "01" from "A01")
        String numericPart = lastJobCardCode.substring(1); // Skip the first letter
        int lastNumber = Integer.parseInt(numericPart);    // Convert to integer

        // Check if the last number has reached the maximum value (99)
        if (lastNumber >= MAX_NUMBER) {
            // If so, reset to "A000001"
            return LETTER + "000001";
        } else {
            // Increment the last number
            int newNumber = lastNumber + 1;

            // Format the new number to always have six digits (e.g., "000002", "000003")
            String formattedNewNumber = String.format("%06d", newNumber);

            // Combine the letter and the new number
            return LETTER + formattedNewNumber;
        }
    }
}
