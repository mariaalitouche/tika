package org.apache.tika;

import org.apache.tika.Tika;
import org.apache.tika.detect.Detector;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.AutoDetectParser;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.File;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Path;
import java.util.Properties;
import org.xml.sax.SAXException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.exception.WriteLimitReachedException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.language.translate.DefaultTranslator;
import org.apache.tika.language.translate.Translator;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ParsingReader;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.WriteOutContentHandler;

public class Tika_detect_1_0_Test {

    @Test
    public void testDetect() throws IOException {
        Detector detector = new DefaultDetector();
        Parser parser = new AutoDetectParser(detector);
        Tika tika = new Tika(detector, parser);
        InputStream stream = new ByteArrayInputStream("Hello, World!".getBytes());
        Metadata metadata = new Metadata();
        String detectedType = tika.detect(stream, metadata);
        assertEquals("text/plain", detectedType);
    }
}
