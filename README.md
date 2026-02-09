# Shopify Inventory Tool

## Overview

The **Shopify Inventory Tool** is a Java desktop application designed to help merchants and inventory managers easily compare Shopify export files with local inventory data.  
It supports **CSV** and **Excel** files, provides a preview of inventory adjustments, highlights discrepancies, and allows saving updated files for downstream use.

This tool is cross-platform and runs on **Windows** and **Linux** with Java 17+.

---

## Features

- **Load Shopify Export and Local Inventory Files**: Supports `.csv`, `.xls`, `.xlsx`, and `.xlsm` formats.
- **Preview Updated Inventory**: Matches Shopify SKUs with local inventory and updates QUA values in a preview table.
- **Discrepancy Analysis**: Identify:
    - Extra or blank SKUs in local inventory
    - Extra SKUs in Shopify export
    - Blank SKUs in Shopify export
- **Save Functionality**:
    - Save the previewed inventory to a CSV file
    - Save all discrepancies to a single CSV file, separated by type
- **Error Logging**: Logs exceptions to `errors.txt`
- **Help**: In-app help button opens this README in your default browser

---

## How to Use

1. Ensure you have **Java 17+** installed.
2. Launch the application:

```bash
java -jar ShopifyInventoryTool.jar
