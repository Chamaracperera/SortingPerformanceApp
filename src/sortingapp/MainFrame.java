
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

    private static final Color NAVY = new Color(24, 31, 61);
    private static final Color BLUE = new Color(91, 82, 230);
    private static final Color CYAN = new Color(38, 196, 190);
    private static final Color BG = new Color(246, 247, 253);
    private static final Color MUTED = new Color(104, 111, 139);
    private static final Color GREEN = new Color(24, 157, 117);
    private static final Color BORDER = new Color(229, 231, 244);
    private static final Color PURPLE = new Color(132, 83, 230);

    private final CSVReader csvReader = new CSVReader();
    private final PerformanceEvaluator evaluator = new PerformanceEvaluator();

    private Dataset dataset;
    private File selectedFile;

    private final JButton uploadButton = new RoundedButton("Import CSV");
    private final JButton runButton = new RoundedButton("Analyze Data");
    private final JComboBox<String> columnCombo = new JComboBox<>();

    private final JLabel fileNameLabel = new JLabel("No CSV file selected");
    private final JLabel fileInfoLabel = new JLabel("Upload a CSV file to get started");
    private final JLabel numericInfoLabel = new JLabel("Numeric columns: 0");
    private final JLabel previewInfoLabel = new JLabel("Upload a CSV to preview data");
    private final JLabel statusLabel = new JLabel("Ready — upload a CSV file to begin.");
    private final JLabel bestLabel = new JLabel("No evaluation yet");
    private final JLabel bestMetaLabel = new JLabel("Run an analysis to reveal the champion");
    private final JLabel winnerBadge = new JLabel("★  WAITING FOR RESULTS");
    private final CardLayout resultsCards = new CardLayout();
    private final JPanel resultsCenter = new JPanel(resultsCards);
    private final CardLayout previewCards = new CardLayout();
    private final JPanel previewCenter = new JPanel(previewCards);

    private final JTextArea sortedPreview = new JTextArea();

    private static final int PAGE_SIZE = 100;
    private int currentPreviewPage = 0;

    private final JButton prevPageButton = new JButton("‹ Prev");
    private final JButton nextPageButton = new JButton("Next ›");
    private final JLabel pageLabel = new JLabel("Page 1");

    private final DefaultTableModel previewModel = new DefaultTableModel(new Object[] {}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable previewTable = new JTable(previewModel);

    private final DefaultTableModel resultsModel = new DefaultTableModel(
            new Object[] { "Sorting Algorithm", "Execution Time (ns)" }, 0) {
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

        JPanel root = new AnimatedBackgroundPanel();
        root.setLayout(new BorderLayout(0, 0));
        root.setBackground(BG);

        // HEADER
        JPanel header = new GradientPanel(NAVY, new Color(60, 44, 131));
        header.setLayout(new BorderLayout());
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("Sorting Studio");
        title.setFont(new Font("SansSerif", Font.BOLD, 29));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Benchmark classic algorithms with clarity and confidence");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(210, 220, 238));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("ALGORITHM LAB  •  PERFORMANCE INSIGHTS");
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 10));
        eyebrow.setForeground(new Color(139, 235, 222));
        heading.add(eyebrow);
        heading.add(Box.createVerticalStrut(5));
        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(subtitle);

        header.add(heading, BorderLayout.CENTER);

        JLabel headerBadge = new JLabel("LIVE BENCHMARKING");
        headerBadge.setFont(new Font("SansSerif", Font.BOLD, 10));
        headerBadge.setForeground(Color.WHITE);
        headerBadge.setOpaque(true);
        headerBadge.setBackground(new Color(255, 255, 255, 35));
        headerBadge.setBorder(new EmptyBorder(8, 12, 8, 12));
        header.add(headerBadge, BorderLayout.EAST);
        header.setPreferredSize(new Dimension(0, 132));
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
        content.setOpaque(false);

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

        styleButton(uploadButton, new Color(236, 234, 255), NAVY);
        uploadButton.setPreferredSize(new Dimension(150, 40));

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

        JLabel previewTitle = new JLabel("Dataset Preview");
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

        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        paginationPanel.setOpaque(false);

        styleButton(prevPageButton, new Color(231, 238, 250), NAVY);
        prevPageButton.setPreferredSize(new Dimension(80, 26));
        prevPageButton.setEnabled(false);

        styleButton(nextPageButton, new Color(231, 238, 250), NAVY);
        nextPageButton.setPreferredSize(new Dimension(80, 26));
        nextPageButton.setEnabled(false);

        pageLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        pageLabel.setForeground(MUTED);

        paginationPanel.add(prevPageButton);
        paginationPanel.add(pageLabel);
        paginationPanel.add(nextPageButton);

        prevPageButton.addActionListener(e -> loadPreviewPage(currentPreviewPage - 1));
        nextPageButton.addActionListener(e -> loadPreviewPage(currentPreviewPage + 1));

        previewCard.add(previewHeading, BorderLayout.NORTH);
        previewCenter.setOpaque(false);
        previewCenter.add(previewScroll, "table");
        previewCenter.add(new EmptyStatePanel("▣", "No data to preview",
                "Upload a CSV to preview data"), "empty");
        previewCards.show(previewCenter, "empty");

        previewCard.add(previewCenter, BorderLayout.CENTER);
        previewCard.add(paginationPanel, BorderLayout.SOUTH);

        // PERFORMANCE RESULTS - EXPANDING MAIN AREA
        JPanel resultsCard = card();
        resultsCard.setLayout(new BorderLayout(8, 10));
        resultsCard.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel resultsTitle = new JLabel("Performance Results");
        resultsTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        resultsTitle.setForeground(NAVY);

        JLabel resultsSubtitle = new JLabel(
                "Lower execution time wins • sorted automatically from fastest to slowest");
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

        resultsTable.getColumnModel().getColumn(0).setCellRenderer(new AlgorithmRenderer());
        resultsTable.getColumnModel().getColumn(1).setCellRenderer(new TimeBarRenderer());

        TableRowSorter<DefaultTableModel> resultSorter = new TableRowSorter<>(resultsModel);
        resultsTable.setRowSorter(resultSorter);

        JScrollPane resultsScroll = new JScrollPane(resultsTable);
        resultsScroll.setBorder(BorderFactory.createEmptyBorder());
        resultsScroll.getViewport().setBackground(Color.WHITE);
        resultsScroll.getVerticalScrollBar().setUnitIncrement(18);

        resultsCard.add(resultsHeading, BorderLayout.NORTH);
        resultsCenter.setOpaque(false);
        resultsCenter.add(resultsScroll, "table");
        resultsCenter.add(new EmptyStatePanel("♛", "No results yet",
                "Upload a CSV and select a column to start the benchmark",
                runButton, "Run Analysis"), "empty");
        resultsCards.show(resultsCenter, "empty");
        resultsCard.add(resultsCenter, BorderLayout.CENTER);

        // SUMMARY: COMPACT FOOTER INSIDE RESULTS AREA
        JPanel bestCard = new RoundedPanel(22);
        bestCard.setLayout(new BorderLayout(14, 0));
        bestCard.setBorder(new EmptyBorder(15, 18, 15, 18));
        bestCard.setBackground(new Color(232, 252, 246));

        JLabel trophy = new JLabel("★");
        trophy.setFont(new Font("SansSerif", Font.BOLD, 30));
        trophy.setForeground(new Color(218, 157, 36));
        bestCard.add(trophy, BorderLayout.WEST);

        JPanel bestContent = new JPanel();
        bestContent.setOpaque(false);
        bestContent.setLayout(new BoxLayout(bestContent, BoxLayout.Y_AXIS));

        winnerBadge.setFont(new Font("SansSerif", Font.BOLD, 10));
        winnerBadge.setForeground(GREEN);
        bestLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        bestLabel.setForeground(NAVY);
        bestMetaLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        bestMetaLabel.setForeground(MUTED);

        bestContent.add(winnerBadge);
        bestContent.add(Box.createVerticalStrut(5));
        bestContent.add(bestLabel);
        bestContent.add(Box.createVerticalStrut(3));
        bestContent.add(bestMetaLabel);
        bestCard.add(bestContent, BorderLayout.CENTER);
        bestCard.setPreferredSize(new Dimension(300, 104));

        JPanel sortedCard = card();
        sortedCard.setLayout(new BorderLayout(6, 6));
        sortedCard.setBorder(new EmptyBorder(9, 12, 9, 12));

        JLabel sortedTitle = new JLabel("Winning Sort • Sorted Values");
        sortedTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        sortedTitle.setForeground(NAVY);

        sortedPreview.setEditable(false);
        sortedPreview.setLineWrap(true);
        sortedPreview.setWrapStyleWord(true);
        sortedPreview.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        sortedPreview.setBackground(new Color(248, 247, 255));
        sortedPreview.setBorder(new EmptyBorder(6, 8, 6, 8));

        JScrollPane sortedScroll = new JScrollPane(sortedPreview);
        sortedScroll.getVerticalScrollBar().setUnitIncrement(12);

        sortedCard.add(sortedTitle, BorderLayout.NORTH);
        sortedCard.add(sortedScroll, BorderLayout.CENTER);
        sortedCard.setBackground(new Color(248, 247, 255));
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
        JPanel panel = new RoundedPanel(18);
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(1, 1, 1, 1));
        return panel;
    }

    private void styleButton(JButton button, Color background, Color foreground) {
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(8, 12, 8, 12));
        button.putClientProperty("buttonColor", background);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setRowHeight(29);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(224, 222, 255));
        table.setSelectionForeground(NAVY);

        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 239, 253));
        table.getTableHeader().setForeground(NAVY);
        table.getTableHeader().setReorderingAllowed(false);
    }

    private static class AlgorithmRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int column) {
            Component component = super.getTableCellRendererComponent(
                    table, value, selected, focused, row, column);
            setBorder(new EmptyBorder(0, 14, 0, 8));
            setFont(new Font("SansSerif", row == 0 ? Font.BOLD : Font.PLAIN, 14));
            setForeground(selected ? NAVY : (row == 0 ? PURPLE : NAVY));
            if (!selected) {
                setBackground(row == 0 ? new Color(245, 242, 255)
                        : (row % 2 == 0 ? Color.WHITE : new Color(251, 251, 254)));
            }
            setText((row == 0 ? "★  " : "    ") + String.valueOf(value));
            return component;
        }
    }

    private static class TimeBarRenderer extends JPanel
            implements javax.swing.table.TableCellRenderer {
        private String text = "";
        private double ratio;
        private boolean winner;

        TimeBarRenderer() {
            setOpaque(false);
            setBorder(new EmptyBorder(0, 10, 0, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int column) {
            long current = value instanceof Number ? ((Number) value).longValue() : 0;
            long maximum = 1;
            for (int i = 0; i < table.getRowCount(); i++) {
                Object candidate = table.getValueAt(i, column);
                if (candidate instanceof Number) {
                    maximum = Math.max(maximum, ((Number) candidate).longValue());
                }
            }
            ratio = Math.max(0.08, Math.min(1.0, current / (double) maximum));
            text = String.format("%,d ns", current);
            winner = row == 0;
            return this;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth() - 22;
            int height = 22;
            int y = Math.max(0, (getHeight() - height) / 2);
            g.setColor(new Color(238, 239, 248));
            g.fillRoundRect(0, y, width, height, 12, 12);
            g.setColor(winner ? CYAN : new Color(166, 151, 238));
            g.fillRoundRect(0, y, (int) (width * ratio), height, 12, 12);
            g.setColor(NAVY);
            g.setFont(new Font("SansSerif", winner ? Font.BOLD : Font.PLAIN, 12));
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(text, Math.max(8, width - metrics.stringWidth(text) - 10),
                    y + (height + metrics.getAscent() - metrics.getDescent()) / 2);
            g.dispose();
        }
    }

    private static class EmptyStatePanel extends JPanel {
        EmptyStatePanel(String icon, String title, String subtitle) {
            this(icon, title, subtitle, null, null);
        }

        EmptyStatePanel(String icon, String title, String subtitle,
                JButton actionSource, String actionText) {
            setOpaque(false);
            setLayout(new GridBagLayout());

            JPanel content = new JPanel();
            content.setOpaque(false);
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

            JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            iconLabel.setFont(new Font("SansSerif", Font.BOLD, 35));
            iconLabel.setForeground(new Color(154, 151, 220));

            JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            titleLabel.setForeground(NAVY);

            JLabel subtitleLabel = new JLabel(subtitle, SwingConstants.CENTER);
            subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            subtitleLabel.setForeground(MUTED);

            content.add(iconLabel);
            content.add(Box.createVerticalStrut(8));
            content.add(titleLabel);
            content.add(Box.createVerticalStrut(4));
            content.add(subtitleLabel);

            if (actionSource != null) {
                JButton action = new RoundedButton(actionText);
                styleButton(action, BLUE, Color.WHITE);
                action.setAlignmentX(Component.CENTER_ALIGNMENT);
                action.setPreferredSize(new Dimension(106, 28));
                action.addActionListener(event -> actionSource.doClick());
                content.add(Box.createVerticalStrut(10));
                content.add(action);
            }

            add(content);
        }

        private static void styleButton(JButton button, Color background,
                Color foreground) {
            button.setFont(new Font("SansSerif", Font.BOLD, 11));
            button.setBackground(background);
            button.setForeground(foreground);
            button.setFocusPainted(false);
            button.setContentAreaFilled(false);
            button.setOpaque(false);
            button.setBorder(new EmptyBorder(6, 12, 6, 12));
            button.putClientProperty("buttonColor", background);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    private static class GradientPanel extends JPanel {
        private final Color start;
        private final Color end;

        GradientPanel(Color start, Color end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setPaint(new GradientPaint(0, 0, start, getWidth(), getHeight(), end));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;

        RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(getBackground());
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.setColor(BORDER);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class RoundedButton extends JButton {
        RoundedButton(String text) {
            super(text);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            Color base = (Color) getClientProperty("buttonColor");
            if (base == null) {
                base = getBackground();
            }
            if (getModel().isPressed()) {
                base = base.darker();
            } else if (getModel().isRollover()) {
                base = base.brighter();
            }
            g.setColor(base);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class AnimatedBackgroundPanel extends JPanel {
        private double phase;

        AnimatedBackgroundPanel() {
            setOpaque(true);
            Timer timer = new Timer(45, event -> {
                phase += 0.012;
                repaint();
            });
            timer.setCoalesce(true);
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(91, 82, 230, 18));
            int firstX = (int) (getWidth() * 0.78 + Math.sin(phase) * 28);
            int firstY = (int) (getHeight() * 0.18 + Math.cos(phase) * 18);
            g.fillOval(firstX, firstY, 190, 190);
            g.setColor(new Color(38, 196, 190, 14));
            int secondX = (int) (getWidth() * 0.06 + Math.cos(phase) * 22);
            int secondY = (int) (getHeight() * 0.72 + Math.sin(phase) * 18);
            g.fillOval(secondX, secondY, 230, 230);
            g.dispose();
        }
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

            currentPreviewPage = 0;
            loadPreviewPage(0);
            previewCards.show(previewCenter, "table");

            for (int i = 0; i < previewTable.getColumnCount(); i++) {
                previewTable.getColumnModel().getColumn(i).setPreferredWidth(100);
            }

            resultsModel.setRowCount(0);
            resultsCards.show(resultsCenter, "empty");
            sortedPreview.setText("");
            bestLabel.setText("Run sorting to see the fastest algorithm");
            bestMetaLabel.setText("Run an analysis to reveal the champion");
            winnerBadge.setText("★  WAITING FOR RESULTS");

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
            resultsCards.show(resultsCenter, "empty");
            previewCards.show(previewCenter, "empty");

            currentPreviewPage = 0;
            prevPageButton.setEnabled(false);
            nextPageButton.setEnabled(false);
            pageLabel.setText("Page 1");

            fileNameLabel.setText("No CSV file selected");
            fileInfoLabel.setText("Unable to load the selected file");
            numericInfoLabel.setText("Numeric columns: 0");
            previewInfoLabel.setText("Preview unavailable");

            runButton.setEnabled(false);
            sortedPreview.setText("");
            bestLabel.setText("No evaluation yet");
            bestMetaLabel.setText("Run an analysis to reveal the champion");
            winnerBadge.setText("★  WAITING FOR RESULTS");
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
            resultsCards.show(resultsCenter, "table");

            for (PerformanceEvaluator.Result result : results) {
                resultsModel.addRow(new Object[] {
                        result.getAlgorithm(),
                        result.getTimeNs()
                });
            }

            // Numeric execution-time sorting
            if (resultsTable.getRowSorter() instanceof TableRowSorter) {
                @SuppressWarnings("unchecked")
                TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) resultsTable
                        .getRowSorter();

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
            winnerBadge.setText("★  FASTEST ALGORITHM");
            bestMetaLabel.setText("Best result for the “" + selectedColumn
                    + "” column • " + values.length + " values tested");

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

    private void loadPreviewPage(int page) {

        if (dataset == null)
            return;

        String[] columns = dataset.getColumnNames();
        String[][] rows = dataset.getData();

        int totalRows = rows.length;
        int totalPages = Math.max(1, (totalRows + PAGE_SIZE - 1) / PAGE_SIZE);

        currentPreviewPage = page;

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, totalRows);

        previewModel.setRowCount(0);

        for (int i = start; i < end; i++) {

            Object[] previewRow = new Object[columns.length];

            for (int j = 0; j < columns.length; j++) {
                previewRow[j] = rows[i] != null && j < rows[i].length
                        && rows[i][j] != null ? rows[i][j] : "";
            }

            previewModel.addRow(previewRow);
        }

        previewInfoLabel.setText(
                "Rows " + (start + 1) + "–" + end + " of " + totalRows);

        pageLabel.setText("Page " + (page + 1) + " of " + totalPages);
        prevPageButton.setEnabled(page > 0);
        nextPageButton.setEnabled(page < totalPages - 1);
    }
}
