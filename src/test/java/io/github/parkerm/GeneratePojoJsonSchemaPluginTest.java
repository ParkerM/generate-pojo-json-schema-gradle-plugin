package io.github.parkerm;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratePojoJsonSchemaPluginTest {

    private Project project;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder().build();
        project.getPlugins().apply("io.github.parkerm.generate-pojo-json-schema");
    }

    @Test
    void pluginRegistersTask() {
        assertThat(project.getTasks().stream())
                .extracting(Task::getName)
                .containsExactlyInAnyOrder("generateJsonSchema");
    }
}
