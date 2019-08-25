package io.github.parkerm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class GeneratePojoJsonSchemaTask extends DefaultTask {

    private List<Class<?>> classes;
    private boolean prettyPrint;
    private ObjectMapper mapper;
    private PrintStream outStream;

    public GeneratePojoJsonSchemaTask() {
        classes = new ArrayList<>();
        mapper = new ObjectMapper();
        prettyPrint = true;
        outStream = System.out;
    }

    @TaskAction
    public void action() {
        classes.forEach(clazz -> {
            try {
                outStream.println(getJsonSchema(clazz));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public List<Class<?>> getClasses() {
        return classes;
    }

    public void setClasses(List<Class<?>> classes) {
        this.classes = classes;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public PrintStream getOutStream() {
        return outStream;
    }

    public void setOutStream(PrintStream outStream) {
        this.outStream = outStream;
    }

    private <T> String getJsonSchema(Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
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
}
