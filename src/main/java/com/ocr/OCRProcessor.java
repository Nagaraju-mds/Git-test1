package com.ocr;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
/**
 * PDF OCR Processor using Baidu PaddleOCR via Python
 */
public class OCRProcessor {
    private static final Logger logger = LoggerFactory.getLogger(OCRProcessor.class);
    private final int dpi;
    private String pythonPath;
    public OCRProcessor() {
        this.dpi = 300;
        this.pythonPath = findPythonPath();
    }
    private String findPythonPath() {
        try {
            ProcessBuilder pb = new ProcessBuilder("python3", "--version");
            Process p = pb.start();
            if (p.waitFor() == 0) return "python3";
        } catch (Exception e) {}
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "--version");
            Process p = pb.start();
            if (p.waitFor() == 0) return "python";
        } catch (Exception e) {}
        throw new RuntimeException("Python not found. Install Python 3.7+ with: pip install paddleocr");
    }
    public String processPDF(String pdfFilePath, String outputPath) {
        if (pdfFilePath == null || pdfFilePath.isEmpty()) {
            throw new IllegalArgumentException("PDF file path cannot be null or empty");
        }
        StringBuilder allText = new StringBuilder();
        File tempDir = null;
        try (PDDocument document = Loader.loadPDF(new File(pdfFilePath))) {
            int pages = document.getNumberOfPages();
            logger.info("Processing PDF: {} (Total pages: {})", pdfFilePath, pages);
            tempDir = Files.createTempDirectory("ocr_").toFile();
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int pageIndex = 0; pageIndex < pages; pageIndex++) {
                logger.debug("Processing page {}/{}", pageIndex + 1, pages);
                BufferedImage pageImage = pdfRenderer.renderImageWithDPI(pageIndex, dpi);
                String imagePath = new File(tempDir, "page_" + pageIndex + ".png").getAbsolutePath();
                ImageIO.write(pageImage, "png", new File(imagePath));
                String pageText = extractTextViaPython(imagePath);
                allText.append("--- PAGE ").append(pageIndex + 1).append(" ---\n");
                allText.append(pageText).append("\n\n");
                pageImage.flush();
            }
            if (outputPath != null && !outputPath.isEmpty()) {
                saveTextToFile(outputPath, allText.toString());
                logger.info("Extracted text saved to: {}", outputPath);
            }
            logger.info("PDF processing completed: {}", pdfFilePath);
        } catch (IOException e) {
            logger.error("Error reading PDF: {}", pdfFilePath, e);
            throw new RuntimeException("Failed to read PDF file: " + pdfFilePath, e);
        } finally {
            if (tempDir != null) deleteDirectory(tempDir);
        }
        return allText.toString();
    }
    public List<ProcessingResult> processPDFDirectory(String inputDirectory, String outputDirectory) {
        List<ProcessingResult> results = new ArrayList<>();
        try {
            File dir = new File(inputDirectory);
            if (!dir.isDirectory()) throw new IllegalArgumentException("Not a directory: " + inputDirectory);
            Files.createDirectories(Paths.get(outputDirectory));
            File[] pdfFiles = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".pdf"));
            if (pdfFiles == null || pdfFiles.length == 0) {
                logger.warn("No PDF files found: {}", inputDirectory);
                return results;
            }
            logger.info("Found {} PDF files", pdfFiles.length);
            for (File pdfFile : pdfFiles) {
                ProcessingResult result = processSingleFile(pdfFile, outputDirectory);
                results.add(result);
            }
            printBatchSummary(results);
        } catch (Exception e) {
            logger.error("Batch processing failed", e);
            throw new RuntimeException("Batch processing failed", e);
        }
        return results;
    }
    private ProcessingResult processSingleFile(File pdfFile, String outputDirectory) {
        ProcessingResult result = new ProcessingResult(pdfFile.getName());
        try {
            String outputFileName = pdfFile.getName().replaceAll("\\.pdf$", "") + "_extracted.txt";
            String outputPath = Paths.get(outputDirectory, outputFileName).toString();
            long startTime = System.currentTimeMillis();
            String extractedText = processPDF(pdfFile.getAbsolutePath(), outputPath);
            long duration = System.currentTimeMillis() - startTime;
            result.setSuccess(true);
            result.setOutputFile(outputPath);
            result.setDuration(duration);
            result.setCharactersExtracted(extractedText.length());
            logger.info("Processed: {} ({} ms)", pdfFile.getName(), duration);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setError(e.getMessage());
            logger.error("Failed: {}", pdfFile.getName(), e);
        }
        return result;
    }
    private String extractTextViaPython(String imagePath) {
        try {
            String pythonCode = "import paddleocr\n" +
                    "ocr = paddleocr.PaddleOCR(use_angle_cls=True, lang='en')\n" +
                    "result = ocr.ocr('" + imagePath.replace("\\", "\\\\") + "', cls=True)\n" +
                    "if result:\n    for line in result:\n        for word_info in line:\n            print(word_info[1][0])";
            ProcessBuilder pb = new ProcessBuilder(pythonPath, "-c", pythonCode);
            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            if (process.waitFor() == 0) {
                return output.toString().trim();
            }
            return "[OCR Failed]";
        } catch (Exception e) {
            logger.error("OCR failed", e);
            return "[OCR Failed]";
        }
    }
    private void saveTextToFile(String outputPath, String content) {
        try {
            Path path = Paths.get(outputPath);
            Files.createDirectories(path.getParent());
            Files.write(path, content.getBytes());
        } catch (IOException e) {
            logger.error("Error saving file: {}", outputPath, e);
            throw new RuntimeException("Failed to save file: " + outputPath, e);
        }
    }
    private void deleteDirectory(File directory) {
        try {
            if (!directory.exists()) return;
            File[] files = directory.listFiles();
            if (files != null) for (File f : files) if (f.isDirectory()) deleteDirectory(f); else f.delete();
            directory.delete();
        } catch (Exception e) {}
    }
    private void printBatchSummary(List<ProcessingResult> results) {
        System.out.println("\n========== BATCH SUMMARY ==========\n");
        int success = 0, fail = 0;
        long totalTime = 0, totalChars = 0;
        for (ProcessingResult r : results) {
            if (r.isSuccess()) {
                success++;
                totalTime += r.getDuration();
                totalChars += r.getCharactersExtracted();
                System.out.printf("✓ %s (%.2f sec, %d chars)%n", r.getFileName(), r.getDuration() / 1000.0, r.getCharactersExtracted());
            } else {
                fail++;
                System.out.printf("✗ %s (Error: %s)%n", r.getFileName(), r.getError());
            }
        }
        System.out.printf("\nTotal: %d | Success: %d | Failed: %d | Time: %.2f sec | Chars: %d\n====================================\n",
                results.size(), success, fail, totalTime / 1000.0, totalChars);
    }
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("PDF OCR Processor - Baidu PaddleOCR\nPreq: Python 3.7+ with: pip install paddleocr\nUsage: java -jar pdf-ocr-processor.jar <pdf> [output] or --batch <dir> [out]");
            System.exit(1);
        }
        try {
            OCRProcessor processor = new OCRProcessor();
            if ("--batch".equals(args[0])) {
                if (args.length < 2) { System.err.println("Batch mode needs input directory"); System.exit(1); }
                processor.processPDFDirectory(args[1], args.length > 2 ? args[2] : "output");
            } else {
                String text = processor.processPDF(args[0], args.length > 1 ? args[1] : null);
                System.out.println("\n========== EXTRACTED TEXT ==========\n" + text + "\n====================================\n");
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(OCRProcessor.class).error("Error", e);
            e.printStackTrace();
            System.exit(1);
        }
    }
    public static class ProcessingResult {
        private final String fileName;
        private boolean success;
        private String outputFile;
        private long duration;
        private long charactersExtracted;
        private String error;
        public ProcessingResult(String fileName) { this.fileName = fileName; }
        public String getFileName() { return fileName; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getOutputFile() { return outputFile; }
        public void setOutputFile(String outputFile) { this.outputFile = outputFile; }
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        public long getCharactersExtracted() { return charactersExtracted; }
        public void setCharactersExtracted(long charactersExtracted) { this.charactersExtracted = charactersExtracted; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
