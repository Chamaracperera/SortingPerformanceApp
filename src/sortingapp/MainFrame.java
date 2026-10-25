
package sortingapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class MainFrame extends JFrame {

    private final CSVReader csvReader = new CSVReader();
    private final PerformanceEvaluator evaluator =
            new PerformanceEvaluator();

    private Dataset dataset;
    private File selectedFile;

    private final JButton uploadButton =
            new JButton("Upload CSV");

    private final JButton runButton =
            new JButton("Run Sorting Algorithms");

    private final JComboBox<String> columnCombo =
            new JComboBox<>();

    private final JLabel fileLabel =
            new JLabel("No CSV file selected");

    private final JLabel statusLabel =
            new JLabel("Upload a CSV file to begin.");

    private final JLabel bestLabel =
            new JLabel("Best Algorithm: Not evaluated");

    private final JTextArea sortedPreview =
            new JTextArea(6, 45);

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new Object[]{
                        "Algorithm",
                        "Execution Time (ns)"
                    }, 0
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable resultsTable =
            new JTable(tableModel);

    public MainFrame() {

        setTitle("Sorting Algorithm Performance");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 600);
        setLocationRelativeTo(null);

        buildInterface();

        uploadButton.addActionListener(e -> uploadCSV());
        runButton.addActionListener(e -> runSorting());

        runButton.setEnabled(false);
    }

    private void buildInterface() {

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        JLabel title = new JLabel(
                "Sorting Algorithm Performance Evaluation",
                SwingConstants.CENTER
        );

        title.setFont(new Font("Arial", Font.BOLD, 20));

        mainPanel.add(title, BorderLayout.NORTH);

        JPanel topPanel = new JPanel(new GridLayout(0, 1, 5, 5));

        topPanel.add(uploadButton);
        topPanel.add(fileLabel);

        JPanel columnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        columnPanel.add(new JLabel("Select numeric column:"));
        columnPanel.add(columnCombo);
        columnPanel.add(runButton);

        topPanel.add(columnPanel);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));

        centerPanel.add(
                new JScrollPane(resultsTable),
                BorderLayout.CENTER
        );

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));

        bestLabel.setFont(new Font("Arial", Font.BOLD, 14));
        bottomPanel.add(bestLabel, BorderLayout.NORTH);

        sortedPreview.setEditable(false);
        sortedPreview.setLineWrap(true);
        sortedPreview.setWrapStyleWord(true);

        bottomPanel.add(
                new JScrollPane(sortedPreview),
                BorderLayout.CENTER
        );

        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        centerPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Give the results area enough vertical space.
        centerPanel.setPreferredSize(new Dimension(700, 390));

        setContentPane(mainPanel);
    }

    private void uploadCSV() {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a CSV file");

        chooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter(
                        "CSV files (*.csv)", "csv"
                )
        );

        int choice = chooser.showOpenDialog(this);

        if (choice != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        try {

            Dataset loaded = csvReader.readCSV(file);

            dataset = loaded;
            selectedFile = file;

            columnCombo.removeAllItems();

            String[] columns = dataset.getColumnNames();
            String[][] rows = dataset.getData();

            for (int column = 0; column < columns.length; column++) {

                boolean numeric = true;
                boolean hasValue = false;

                for (String[] row : rows) {

                    String value = row[column].trim();

                    if (value.isEmpty()) {
                        continue;
                    }

                    try {
                        double number = Double.parseDouble(value);

                        if (!Double.isFinite(number)) {
                            numeric = false;
                            break;
                        }

                        hasValue = true;

                    } catch (NumberFormatException ex) {
                        numeric = false;
                        break;
                    }
                }

                if (numeric && hasValue) {
                    columnCombo.addItem(columns[column]);
                }
            }

            fileLabel.setText("File: " + file.getName());

            tableModel.setRowCount(0);
            sortedPreview.setText("");
            bestLabel.setText("Best Algorithm: Not evaluated");

            if (columnCombo.getItemCount() == 0) {
                runButton.setEnabled(false);

                statusLabel.setText(
                        "No columns with valid numeric data were found."
                );

            } else {
                runButton.setEnabled(true);

                statusLabel.setText(
                        "CSV loaded. Select a column and run the algorithms."
                );
            }

        } catch (Exception ex) {

            dataset = null;
            selectedFile = null;

            columnCombo.removeAllItems();
            runButton.setEnabled(false);

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "CSV Upload Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void runSorting() {

        if (dataset == null || columnCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please upload a CSV and select a numeric column."
            );
            return;
        }

        try {

            String selectedColumn =
                    (String) columnCombo.getSelectedItem();

            String[] columns = dataset.getColumnNames();
            String[][] rows = dataset.getData();

            int columnIndex = -1;

            for (int i = 0; i < columns.length; i++) {
                if (columns[i].equals(selectedColumn)) {
                    columnIndex = i;
                    break;
                }
            }

            if (columnIndex == -1) {
                throw new IllegalArgumentException(
                        "Selected column was not found."
                );
            }

            double[] values = new double[rows.length];

            for (int i = 0; i < rows.length; i++) {

                String value = rows[i][columnIndex].trim();

                if (value.isEmpty()) {
                    throw new IllegalArgumentException(
                            "The selected column contains empty values."
                    );
                }

                values[i] = Double.parseDouble(value);

                if (!Double.isFinite(values[i])) {
                    throw new IllegalArgumentException(
                            "The selected column contains invalid numbers."
                    );
                }
            }

            List<PerformanceEvaluator.Result> results =
                    evaluator.evaluate(values);

            tableModel.setRowCount(0);

            for (PerformanceEvaluator.Result result : results) {

                tableModel.addRow(new Object[]{
                    result.getAlgorithm(),
                    result.getTimeNs()
                });
            }

            PerformanceEvaluator.Result best =
                    evaluator.findBest(results);

            bestLabel.setText(
                    "Best Algorithm: " + best.getAlgorithm()
                    + " (" + best.getTimeNs() + " ns)"
            );

            double[] sorted = best.getSortedData();

            StringBuilder preview = new StringBuilder();

            preview.append("Sorted values from the fastest run:\n");

            int limit = Math.min(sorted.length, 100);

            for (int i = 0; i < limit; i++) {
                if (i > 0) {
                    preview.append(", ");
                }

                preview.append(sorted[i]);
            }

            if (sorted.length > limit) {
                preview.append(" ... (showing first ")
                       .append(limit)
                       .append(" values)");
            }

            sortedPreview.setText(preview.toString());

            statusLabel.setText(
                    "Evaluation complete for " + selectedColumn
                    + " in " + selectedFile.getName()
            );

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to sort selected column: " + ex.getMessage(),
                    "Sorting Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
