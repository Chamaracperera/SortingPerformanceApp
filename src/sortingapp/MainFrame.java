
package sortingapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Collections;
import javax.swing.SortOrder;

public class MainFrame extends JFrame {

    private static final Color NAVY = new Color(25, 43, 75);
    private static final Color BLUE = new Color(48, 105, 190);
    private static final Color BG = new Color(244, 247, 252);
    private static final Color MUTED = new Color(105, 117, 138);
    private static final Color GREEN = new Color(27, 125, 91);
    private static final Color BORDER = new Color(228, 233, 241);

    private final CSVReader csvReader = new CSVReader();
    private final PerformanceEvaluator evaluator = new PerformanceEvaluator();

    private Dataset dataset;
    private File selectedFile;

    private final JButton uploadButton = new JButton("Choose CSV File");
    private final JButton runButton = new JButton("Run Sorting");
    private final JComboBox<String> columnCombo = new JComboBox<>();

    private final JLabel fileNameLabel = new JLabel("No CSV file selected");
    private final JLabel fileInfoLabel = new JLabel("Upload a CSV file to get started");
    private final JLabel numericInfoLabel = new JLabel("Numeric columns: 0");
    private final JLabel previewInfoLabel = new JLabel("Upload a CSV to preview data");
    private final JLabel statusLabel = new JLabel("Ready — upload a CSV file to begin.");
    private final JLabel bestLabel = new JLabel("No evaluation yet");

    private final JTextArea sortedPreview = new JTextArea();

    private final DefaultTableModel previewModel =
            new DefaultTableModel(new Object[]{}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable previewTable = new JTable(previewModel);

    private final DefaultTableModel resultsModel =
            new DefaultTableModel(
                    new Object[]{"Sorting Algorithm", "Execution Time (ns)"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 1 ? Long.class : String.class;
                }
            };

    private final JTable resultsTable = new JTable(resultsModel);

    public MainFrame() {
        setTitle("Sorting Algorithm Performance Evaluator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680));
        setSize(1400, 850);
        setLocationRelativeTo(null);

        buildInterface();

        uploadButton.addActionListener(e -> uploadCSV());
        runButton.addActionListener(e -> runSorting());

        runButton.setEnabled(false);
    }

    private void buildInterface() {

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("Sorting Algorithm Performance Evaluator");
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Upload your dataset, preview the data and compare algorithms");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(210, 220, 238));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);

        header.add(heading, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        // FOOTER
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(9, 20, 9, 20));

        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(MUTED);

        footer.add(statusLabel, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        // MAIN CONTENT
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(14, 18, 12, 18));

        // UPLOAD CARD
        JPanel uploadCard = card();
        uploadCard.setLayout(new BorderLayout(12, 8));
        uploadCard.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel fileDetails = new JPanel();
        fileDetails.setOpaque(false);
        fileDetails.setLayout(new BoxLayout(fileDetails, BoxLayout.Y_AXIS));

        JLabel fileHeading = new JLabel("YOUR DATASET");
        fileHeading.setFont(new Font("SansSerif", Font.BOLD, 10));
        fileHeading.setForeground(MUTED);

        fileNameLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        fileNameLabel.setForeground(NAVY);

        fileInfoLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        fileInfoLabel.setForeground(MUTED);

        numericInfoLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        numericInfoLabel.setForeground(GREEN);

        fileDetails.add(fileHeading);
        fileDetails.add(Box.createVerticalStrut(4));
        fileDetails.add(fileNameLabel);
        fileDetails.add(Box.createVerticalStrut(3));
        fileDetails.add(fileInfoLabel);
        fileDetails.add(Box.createVerticalStrut(4));
        fileDetails.add(numericInfoLabel);

        styleButton(uploadButton, new Color(231, 238, 250), NAVY);
        uploadButton.setPreferredSize(new Dimension(160, 40));

        uploadCard.add(fileDetails, BorderLayout.CENTER);
        uploadCard.add(uploadButton, BorderLayout.EAST);

        // COLUMN SELECTION
        JPanel optionsCard = card();
        optionsCard.setLayout(new BorderLayout(10, 8));
        optionsCard.setBorder(new EmptyBorder(10, 16, 10, 16));

        JPanel columnDetails = new JPanel();
        columnDetails.setOpaque(false);
        columnDetails.setLayout(new BoxLayout(columnDetails, BoxLayout.Y_AXIS));

        JLabel columnTitle = new JLabel("Select a numeric column");
        columnTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        columnTitle.setForeground(NAVY);

        JLabel columnHint = new JLabel(
                "Choose the numeric values you want to sort");
        columnHint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        columnHint.setForeground(MUTED);

        columnDetails.add(columnTitle);
        columnDetails.add(Box.createVerticalStrut(3));
        columnDetails.add(columnHint);

        columnCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        columnCombo.setBackground(Color.WHITE);
        columnCombo.setPreferredSize(new Dimension(220, 38));

        styleButton(runButton, BLUE, Color.WHITE);
        runButton.setPreferredSize(new Dimension(150, 40));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(columnCombo);
        actions.add(runButton);

        optionsCard.add(columnDetails, BorderLayout.CENTER);
        optionsCard.add(actions, BorderLayout.EAST);

        JPanel topSection = new JPanel();
        topSection.setOpaque(false);
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.add(uploadCard);
        topSection.add(Box.createVerticalStrut(9));
        topSection.add(optionsCard);

        // CSV PREVIEW - RIGHT SIDE
        JPanel previewCard = card();
        previewCard.setLayout(new BorderLayout(6, 8));
        previewCard.setBorder(new EmptyBorder(12, 10, 10, 10));

        JLabel previewTitle = new JLabel("CSV Data Preview");
        previewTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        previewTitle.setForeground(NAVY);

        previewInfoLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        previewInfoLabel.setForeground(MUTED);

        JPanel previewHeading = new JPanel();
        previewHeading.setOpaque(false);
        previewHeading.setLayout(new BoxLayout(previewHeading, BoxLayout.Y_AXIS));
        previewHeading.add(previewTitle);
        previewHeading.add(Box.createVerticalStrut(4));
        previewHeading.add(previewInfoLabel);

        styleTable(previewTable);
        previewTable.setFont(new Font("SansSerif", Font.PLAIN, 11));
        previewTable.setRowHeight(25);
        previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        previewTable.setAutoCreateRowSorter(true);

        JScrollPane previewScroll = new JScrollPane(previewTable);
        previewScroll.getViewport().setBackground(Color.WHITE);
        previewScroll.getVerticalScrollBar().setUnitIncrement(16);
        previewScroll.getHorizontalScrollBar().setUnitIncrement(16);

        previewCard.add(previewHeading, BorderLayout.NORTH);
        previewCard.add(previewScroll, BorderLayout.CENTER);

        // PERFORMANCE RESULTS - EXPANDING MAIN AREA
        JPanel resultsCard = card();
        resultsCard.setLayout(new BorderLayout(8, 10));
        resultsCard.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel resultsTitle = new JLabel("Performance Results");
        resultsTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        resultsTitle.setForeground(NAVY);

        JLabel resultsSubtitle = new JLabel(
                "Compare execution times for each sorting algorithm");
        resultsSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        resultsSubtitle.setForeground(MUTED);

        JPanel resultsHeading = new JPanel();
        resultsHeading.setOpaque(false);
        resultsHeading.setLayout(new BoxLayout(resultsHeading, BoxLayout.Y_AXIS));
        resultsHeading.add(resultsTitle);
        resultsHeading.add(Box.createVerticalStrut(5));
        resultsHeading.add(resultsSubtitle);

        styleTable(resultsTable);
        resultsTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        resultsTable.setRowHeight(34);
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        resultsTable.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 13));
        resultsTable.getTableHeader().setPreferredSize(new Dimension(0, 38));

        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(260);
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(260);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        rightRenderer.setBorder(new EmptyBorder(0, 0, 0, 14));
        resultsTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);

        TableRowSorter<DefaultTableModel> resultSorter =
                new TableRowSorter<>(resultsModel);
        resultsTable.setRowSorter(resultSorter);

        JScrollPane resultsScroll = new JScrollPane(resultsTable);
        resultsScroll.setBorder(BorderFactory.createEmptyBorder());
        resultsScroll.getViewport().setBackground(Color.WHITE);
        resultsScroll.getVerticalScrollBar().setUnitIncrement(18);

        resultsCard.add(resultsHeading, BorderLayout.NORTH);
        resultsCard.add(resultsScroll, BorderLayout.CENTER);

        // SUMMARY: COMPACT FOOTER INSIDE RESULTS AREA
        JPanel bestCard = card();
        bestCard.setLayout(new BorderLayout(8, 0));
        bestCard.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel bestHeading = new JLabel("FASTEST ALGORITHM");
        bestHeading.setFont(new Font("SansSerif", Font.BOLD, 10));
        bestHeading.setForeground(GREEN);

        bestLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        bestLabel.setForeground(NAVY);

        JPanel bestContent = new JPanel();
        bestContent.setOpaque(false);
        bestContent.setLayout(new BoxLayout(bestContent, BoxLayout.Y_AXIS));
        bestContent.add(bestHeading);
        bestContent.add(Box.createVerticalStrut(5));
        bestContent.add(bestLabel);

        bestCard.add(bestContent, BorderLayout.CENTER);
        bestCard.setPreferredSize(new Dimension(300, 65));

        JPanel sortedCard = card();
        sortedCard.setLayout(new BorderLayout(6, 6));
        sortedCard.setBorder(new EmptyBorder(9, 12, 9, 12));

        JLabel sortedTitle = new JLabel("Sorted Values Preview");
        sortedTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        sortedTitle.setForeground(NAVY);

        sortedPreview.setEditable(false);
        sortedPreview.setLineWrap(true);
        sortedPreview.setWrapStyleWord(true);
        sortedPreview.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        sortedPreview.setBackground(new Color(248, 250, 253));
        sortedPreview.setBorder(new EmptyBorder(6, 8, 6, 8));

        JScrollPane sortedScroll = new JScrollPane(sortedPreview);
        sortedScroll.getVerticalScrollBar().setUnitIncrement(12);

        sortedCard.add(sortedTitle, BorderLayout.NORTH);
        sortedCard.add(sortedScroll, BorderLayout.CENTER);
        sortedCard.setPreferredSize(new Dimension(300, 105));

        JPanel bottomSection = new JPanel(new GridLayout(1, 2, 10, 0));
        bottomSection.setOpaque(false);
        bottomSection.add(bestCard);
        bottomSection.add(sortedCard);

        // MAIN AREA: RESULTS TAKE ALL REMAINING HEIGHT
        JPanel resultSection = new JPanel(new BorderLayout(0, 10));
        resultSection.setOpaque(false);
        resultSection.add(resultsCard, BorderLayout.CENTER);
        resultSection.add(bottomSection, BorderLayout.SOUTH);

        // SIDE-BY-SIDE LAYOUT
        JPanel centerSection = new JPanel(new BorderLayout(14, 0));
        centerSection.setOpaque(false);

        JPanel rightSection = new JPanel(new BorderLayout());
        rightSection.setPreferredSize(new Dimension(320, 0));
        rightSection.setMinimumSize(new Dimension(250, 150));
        rightSection.setOpaque(false);
        rightSection.add(previewCard, BorderLayout.CENTER);

        centerSection.add(resultSection, BorderLayout.CENTER);
        centerSection.add(rightSection, BorderLayout.EAST);

        content.add(topSection, BorderLayout.NORTH);
        content.add(centerSection, BorderLayout.CENTER);

        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(1, 1, 1, 1)));
        return panel;
    }

    private void styleButton(JButton button, Color background, Color foreground) {
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.darker()),
                new EmptyBorder(8, 12, 8, 12)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setRowHeight(29);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(226, 237, 253));
        table.setSelectionForeground(NAVY);

        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(235, 240, 248));
        table.getTableHeader().setForeground(NAVY);
        table.getTableHeader().setReorderingAllowed(false);
    }

    // CSV UPLOAD
    private void uploadCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a CSV file");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "CSV files (*.csv)", "csv"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        try {
            Dataset loaded = csvReader.readCSV(file);

            if (loaded == null || loaded.getColumnNames() == null
                    || loaded.getData() == null) {
                throw new IllegalArgumentException(
                        "The CSV file does not contain readable data.");
            }

            String[] columns = loaded.getColumnNames();
            String[][] rows = loaded.getData();

            if (columns.length == 0) {
                throw new IllegalArgumentException(
                        "No column headers were found in the CSV file.");
            }

            dataset = loaded;
            selectedFile = file;

            columnCombo.removeAllItems();
            int numericCount = 0;

            for (int column = 0; column < columns.length; column++) {
                boolean numeric = true;
                boolean hasValue = false;

                for (String[] row : rows) {
                    if (row == null || column >= row.length || row[column] == null) {
                        continue;
                    }

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
                    numericCount++;
                }
            }

            fileNameLabel.setText(file.getName());
            fileNameLabel.setToolTipText(file.getAbsolutePath());

            fileInfoLabel.setText(
                    rows.length + " rows  •  " + columns.length
                            + " columns  •  " + formatFileSize(file.length()));

            numericInfoLabel.setText("Numeric columns available: " + numericCount);

            previewModel.setRowCount(0);
            previewModel.setColumnCount(0);

            for (String column : columns) {
                previewModel.addColumn(column);
            }

            int previewLimit = Math.min(rows.length, 100);

            for (int i = 0; i < previewLimit; i++) {
                Object[] previewRow = new Object[columns.length];

                for (int j = 0; j < columns.length; j++) {
                    previewRow[j] = rows[i] != null && j < rows[i].length
                            && rows[i][j] != null ? rows[i][j] : "";
                }

                previewModel.addRow(previewRow);
            }

            previewInfoLabel.setText(
                    "Showing " + previewLimit + " of " + rows.length + " rows");

            for (int i = 0; i < previewTable.getColumnCount(); i++) {
                previewTable.getColumnModel().getColumn(i).setPreferredWidth(100);
            }

            resultsModel.setRowCount(0);
            sortedPreview.setText("");
            bestLabel.setText("Run sorting to see the fastest algorithm");

            if (numericCount == 0) {
                runButton.setEnabled(false);
                statusLabel.setText(
                        "CSV loaded, but no numeric columns were detected.");
            } else {
                runButton.setEnabled(true);
                statusLabel.setText(
                        "CSV loaded successfully. Select a column and run sorting.");
            }

        } catch (Exception ex) {
            dataset = null;
            selectedFile = null;

            columnCombo.removeAllItems();
            previewModel.setRowCount(0);
            previewModel.setColumnCount(0);
            resultsModel.setRowCount(0);

            fileNameLabel.setText("No CSV file selected");
            fileInfoLabel.setText("Unable to load the selected file");
            numericInfoLabel.setText("Numeric columns: 0");
            previewInfoLabel.setText("Preview unavailable");

            runButton.setEnabled(false);
            sortedPreview.setText("");
            bestLabel.setText("No evaluation yet");
            statusLabel.setText("CSV upload failed.");

            JOptionPane.showMessageDialog(
                    this,
                    "Could not load the CSV file:\n" + ex.getMessage(),
                    "CSV Upload Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }

        if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        }

        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    // RUN SORTING
    private void runSorting() {
        if (dataset == null || columnCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please upload a CSV and select a numeric column.",
                    "Missing Dataset",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        runButton.setEnabled(false);
        statusLabel.setText("Sorting in progress...");

        try {
            String selectedColumn = (String) columnCombo.getSelectedItem();
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
                throw new IllegalArgumentException("Selected column was not found.");
            }

            double[] values = new double[rows.length];

            for (int i = 0; i < rows.length; i++) {
                if (rows[i] == null || columnIndex >= rows[i].length
                        || rows[i][columnIndex] == null
                        || rows[i][columnIndex].trim().isEmpty()) {
                    throw new IllegalArgumentException(
                            "The selected column contains empty values. "
                                    + "Please choose a column without missing values.");
                }

                values[i] = Double.parseDouble(rows[i][columnIndex].trim());

                if (!Double.isFinite(values[i])) {
                    throw new IllegalArgumentException(
                            "The selected column contains invalid numbers.");
                }
            }

            List<PerformanceEvaluator.Result> results = evaluator.evaluate(values);

            if (results == null || results.isEmpty()) {
                throw new IllegalStateException("No sorting results were returned.");
            }

            resultsModel.setRowCount(0);

            for (PerformanceEvaluator.Result result : results) {
                resultsModel.addRow(new Object[]{
                        result.getAlgorithm(),
                        result.getTimeNs()
                });
            }

            // Numeric execution-time sorting
            if (resultsTable.getRowSorter() instanceof TableRowSorter) {
                @SuppressWarnings("unchecked")
                TableRowSorter<DefaultTableModel> sorter =
                        (TableRowSorter<DefaultTableModel>) resultsTable.getRowSorter();

                sorter.setSortKeys(Collections.singletonList(
                        new RowSorter.SortKey(1, SortOrder.ASCENDING)));
                sorter.sort();
            }

            PerformanceEvaluator.Result best = evaluator.findBest(results);

            if (best == null) {
                throw new IllegalStateException(
                        "Could not determine the fastest algorithm.");
            }

            bestLabel.setText(
                    best.getAlgorithm() + " — " + best.getTimeNs() + " ns");

            double[] sorted = best.getSortedData();
            StringBuilder preview = new StringBuilder();
            preview.append("Sorted values:\n\n");

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
            sortedPreview.setCaretPosition(0);

            statusLabel.setText(
                    "Evaluation complete • Column: " + selectedColumn
                            + " • File: " + selectedFile.getName());

        } catch (Exception ex) {
            statusLabel.setText("Sorting failed.");

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to sort selected column:\n" + ex.getMessage(),
                    "Sorting Error",
                    JOptionPane.ERROR_MESSAGE);

        } finally {
            runButton.setEnabled(
                    dataset != null && columnCombo.getSelectedItem() != null);
        }
    }
}
