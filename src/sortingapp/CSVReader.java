
package sortingapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {

    public Dataset readCSV(File file) throws IOException {

        if (file == null || !file.isFile()) {
            throw new IOException("Please select a valid file.");
        }

        if (!file.getName().toLowerCase().endsWith(".csv")) {
            throw new IOException("Please select a CSV file.");
        }

        List<String[]> rows = new ArrayList<>();

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }

                rows.add(parseLine(line));
            }
        }

        if (rows.size() < 2) {
            throw new IOException(
                    "CSV must contain a header and at least one data row."
            );
        }

        String[] headers = rows.get(0);

        for (int i = 0; i < headers.length; i++) {
            headers[i] = headers[i].trim();

            if (headers[i].isEmpty()) {
                headers[i] = "Column " + (i + 1);
            }
        }

        String[][] data = new String[rows.size() - 1][];

        for (int i = 1; i < rows.size(); i++) {

            String[] row = rows.get(i);

            if (row.length != headers.length) {
                throw new IOException(
                        "Row " + (i + 1)
                        + " has a different number of columns."
                );
            }

            data[i - 1] = row;
        }

        return new Dataset(headers, data);
    }

    private String[] parseLine(String line) throws IOException {

        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {

            char c = line.charAt(i);

            if (c == '"') {

                if (inQuotes && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }

            } else if (c == ',' && !inQuotes) {

                values.add(current.toString().trim());
                current.setLength(0);

            } else {
                current.append(c);
            }
        }

        if (inQuotes) {
            throw new IOException("Invalid CSV: unmatched quotation mark.");
        }

        values.add(current.toString().trim());

        return values.toArray(new String[0]);
    }
}
