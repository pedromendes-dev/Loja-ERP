package com.example.ERP.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ReportsPackager {
    public static void main(String[] args) throws Exception {
        // Utility CLI: generate and zip
        generateAll();
        Path zip = zipExports();
        System.out.println("Reports packaged: " + zip.toAbsolutePath());
    }

    public static void generateAll() throws Exception {
        // exec generators (using classes in same project)
        ExportCsvPrinter.main(new String[]{});
        ClientsPdfReport.main(new String[]{});
        ProductsPdfReport.main(new String[]{});
        SalesPdfReport.main(new String[]{});
    }

    public static Path zipExports() throws Exception {
        Path exports = Path.of("data", "exports");
        if (!Files.exists(exports)) throw new IllegalStateException("exports folder not found");
        Path out = exports.resolve("reports.zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(out.toFile()))) {
            Files.walk(exports).filter(p->Files.isRegularFile(p)).forEach(p->{
                try {
                    String entryName = exports.relativize(p).toString().replace('\\','/');
                    zos.putNextEntry(new ZipEntry(entryName));
                    try (FileInputStream fis = new FileInputStream(p.toFile())) {
                        byte[] buf = new byte[8192];
                        int r;
                        while ((r = fis.read(buf)) > 0) zos.write(buf,0,r);
                    }
                    zos.closeEntry();
                } catch (Exception ex) { throw new RuntimeException(ex); }
            });
        }
        return out;
    }
}

