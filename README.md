# PDF OCR Processor

A local, open-source PDF OCR processor built with Java using **Baidu PaddleOCR** and Apache PDFBox for PDF handling. PaddleOCR offers superior accuracy with deep learning-based text recognition.

## Features

✅ **Baidu PaddleOCR Integration** - State-of-the-art deep learning OCR engine  
✅ **Local Processing** - Run entirely on your machine, no cloud dependencies  
✅ **High Accuracy** - Advanced neural network models with angle detection  
✅ **Multi-language Support** - Currently configured for English (easily extensible)  
✅ **Batch Processing** - Process multiple PDF files in one go  
✅ **Multi-page Support** - Extracts text from all pages in PDF  
✅ **Logging** - Comprehensive logging with Logback  
✅ **Python Integration** - Seamless Java/Python interoperability  

## System Requirements

- **Java**: JDK 11 or higher
- **Maven**: 3.6.0 or higher
- **Python**: 3.7 or higher (with pip)
- **Memory**: 2GB RAM minimum (4GB recommended for better performance)
- **Disk Space**: 500MB for Java dependencies + ~200MB for PaddleOCR models

## Installation & Setup

### Step 1: Install Python and PaddleOCR

#### Windows Installation:

1. **Install Python 3.7+** (if not already installed):
   - Download from: https://www.python.org/downloads/
   - Make sure to check "Add Python to PATH" during installation

2. **Verify Python installation**:
   ```powershell
   python --version
   python3 --version
   ```

3. **Install PaddleOCR via pip**:
   ```powershell
   pip install paddleocr
   ```
   This will automatically download the required models (~200MB)

4. **Verify PaddleOCR installation**:
   ```powershell
   python -c "import paddleocr; print('PaddleOCR installed successfully')"
   ```

#### Linux Installation:

```bash
# Install Python (if needed)
sudo apt-get install python3 python3-pip

# Install PaddleOCR
pip3 install paddleocr
```

#### macOS Installation:

```bash
# Install Python via Homebrew (if needed)
brew install python3

# Install PaddleOCR
pip3 install paddleocr
```

### Step 2: Build the Project

```bash
cd C:\Users\Nagaraju M\IdeaProjects\test\OCRProject

# Download dependencies and build
mvn clean install

# Build fat JAR (includes all dependencies)
mvn clean package
```

This creates `target/pdf-ocr-processor-1.0.0.jar`

## Usage

### Single PDF Processing

```bash
java -jar target/pdf-ocr-processor-1.0.0.jar documents/sample.pdf output/extracted.txt
```

Output to console (without saving to file):
```bash
java -jar target/pdf-ocr-processor-1.0.0.jar documents/sample.pdf
```

### Batch Processing Multiple PDFs

```bash
java -jar target/pdf-ocr-processor-1.0.0.jar --batch documents output
```

Or using short flag:
```bash
java -jar target/pdf-ocr-processor-1.0.0.jar -b documents output
```

This will:
1. Process all `.pdf` files in the `documents` folder
2. Save extracted text to the `output` folder with `_extracted.txt` suffix
3. Display a summary report with processing statistics

### Using as Library in Your Code

```java
import com.ocr.OCRProcessor;
import java.util.List;

public class MyOCRApp {
    public static void main(String[] args) {
        // Single file processing
        OCRProcessor processor = new OCRProcessor();
        String extractedText = processor.processPDF("input.pdf", "output.txt");
        System.out.println(extractedText);
        
        // Batch processing
        List<OCRProcessor.ProcessingResult> results = 
            processor.processPDFDirectory("docs/", "output/");
        for (OCRProcessor.ProcessingResult result : results) {
            if (result.isSuccess()) {
                System.out.println(result.getFileName() + 
                    ": " + result.getCharactersExtracted() + " chars extracted");
            } else {
                System.out.println(result.getFileName() + 
                    ": FAILED - " + result.getError());
            }
        }
    }
}
```

## Best Approach for Local Running

### 1. **For Command-Line Usage** (Recommended for Simple Processing)

```bash
# Single file
java -jar target/pdf-ocr-processor-1.0.0.jar documents/sample.pdf

# Single file with output file
java -jar target/pdf-ocr-processor-1.0.0.jar documents/sample.pdf output/extracted.txt

# Batch processing (all PDFs in a directory)
java -jar target/pdf-ocr-processor-1.0.0.jar --batch documents output
```

### 2. **For IDE Integration** (IntelliJ IDEA)

1. Open the project in IntelliJ IDEA
2. File → Open → Select the OCRProject folder
3. Right-click on `OCRProcessor.java` → Run 'OCRProcessor.main()'
4. Or create a Run Configuration:
   - Main class: `com.ocr.OCRProcessor`
   - Program arguments: `documents/sample.pdf output/extracted.txt`
   - Working directory: `OCRProject` (project root)

### 3. **For Integration in Existing Project**

1. Add dependency to your `pom.xml`:
```xml
<dependency>
    <groupId>com.ocr</groupId>
    <artifactId>pdf-ocr-processor</artifactId>
    <version>1.0.0</version>
</dependency>
```

2. Install locally:
```bash
mvn install
```

3. Use in your code:
```java
OCRProcessor processor = new OCRProcessor();
String text = processor.processPDF("file.pdf", "output.txt");
```

## Project Structure

```
OCRProject/
├── pom.xml                                    # Maven configuration
├── src/
│   ├── main/
│   │   ├── java/com/ocr/
│   │   │   ├── OCRProcessor.java              # Main PDF OCR processor with PaddleOCR integration
│   │   │   └── ProcessingResult.java          # Result object for batch processing
│   │   └── resources/
│   │       ├── application.properties          # Application configuration
│   │       └── logback.xml                     # Logging configuration
│   └── test/
├── documents/                                  # Input PDF files for processing
├── output/                                     # Extracted text output
└── logs/                                       # Application logs
```

## Dependencies

| Dependency | Version | Purpose |
|-----------|---------|---------|
| pdfbox | 3.0.1 | PDF manipulation and rendering |
| commons-lang3 | 3.13.0 | Apache Commons utilities |
| gson | 2.10.1 | JSON processing |
| slf4j | 2.0.9 | Logging facade |
| logback | 1.4.11 | Logging implementation |
| paddleocr (Python) | Latest | Baidu PaddleOCR OCR engine |

**Note**: PaddleOCR is installed as a Python package, not a Maven dependency. The Java application calls Python to invoke PaddleOCR.

## Performance Tips

1. **Higher DPI = Better Accuracy (Slower)**
   - Edit `dpi` value in `OCRProcessor` class
   - Default: 300 DPI (good balance)
   - Range: 150-600 DPI
   - Higher DPI recommended for scanned documents with small text

2. **Language Settings**
   - Currently configured for English (`lang='en'`)
   - To use other languages, modify the PaddleOCR configuration:
     ```python
     ocr = paddleocr.PaddleOCR(use_angle_cls=True, lang='ch')  # Chinese
     ocr = paddleocr.PaddleOCR(use_angle_cls=True, lang='fr')  # French
     ```
   - Supported languages: `en`, `ch`, `fr`, `de`, `es`, `pt`, `ar`, `ja`, `ko`, etc.

3. **Memory Allocation**
   - For large PDFs, increase Java heap memory:
     ```bash
     java -Xmx4G -jar target/pdf-ocr-processor-1.0.0.jar documents/large.pdf
     ```

4. **PaddleOCR Configuration**
   - `use_angle_cls=True`: Enables angle detection for rotated text (slightly slower)
   - First run will download models (~200MB) and cache them
   - Models are cached locally for faster subsequent runs

## Troubleshooting

### Issue: "Python not found"

**Solution:**
```powershell
# Verify Python installation
python --version
python3 --version

# If not in PATH, add it explicitly
# Run the Python installer and check "Add Python to PATH"
```

### Issue: "paddleocr module not found"

**Solution:**
```powershell
# Install PaddleOCR
pip install paddleocr

# Or for Python 3 specifically
pip3 install paddleocr

# Verify installation
python -c "import paddleocr; print('OK')"
```

### Issue: "Out of Memory" error

**Solution:**
```bash
java -Xmx4G -jar target/pdf-ocr-processor-1.0.0.jar documents/huge.pdf
```

### Issue: Poor OCR accuracy

**Solutions:**
1. Increase DPI in `OCRProcessor` class (300→600)
2. Ensure PDF has good quality images
3. Change language setting if processing non-English documents
4. Verify PaddleOCR is properly installed with all dependencies
5. Check that the PDF is not encrypted or corrupted

### Issue: First run is very slow

**Explanation**: PaddleOCR models are downloaded and cached on first run (~200MB). Subsequent runs will be much faster as models are cached locally.

## Logging

Logs are stored in the `logs/` directory:

- **Console Output**: Real-time progress messages
- **File Logs**: `logs/ocr-processor.log` with detailed information
- **Log Level**: Set in `logback.xml`

## Running Tests

```bash
mvn test
```

## Advanced Usage

### Custom PaddleOCR Configuration

Edit the `extractTextViaPython()` method in `OCRProcessor.java`:

```python
# Current configuration (in Python code executed by Java)
ocr = paddleocr.PaddleOCR(use_angle_cls=True, lang='en')

# Available options:
ocr = paddleocr.PaddleOCR(
    use_angle_cls=True,      # Enable angle detection for rotated text
    lang='en',               # Language: 'en', 'ch', 'fr', 'de', 'es', etc.
    use_gpu=False            # Set to True if CUDA-capable GPU available
)
```

### Extract Text by Page

```java
OCRProcessor processor = new OCRProcessor();
String fullText = processor.processPDF("input.pdf", "output.txt");
// Text is organized with "--- PAGE X ---" markers for each page
```

### GPU Acceleration (Optional)

For NVIDIA GPU acceleration (requires CUDA):

```python
ocr = paddleocr.PaddleOCR(use_angle_cls=True, lang='en', use_gpu=True)
```

## Performance Benchmarks (Windows 10, i5 CPU, 8GB RAM)

- Single-page PDF: 3-7 seconds (first run: slower due to model loading)
- 10-page PDF: 30-60 seconds
- 100-page PDF: 5-10 minutes

*Times vary based on image quality, complexity, and DPI settings. First run downloads/caches models (~200MB).*

## License

- **PaddleOCR**: Apache License 2.0
- **PDFBox**: Apache License 2.0
- **This Project**: MIT License (you can modify freely)

## Next Steps

1. Install Python 3.7+ and PaddleOCR (`pip install paddleocr`)
2. Copy sample PDF files to `documents/` folder
3. Run `mvn clean package`
4. Test with: `java -jar target/pdf-ocr-processor-1.0.0.jar documents/sample.pdf`
5. Check `output/` folder for extracted text

## Support & Resources

- **PaddleOCR Documentation**: https://github.com/PaddlePaddle/PaddleOCR
- **PaddleOCR Supported Languages**: https://github.com/PaddlePaddle/PaddleOCR/blob/release/2.0/README.md
- **PDFBox Documentation**: https://pdfbox.apache.org/
- **Python PaddleOCR Package**: https://pypi.org/project/paddleocr/

---

**Ready to use locally! Install Python with PaddleOCR, build with Maven, and start processing PDFs with advanced deep learning-based OCR.**

