package io.github.parkerm;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.parkerm.assertions.JsonStringAssertion;
import io.github.parkerm.fixtures.NotNullObject;
import io.github.parkerm.fixtures.PlainObject;
import io.github.parkerm.fixtures.TestEnum;
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
    class configuration {
        private PrintStream outStream;

        @BeforeEach
        void setUp() {
            outStream = Mockito.mock(PrintStream.class);
            task.setOutStream(outStream);
        }

        @Test
        void prettyPrint_enabled() {
            task.getClasses().add(PlainObject.class);
            task.setPrettyPrint(true);
            task.action();

            ArgumentCaptor<String> outputCaptor = ArgumentCaptor.forClass(String.class);
            verify(outStream).println(outputCaptor.capture());

            String expectedPrettyJson =
                    "{\n" +
                    "  \"$schema\" : \"http://json-schema.org/draft-04/schema#\",\n" +
                    "  \"title\" : \"Plain Object\",\n" +
                    "  \"type\" : \"object\",\n" +
                    "  \"additionalProperties\" : false,\n" +
                    "  \"properties\" : {\n" +
                    "    \"str\" : {\n" +
                    "      \"type\" : \"string\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";
            assertThat(outputCaptor.getValue())
                    .isEqualToNormalizingNewlines(expectedPrettyJson);
        }

        @Test
        void prettyPrint_disabled() {
            task.getClasses().add(PlainObject.class);
            task.setPrettyPrint(false);
            task.action();

            ArgumentCaptor<String> outputCaptor = ArgumentCaptor.forClass(String.class);
            verify(outStream).println(outputCaptor.capture());

            String expectedUglyJson =
                    "{\"$schema\":\"http://json-schema.org/draft-04/schema#\",\"title\":\"Plain Object\",\"type\":\"object\",\"additionalProperties\":false,\"properties\":{\"str\":{\"type\":\"string\"}}}";
            assertThat(outputCaptor.getValue())
                    .isEqualTo(expectedUglyJson);
        }
    }

    @Nested
    class schemaGeneration {

        private ArgumentCaptor<String> outputCaptor;
        private PrintStream outStream;

        @BeforeEach
        void setUp() {
            outputCaptor = ArgumentCaptor.forClass(String.class);
            outStream = Mockito.mock(PrintStream.class);
            task.setOutStream(outStream);
        }

        @Test
        void basicPojo() {
            task.getClasses().add(PlainObject.class);
            task.action();

            verify(outStream).println(outputCaptor.capture());

            JsonStringAssertion.assertThat(outputCaptor.getValue())
                    .doesNotHaveField("required");
        }

        @Test
        void notNullValidator() {
            task.getClasses().add(NotNullObject.class);
            task.action();

            verify(outStream).println(outputCaptor.capture());

            JsonStringAssertion.assertThat(outputCaptor.getValue())
                    .withArrayStringField("required")
                    .containsExactlyInAnyOrder("str");
        }

        @Test
        void enumProperty() {
            class EnumObject {
                private TestEnum testEnum;

                public TestEnum getTestEnum() { return testEnum; }
                public void setTestEnum(TestEnum testEnum) { this.testEnum = testEnum; }
            }

            task.getClasses().add(EnumObject.class);
            task.action();

            verify(outStream).println(outputCaptor.capture());

            JsonStringAssertion.assertThat(outputCaptor.getValue())
                    .extractingNode("properties")
                    .extractingNode("testEnum")
                    .withArrayStringField("enum")
                    .containsExactlyInAnyOrder("EVAL_1", "EVAL_2", "EVAL_3");
        }
    }
}
