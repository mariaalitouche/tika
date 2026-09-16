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
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.*;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;

public class TikaDetectInputStreamMetadataTest {
    private Tika tika;

    @BeforeEach
    public void setUp() {
        tika = new Tika();
    }

    // cas avec input non nul et métadonnée nul
    @Test
    public void testDetectWithNullInputStreamAndEmptyMetadata() throws IOException {
        Metadata metadata = new Metadata(); // on instancie un objet vide

        // flux nul + métadonnée vide nous donnera un fallback
        String result = tika.detect((InputStream) null, metadata);

        // doit retourner la valeur par défaut "application/octet-stream"
        assertEquals("application/octet-stream", result);
    }

    // cas input null et métadonnée non nul
    @Test
    public void testDetectWithNullInputStreamAndFilenameInMetadata() throws IOException {
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, "document.pdf");

        String result = tika.detect((InputStream) null, metadata);

        assertEquals("application/pdf", result);
    }

    // cas input non vide et métadonnée nul
    @Test
    public void testDetectWithValidInputStreamAndEmptyMetadata() throws IOException {
        byte[] htmlData = "<html><body></body></html>".getBytes(StandardCharsets.UTF_8);
        InputStream input = new ByteArrayInputStream(htmlData);
        Metadata metadata = new Metadata();

        String result = tika.detect(input, metadata);

        assertEquals("text/html", result);
    }

    // cas avec un inpt valide mais une métadonée fausse
    @Test
    public void testDetectWithValidInputStreamAndWrongMetadata() throws IOException {
        byte[] htmlData = "<html><body></body></html>".getBytes(StandardCharsets.UTF_8);
        InputStream input = new ByteArrayInputStream(htmlData);
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, "document.txt");

        String result = tika.detect(input, metadata);

        assertEquals("text/html", result);
    }

}
