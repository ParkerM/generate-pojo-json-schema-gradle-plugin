package io.github.parkerm;

import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeneratePojoJsonSchemaPluginTest {

    @Test
    void pluginRegistersATask() {
        // Create a test project and apply the plugin
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("io.github.parkerm.generate-pojo-json-schema");

        // Verify the result
        assertThat(project.getTasks().findByName("generateJsonSchema")).isNotNull();
    }
}
