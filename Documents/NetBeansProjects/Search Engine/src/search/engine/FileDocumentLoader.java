/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package search.engine;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/*
 * Loads Document objects from real files in a folder, instead of using
 * the hardcoded SampleDocuments. Supports both:
 *   - .txt files (read directly as plain text)
 *   - .pdf files (text extracted using Apache PDFBox)
 *
 * Each file becomes one Document: the filename (without extension) is
 * the title, and the extracted text is the searchable content.
 */
public class FileDocumentLoader {

    /**
     * Reads every .txt and .pdf file directly inside the given folder and
     * returns them as a list of Documents with sequential ids.
     */
    public static List<Document> loadFromFolder(String folderPath) throws IOException {
        List<Document> docs = new ArrayList<>();
        Path folder = Paths.get(folderPath);

        System.out.println("Looking for documents in: " + folder.toAbsolutePath());

        if (!Files.isDirectory(folder)) {
            throw new IOException("Not a folder: " + folder.toAbsolutePath());
        }

        int id = 1;
        try (Stream<Path> files = Files.list(folder)) {
            List<Path> allFiles = files.toList();
            System.out.println("Found " + allFiles.size() + " item(s) in that folder: " + allFiles);

            List<Path> supportedFiles = allFiles.stream()
                    .filter(p -> {
                        String name = p.toString().toLowerCase();
                        return name.endsWith(".txt") || name.endsWith(".pdf");
                    })
                    .sorted()
                    .toList();

            for (Path file : supportedFiles) {
                String fileName = file.getFileName().toString();
                String lowerName = fileName.toLowerCase();
                String title = fileName.substring(0, fileName.lastIndexOf('.'));

                String content;
                try {
                    if (lowerName.endsWith(".pdf")) {
                        content = extractPdfText(file);
                    } else {
                        content = Files.readString(file);
                    }
                } catch (Exception e) {
                    System.out.println("Skipping '" + fileName + "' -- could not read it (" + e.getMessage() + ")");
                    continue;
                }

                docs.add(new Document(id++, title, content));
            }
        }

        return docs;
    }

    /** Extracts all text from a PDF file using Apache PDFBox. */
    private static String extractPdfText(Path pdfFile) throws IOException {
        try (PDDocument pdf = PDDocument.load(pdfFile.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(pdf);
        }
    }
}