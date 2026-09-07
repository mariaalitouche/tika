package org.apache.tika.serialization;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.serialization.JsonMetadata;
import org.apache.tika.serialization.serdes.MetadataSerializer;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.io.IOException;
import java.lang.reflect.Field;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JsonMetadata_toJson_3_0_Test {

    @Spy
    private Metadata metadata;

    @Mock
    private Writer writer;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ObjectMapper prettySerializer;

    @BeforeEach
    public void setUp() throws Exception {
        Field prettySerializerField = JsonMetadata.class.getDeclaredField("PRETTY_SERIALIZER");
        prettySerializerField.setAccessible(true);
        prettySerializerField.set(null, prettySerializer);
        Field objectMapperField = JsonMetadata.class.getDeclaredField("OBJECT_MAPPER");
        objectMapperField.setAccessible(true);
        objectMapperField.set(null, objectMapper);
        when(JsonMetadata.getStreamReadConstraints()).thenReturn(mock(StreamReadConstraints.class));
    }

    @Test
    public void testToJsonWithPrettyPrint() throws IOException {
        JsonMetadata.setPrettyPrinting(true);
        JsonMetadata.toJson(metadata, writer);
        verify(prettySerializer, times(1)).writerWithDefaultPrettyPrinter();
        verify(prettySerializer, times(1)).writeValue(writer, metadata);
    }

    @Test
    public void testToJsonWithoutPrettyPrint() throws IOException {
        JsonMetadata.setPrettyPrinting(false);
        JsonMetadata.toJson(metadata, writer);
        verify(objectMapper, times(1)).writeValue(writer, metadata);
    }

    @Test
    public void testGetStreamReadConstraints() {
        JsonMetadata.setStreamReadConstraints(mock(StreamReadConstraints.class));
        assertEquals(JsonMetadata.getStreamReadConstraints(), mock(StreamReadConstraints.class));
    }

    @Test
    public void testSetStreamReadConstraints() {
        JsonMetadata.setStreamReadConstraints(mock(StreamReadConstraints.class));
        assertEquals(JsonMetadata.getStreamReadConstraints(), mock(StreamReadConstraints.class));
    }
}
