package com.example.ERP.tools;

import java.nio.file.Path;

public class ZipExportsOnly {
    public static void main(String[] args) throws Exception {
        Path zip = ReportsPackager.zipExports();
        System.out.println("Zipped: " + zip.toAbsolutePath());
    }
}

