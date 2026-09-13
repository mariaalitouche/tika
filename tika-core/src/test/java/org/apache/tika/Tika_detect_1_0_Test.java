/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.*;

import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;

public class Tika_detect_1_0_Test {

    @Test
    public void testDetect() throws IOException {
        Detector detector = new DefaultDetector();
        Parser parser = new AutoDetectParser(detector);
        Tika tika = new Tika(detector, parser);
        InputStream stream = new ByteArrayInputStream("This is a plain text document for testing purposes.".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Metadata metadata = new Metadata();
        String detectedType = tika.detect(stream, metadata);
        assertEquals("text/plain", detectedType);
    }
}
