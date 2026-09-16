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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TikaDetectStringTest {

    private Tika tika;

    @BeforeEach
    public void setUp() {
        tika = new Tika();
    }

    @Test
    public void testDetectThrowsIllegalStateExceptionOnIOException() throws Exception {
        // on crée le spy Mockito
        Tika tikaSpy = spy(tika);

        // on forme le comportement simulé
        // on indique au Mockito de lancer une exception au moment de lancer
        // notre appel a detect
        doThrow(new IOException("Erreur simulee"))
            // n'importe quel flux et nom de fichier "nom_fichier"
            .when(tikaSpy).detect((InputStream) any(), eq("nom_fichier"));

        // on exécute et vérifie que l'exception est bien relancée
        assertThrows(IllegalStateException.class, () -> {
            tikaSpy.detect("nom_fichier");
        });
    }
}
