# Shopify Inventory Tool

## Overview

The **Shopify Inventory Tool** is a Java desktop application that helps merchants and inventory managers compare Shopify exports with local inventory files. It was written for a specific Hardware stores' use case so some of the features might seem odd but the tool was built for a specific use case not for general use. That being said much of the structure and feature set are applicable for other stores and I welcome anyone using and making changes/contributions so long as they don't affect the current usability.

As of right now supports **CSV** and **Excel** files, provides previews of inventory updates, highlights discrepancies, and allows saving updated data for further use. If you have any questions over the code or how to compile/use it feel free to contact me through Github.

The application runs on **Windows** and **Linux** with Java 17+.

---

## Features

- Load Shopify export and local inventory files (`.csv`, `.xls`, `.xlsx`, `.xlsm`)
- Preview Shopify inventory updated with local quantities
- Identify discrepancies:
    - Extra or blank SKUs in local inventory
    - Extra SKUs in Shopify export
    - Blank SKUs in Shopify export
- Save previews and discrepancies to CSV
- Error logging to `errors.txt`
- Help button opens this README in your browser

---

## How to Use

### 1. Launch the Application

Make sure you have **Java 17+** installed, compile using:

```bash
maven clean package
```
You will se two jar files. One is a FAT jar the other is a regular jar. The larger one is the FAT and therefore the standalone file to run using:
```bash
java -jar <Insert your jar file name>
```

---

### 2. Load Files

- **Shopify Export:** Click the **Shopify Export** button and select your Shopify CSV or Excel file.
- **Local Inventory Export:** Click the **Local Inventory Export** button and select your local inventory file.

---

### 3. Select Columns

Above the table, each column has **SKU** and **QUA** checkboxes:

- Check **SKU** for the column containing product SKUs.
- Check **QUA** for the column containing quantities.
- Only one column can be selected as SKU and one as QUA per file.

---

### 4. Preview Tab

- Click the **Preview** button.
- This shows a **copy of the Shopify export** with quantities replaced by the local inventory where SKUs match.
- **Note:** Only the first 10 rows are shown for quick preview.
- Use the **Save Preview** button inside the preview tab to save the updated inventory to a CSV file.

---

### 5. Discrepancies Tab

- Click the **Discrepancies** button.
- Shows **all discrepancies** between the two files:
    - **Extra/Blank Inventory SKUs** – SKUs in local inventory missing or blank in Shopify
    - **Extra Shopify SKUs** – SKUs in Shopify missing in local inventory
    - **Blank Shopify SKUs** – rows in Shopify with blank SKU fields
- Use the **Save Discrepancies** button in this tab to export all discrepancies to a CSV file. Each type is separated by ten empty rows with a title row.

---

### 6. Help Button

- Opens this README in your default web browser for reference.

---

## File Output

- **Preview CSV:** Saved from the Preview tab.
- **Discrepancies CSV:** Saved from the Discrepancies tab.
- **Error Log:** `errors.txt` logs any exceptions or issues encountered during processing.

---

## Requirements

- Java 17 or higher
- OpenCSV (`opencsv-5.x.jar`)
- Apache POI (`poi` and `poi-ooxml`)

---

## License

This project is licensed under the **MIT License**:

```
MIT License

Copyright (c) 2026 Benton Hershberger

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## Contributing

Contributions, issues, and feature requests are welcome!

1. Fork the repository
2. Make your changes in a feature branch
3. Submit a pull request

---

## Repository

[GitHub: Shopify Inventory Tool](https://github.com/Benton60/Shopify_Inventory_Tool/tree/master#readme)

