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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;

public class TikaDetectStringTest {

    @Test
    public void testDetectThrowsIllegalStateExceptionOnIOException() throws Exception {
        // on forme le comportement simulé
        // on crée un détecteur qui lève une exception
        // au moment de lancer notre appel a detect
        Detector detector = new Detector() {
            @Override
            public MediaType detect(TikaInputStream input, Metadata metadata, ParseContext context)
                    throws IOException {
                throw new IOException("Erreur simulée");
            }
        };

        // on crée l'instance de Tika qui utilise ce détecteur
        Tika tika = new Tika(detector);

        // on exécute et vérifie que l'exception est bien relancée
        assertThrows(IllegalStateException.class, () -> {
            tika.detect("nom_fichier");
        });
    }
}