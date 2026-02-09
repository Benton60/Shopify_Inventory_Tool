package com.hardware;

import com.opencsv.CSVReader;
import org.apache.poi.ss.usermodel.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import com.opencsv.CSVReader;
import org.apache.poi.ss.usermodel.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;

public class PreviewApp extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    private JPanel selectorPanel; // NEW row of checkboxes

    private String currentKey = null;
    private Component centerPanel;

    private static final String SHOPIFY = "shopify";
    private static final String LOCAL = "local";
    private JScrollPane mainScroll; // the scroll pane that wraps selector + table

    // =============================
    // Per-file state holder
    // =============================
    static class FileState {
        List<String[]> rows;
        int skuColumn = -1;
        int quantityColumn = -1;

        FileState(List<String[]> rows) {
            this.rows = rows;
        }
    }

    private final Map<String, FileState> files = new HashMap<>();

    // =============================
    public PreviewApp() {
        super("Inventory Compare Tool");

        setLayout(new BorderLayout());

        // ===== Toolbar =====
        JToolBar toolbar = new JToolBar();

        JButton shopifyBtn = new JButton("Shopify Export");
        JButton localBtn = new JButton("Local Inventory Export");
        JButton previewBtn = new JButton("Preview");
        JButton savePreviewBtn = new JButton("Save Preview");
        JButton discrepanciesBtn = new JButton("Discrepancies");
        JButton saveDiscrepanciesBtn = new JButton("Save Discrepancies");


        toolbar.add(shopifyBtn);
        toolbar.add(localBtn);
        toolbar.add(previewBtn);
        toolbar.add(discrepanciesBtn);
        toolbar.addSeparator();
        toolbar.add(saveDiscrepanciesBtn);
        toolbar.add(new JLabel("   (more tools coming soon)"));

        add(toolbar, BorderLayout.NORTH);

        // ===== Selector + Table layout =====
        selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

        model = new DefaultTableModel();
        table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(selectorPanel);
        contentPanel.add(tableScroll);

        mainScroll = new JScrollPane(contentPanel);
        mainScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        mainScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        centerPanel = mainScroll;
        add(centerPanel, BorderLayout.CENTER);

        // ===== Button actions =====
        shopifyBtn.addActionListener(e -> loadOrShow(SHOPIFY));
        localBtn.addActionListener(e -> loadOrShow(LOCAL));

        previewBtn.addActionListener(e -> showPreview());
        savePreviewBtn.addActionListener(e -> savePreview());
        discrepanciesBtn.addActionListener(e -> showDiscrepancies());
        saveDiscrepanciesBtn.addActionListener(e -> saveDiscrepancies());


        setSize(1200, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }



    // =============================
    // Load or show
    // =============================
    private void loadOrShow(String key) {

        currentKey = key;

        if (!files.containsKey(key)) {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

            File file = chooser.getSelectedFile();

            try {
                List<String[]> rows;

                if (file.getName().toLowerCase().endsWith(".csv")) {
                    rows = readCSV(file);
                } else if (file.getName().toLowerCase().matches(".*\\.(xls|xlsx|xlsm)$")) {
                    rows = readExcel(file);
                } else {
                    JOptionPane.showMessageDialog(this, "Unsupported file type");
                    return;
                }

                files.put(key, new FileState(rows));

            } catch (Exception ex) {
                logError(ex);
                return;
            }
        }

        FileState state = files.get(key);
        if (state == null) return;

        // --- Restore main scroll pane if not already ---
        if ((key.equals(SHOPIFY) || key.equals(LOCAL)) && centerPanel != mainScroll) {
            getContentPane().remove(centerPanel);
            centerPanel = mainScroll;
            add(centerPanel, BorderLayout.CENTER);
        }

        // --- Update table model ---
        model.setRowCount(0);
        model.setColumnCount(0);

        String[] headers = state.rows.get(0);
        for (String h : headers) model.addColumn(h);

        int limit = Math.min(10, state.rows.size() - 1);
        for (int i = 1; i <= limit; i++)
            model.addRow(state.rows.get(i));

        // --- Build selectors ---
        buildSelectors(state);

        revalidate();
        repaint();
    }

    // =============================
    // Build SKU/QUA checkboxes
    // =============================
    private void buildSelectors(FileState state) {

        selectorPanel.removeAll();

        String[] headers = state.rows.get(0);

        ButtonGroup skuGroup = new ButtonGroup();
        ButtonGroup quaGroup = new ButtonGroup();

        for (int col = 0; col < headers.length; col++) {

            int index = col;

            JPanel colPanel = new JPanel();
            colPanel.setLayout(new BoxLayout(colPanel, BoxLayout.Y_AXIS));

            JLabel label = new JLabel(headers[col]);

            JCheckBox sku = new JCheckBox("SKU");
            JCheckBox qua = new JCheckBox("QUA");

            skuGroup.add(sku);
            quaGroup.add(qua);

            if (state.skuColumn == index) sku.setSelected(true);
            if (state.quantityColumn == index) qua.setSelected(true);

            sku.addActionListener(e -> state.skuColumn = index);
            qua.addActionListener(e -> state.quantityColumn = index);

            colPanel.add(label);
            colPanel.add(sku);
            colPanel.add(qua);

            colPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

            selectorPanel.add(colPanel);
        }

        selectorPanel.revalidate();
        selectorPanel.repaint();
    }

    // =============================
    // Table preview
    // =============================
    private void updateTable(List<String[]> rows) {

        model.setRowCount(0);
        model.setColumnCount(0);

        if (rows.isEmpty()) return;

        String[] headers = rows.get(0);

        for (String h : headers)
            model.addColumn(h);

        int limit = Math.min(10, rows.size() - 1);

        for (int i = 1; i <= limit; i++)
            model.addRow(rows.get(i));
    }

    // =============================
    // CSV
    // =============================
    private List<String[]> readCSV(File file) throws Exception {
        CSVReader reader = new CSVReader(new FileReader(file));
        return reader.readAll();
    }

    // =============================
    // Excel (xls + xlsx)
    // =============================
    private List<String[]> readExcel(File file) throws Exception {

        List<String[]> rows = new ArrayList<>();

        Workbook workbook = WorkbookFactory.create(new FileInputStream(file));
        Sheet sheet = workbook.getSheetAt(0);

        DataFormatter formatter = new DataFormatter();

        for (Row row : sheet) {

            int last = row.getLastCellNum();
            String[] vals = new String[last];

            for (int i = 0; i < last; i++) {
                Cell c = row.getCell(i);
                vals[i] = c == null ? "" : formatter.formatCellValue(c);
            }

            rows.add(vals);
        }

        workbook.close();
        return rows;
    }

    private void showPreview() {
        FileState shopify = files.get(SHOPIFY);
        FileState local = files.get(LOCAL);

        if (shopify == null || local == null) {
            JOptionPane.showMessageDialog(this, "Both Shopify and Local files must be loaded!");
            return;
        }

        if (shopify.skuColumn == -1 || shopify.quantityColumn == -1
                || local.skuColumn == -1 || local.quantityColumn == -1) {
            JOptionPane.showMessageDialog(this, "Please select SKU and QUA columns in both files.");
            return;
        }

        // Build map: SKU -> QUA from local inventory
        Map<String, String> localMap = new HashMap<>();
        for (int i = 1; i < local.rows.size(); i++) {
            String sku = local.rows.get(i)[local.skuColumn];
            String qua = local.rows.get(i)[local.quantityColumn];
            localMap.put(sku, qua);
        }

        // Build preview rows (copy of Shopify)
        List<String[]> previewRows = new ArrayList<>();
        previewRows.add(shopify.rows.get(0).clone()); // header

        for (int i = 1; i < shopify.rows.size(); i++) {
            String[] row = shopify.rows.get(i).clone();
            String sku = row[shopify.skuColumn];
            if (localMap.containsKey(sku)) {
                row[shopify.quantityColumn] = localMap.get(sku);
            } else {
                row[shopify.quantityColumn] = ""; // missing SKU in local
            }
            previewRows.add(row);
        }

        // Store preview in memory
        files.put("preview", new FileState(previewRows));

        // --- Build preview panel ---
        JPanel previewPanel = new JPanel();
        previewPanel.setLayout(new BorderLayout());

        // Save Preview button at top
        JButton savePreviewBtn = new JButton("Save Preview");
        savePreviewBtn.addActionListener(e -> savePreview());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(savePreviewBtn);

        previewPanel.add(topPanel, BorderLayout.NORTH);

        // Table (full preview, no checkboxes)
        DefaultTableModel previewModel = new DefaultTableModel();
        JTable previewTable = new JTable(previewModel);
        previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // populate table
        if (!previewRows.isEmpty()) {
            String[] headers = previewRows.get(0);
            for (String h : headers) previewModel.addColumn(h);
            for (int i = 1; i < previewRows.size(); i++) previewModel.addRow(previewRows.get(i));
        }

        previewPanel.add(new JScrollPane(previewTable), BorderLayout.CENTER);

        // Replace current CENTER panel
        getContentPane().remove(centerPanel);
        centerPanel = previewPanel;
        add(centerPanel, BorderLayout.CENTER);
        revalidate();
        repaint();

        currentKey = "preview";
    }

    private void savePreview() {
        FileState preview = files.get("preview");

        if (preview == null) {
            JOptionPane.showMessageDialog(this, "No preview to save!");
            return;
        }

        // Open Save File dialog
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Preview As");
        chooser.setSelectedFile(new File("Updated_Shopify_Export.csv"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        try (PrintWriter pw = new PrintWriter(file)) {
            for (String[] row : preview.rows) {
                // Escape commas in values if needed
                for (int i = 0; i < row.length; i++) {
                    if (row[i].contains(",")) {
                        row[i] = "\"" + row[i].replace("\"", "\"\"") + "\"";
                    }
                }
                pw.println(String.join(",", row));
            }
            JOptionPane.showMessageDialog(this, "Preview saved as: " + file.getAbsolutePath());
        } catch (Exception ex) {
            logError(ex);
            JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage());
        }
    }

    private void showDiscrepancies() {
        FileState shopify = files.get(SHOPIFY);
        FileState local = files.get(LOCAL);

        if (shopify == null || local == null) {
            JOptionPane.showMessageDialog(this, "Both Shopify and Local files must be loaded!");
            return;
        }

        if (shopify.skuColumn == -1 || shopify.quantityColumn == -1
                || local.skuColumn == -1 || local.quantityColumn == -1) {
            JOptionPane.showMessageDialog(this, "Please select SKU and QUA columns in both files.");
            return;
        }

        // Map SKU -> QUA for Local Inventory
        Map<String, String> localMap = new HashMap<>();
        for (int i = 1; i < local.rows.size(); i++) {
            String sku = local.rows.get(i)[local.skuColumn];
            String qua = local.rows.get(i)[local.quantityColumn];
            localMap.put(sku, qua);
        }

        // Lists for the three categories
        List<String[]> localExtraOrBlank = new ArrayList<>();
        List<String[]> shopifyExtra = new ArrayList<>();
        List<String[]> blankShopify = new ArrayList<>();

        // Headers for all three tables
        String[] shopHeaders = shopify.rows.get(0);
        localExtraOrBlank.add(shopHeaders.clone());
        shopifyExtra.add(shopHeaders.clone());
        blankShopify.add(shopHeaders.clone());

        // Collect Local extra/blank SKUs
        for (int i = 1; i < local.rows.size(); i++) {
            String sku = local.rows.get(i)[local.skuColumn];
            if (sku == null || sku.trim().isEmpty() || !existsInShopify(sku, shopify)) {
                localExtraOrBlank.add(local.rows.get(i).clone());
            }
        }

        // Collect Shopify extra SKUs
        for (int i = 1; i < shopify.rows.size(); i++) {
            String sku = shopify.rows.get(i)[shopify.skuColumn];
            if (!localMap.containsKey(sku)) {
                shopifyExtra.add(shopify.rows.get(i).clone());
            }
        }

        // Collect blank Shopify SKUs
        for (int i = 1; i < shopify.rows.size(); i++) {
            String sku = shopify.rows.get(i)[shopify.skuColumn];
            if (sku == null || sku.trim().isEmpty()) {
                blankShopify.add(shopify.rows.get(i).clone());
            }
        }

        // Build a horizontal panel for 3 tables
        JPanel discrepancyPanel = new JPanel(new GridLayout(1, 3));

        discrepancyPanel.add(createScrollTable(localExtraOrBlank, "Extra / Blank Inventory SKUs"));
        discrepancyPanel.add(createScrollTable(shopifyExtra, "Extra Shopify SKUs"));
        discrepancyPanel.add(createScrollTable(blankShopify, "Blank Shopify SKUs"));

        // --- FIX: Replace only CENTER ---
        // --- Replace only CENTER ---
        getContentPane().remove(centerPanel);
        centerPanel = discrepancyPanel;
        add(centerPanel, BorderLayout.CENTER);
        revalidate();
        repaint();


        currentKey = "discrepancies";
    }

    // Helper to create a JScrollPane with a table
    private JScrollPane createScrollTable(List<String[]> rows, String title) {
        DefaultTableModel tempModel = new DefaultTableModel();
        JTable tempTable = new JTable(tempModel);
        tempTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        if (!rows.isEmpty()) {
            String[] headers = rows.get(0);
            for (String h : headers) tempModel.addColumn(h);

            for (int i = 1; i < rows.size(); i++) tempModel.addRow(rows.get(i));
        }

        JScrollPane scroll = new JScrollPane(tempTable);
        scroll.setBorder(BorderFactory.createTitledBorder(title));
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        return scroll;
    }

    // Helper: check if SKU exists in Shopify
    private boolean existsInShopify(String sku, FileState shopify) {
        for (int i = 1; i < shopify.rows.size(); i++) {
            String s = shopify.rows.get(i)[shopify.skuColumn];
            if (s != null && s.equals(sku)) return true;
        }
        return false;
    }

    private void saveDiscrepancies() {
        FileState shopify = files.get(SHOPIFY);
        FileState local = files.get(LOCAL);

        if (shopify == null || local == null) {
            JOptionPane.showMessageDialog(this, "Both Shopify and Local files must be loaded!");
            return;
        }

        if (shopify.skuColumn == -1 || shopify.quantityColumn == -1
                || local.skuColumn == -1 || local.quantityColumn == -1) {
            JOptionPane.showMessageDialog(this, "Please select SKU and QUA columns in both files.");
            return;
        }

        // Map SKU -> QUA for Local Inventory
        Map<String, String> localMap = new HashMap<>();
        for (int i = 1; i < local.rows.size(); i++) {
            String sku = local.rows.get(i)[local.skuColumn];
            String qua = local.rows.get(i)[local.quantityColumn];
            localMap.put(sku, qua);
        }

        // Lists for the three categories
        List<String[]> localExtraOrBlank = new ArrayList<>();
        List<String[]> shopifyExtra = new ArrayList<>();
        List<String[]> blankShopify = new ArrayList<>();

        String[] headers = shopify.rows.get(0);

        // Collect Local extra/blank SKUs
        for (int i = 1; i < local.rows.size(); i++) {
            String sku = local.rows.get(i)[local.skuColumn];
            if (sku == null || sku.trim().isEmpty() || !existsInShopify(sku, shopify)) {
                localExtraOrBlank.add(local.rows.get(i).clone());
            }
        }

        // Collect Shopify extra SKUs
        for (int i = 1; i < shopify.rows.size(); i++) {
            String sku = shopify.rows.get(i)[shopify.skuColumn];
            if (!localMap.containsKey(sku)) {
                shopifyExtra.add(shopify.rows.get(i).clone());
            }
        }

        // Collect blank Shopify SKUs
        for (int i = 1; i < shopify.rows.size(); i++) {
            String sku = shopify.rows.get(i)[shopify.skuColumn];
            if (sku == null || sku.trim().isEmpty()) {
                blankShopify.add(shopify.rows.get(i).clone());
            }
        }

        // Open Save File dialog
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Discrepancies");
        chooser.setSelectedFile(new File("Discrepancies.csv"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();

        try (PrintWriter pw = new PrintWriter(file)) {
            // Helper to write a category
            writeCategory(pw, "Extra / Blank Inventory SKUs", headers, localExtraOrBlank);
            writeBlankLines(pw, 10);

            writeCategory(pw, "Extra Shopify SKUs", headers, shopifyExtra);
            writeBlankLines(pw, 10);

            writeCategory(pw, "Blank Shopify SKUs", headers, blankShopify);

            JOptionPane.showMessageDialog(this, "Discrepancies saved as: " + file.getAbsolutePath());
        } catch (Exception ex) {
            logError(ex);
            JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage());
        }
    }

    // Writes a category title + headers + rows
    private void writeCategory(PrintWriter pw, String title, String[] headers, List<String[]> rows) {
        pw.println(title);
        pw.println(String.join(",", headers));
        for (String[] row : rows) {
            // Escape commas
            for (int i = 0; i < row.length; i++) {
                if (row[i].contains(",")) {
                    row[i] = "\"" + row[i].replace("\"", "\"\"") + "\"";
                }
            }
            pw.println(String.join(",", row));
        }
    }

    // Write N blank lines
    private void writeBlankLines(PrintWriter pw, int n) {
        for (int i = 0; i < n; i++) pw.println();
    }


    // Simple error logger
    private void logError(Exception ex) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("errors.txt", true))) {
            pw.println("=== " + new Date() + " ===");
            ex.printStackTrace(pw);
            pw.println();
        } catch (IOException ioEx) {
            logError(ioEx); // fallback to console
        }
    }

    // Overload for messages
    private void logError(String message) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("errors.txt", true))) {
            pw.println("=== " + new Date() + " ===");
            pw.println(message);
            pw.println();
        } catch (IOException ioEx) {
            logError(ioEx); // fallback
        }
    }

    // =============================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PreviewApp().setVisible(true));
    }
}
