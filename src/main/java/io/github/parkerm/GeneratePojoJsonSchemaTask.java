package io.github.parkerm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneratePojoJsonSchemaTask extends DefaultTask {

    private List<Class<?>> classes;
    private boolean prettyPrint;
    private ObjectMapper mapper;
    private PrintStream outStream;
    private boolean redirectToOutStream;
    private File generatedFileDir;

    public GeneratePojoJsonSchemaTask() {
        classes = new ArrayList<>();
        mapper = new ObjectMapper();
        prettyPrint = true;
        outStream = System.out;
        redirectToOutStream = false;
        generatedFileDir = Paths.get("build", "schema", "json").toFile();
    }

    @TaskAction
    public void action() {
        Map<String, String> schemaMap = new HashMap<>();
        classes.forEach(clazz -> {
            try {
                String jsonSchema = getJsonSchema(clazz);
                String fileName = clazz.getSimpleName() + ".schema.json";
                schemaMap.put(fileName, jsonSchema);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        if (redirectToOutStream) {
            schemaMap.forEach((fileName, schema) -> outStream.println(schema));
        } else {
            Path schemaDir;
            try {
                schemaDir = Files.createDirectories(generatedFileDir.toPath());
            } catch (IOException e) {
                getState().setOutcome(new RuntimeException(e));
                return;
            }

            schemaMap.forEach((fileName, schema) -> {
                try {
                    writeString(new File(getProject().file(schemaDir), fileName), schema);
                } catch (IOException e) {
                    getState().setOutcome(new RuntimeException(e));
                }
            });
        }
    }

    @Input
    public List<Class<?>> getClasses() {
        return classes;
    }

    public void setClasses(List<Class<?>> classes) {
        this.classes = classes;
    }

    @Input
    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public PrintStream getOutStream() {
        return outStream;
    }

    public void setOutStream(PrintStream outStream) {
        this.outStream = outStream;
    }

    @Input
    public boolean isRedirectToOutStream() {
        return redirectToOutStream;
    }

    public void setRedirectToOutStream(boolean redirectToOutStream) {
        this.redirectToOutStream = redirectToOutStream;
    }

    @OutputDirectory
    public File getGeneratedFileDir() {
        return generatedFileDir;
    }

    public void setGeneratedFileDir(File generatedFileDir) {
        this.generatedFileDir = generatedFileDir;
    }

    private <T> String getJsonSchema(Class<T> clazz) throws IOException {
        JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(mapper);

        JsonNode jsonSchema = jsonSchemaGenerator.generateJsonSchema(clazz);

        ObjectWriter writer;
        if (prettyPrint) {
            writer = mapper.writerWithDefaultPrettyPrinter();
        } else {
            writer = mapper.writer();
        }
        return writer.writeValueAsString(jsonSchema);
    }

    private void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }
}
