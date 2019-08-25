package io.github.parkerm;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.parkerm.fixtures.NotNullObject;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class GeneratePojoJsonSchemaTaskTest {

    private GeneratePojoJsonSchemaTask task;

    @BeforeEach
    void setUp() {
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("io.github.parkerm.generate-pojo-json-schema");

        task = (GeneratePojoJsonSchemaTask) project.getTasks().findByName("generateJsonSchema");
    }

    @AfterEach
    void tearDown() {
        task.setOutStream(System.out);
    }

    @Test
    void defaultConfigurationProperties() {
        assertThat(task.getClasses())
                .isNotNull()
                .isEmpty();
        assertThat(task.isPrettyPrint())
                .isTrue();
        assertThat(task.getMapper())
                .isInstanceOf(ObjectMapper.class);
        assertThat(task.getOutStream())
                .isEqualTo(System.out);
    }

    @Nested
    class schemaGeneration {

        private PrintStream outStream;

        @BeforeEach
        void setUp() {
            outStream = Mockito.mock(PrintStream.class);
            task.setOutStream(outStream);
        }

        @Test
        void notNullValidator() {
            task.getClasses().add(NotNullObject.class);

            task.action();

            ArgumentCaptor<String> outputCaptor = ArgumentCaptor.forClass(String.class);
            verify(outStream).println(outputCaptor.capture());

            String expectedJson =
                    "{\n" +
                    "  \"$schema\" : \"http://json-schema.org/draft-04/schema#\",\n" +
                    "  \"title\" : \"Not Null Object\",\n" +
                    "  \"type\" : \"object\",\n" +
                    "  \"additionalProperties\" : false,\n" +
                    "  \"properties\" : {\n" +
                    "    \"str\" : {\n" +
                    "      \"type\" : \"string\"\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"required\" : [ \"str\" ]\n" +
                    "}";
            assertThat(outputCaptor.getValue())
                    .isEqualToNormalizingNewlines(expectedJson);
        }
    }
}