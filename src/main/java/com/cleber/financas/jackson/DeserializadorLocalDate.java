package com.cleber.financas.jackson;

/*ObjectMapper - permite tanto a serialização (converter objetos Java em JSON)
 quanto a desserialização (converter JSON em objetos Java*/

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration
public class DeserializadorLocalDate extends JsonDeserializer<LocalDate> {
    
    private final ObjectMapper objectMapper;
    
    public DeserializadorLocalDate(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @PostConstruct
    public void setup() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(LocalDate.class, this);
        objectMapper.registerModule(simpleModule);
    }
    
    @Override
    public LocalDate deserialize(JsonParser jsonParser, DeserializationContext ctxt)
            throws IOException, JacksonException {
        String data = jsonParser.getText();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return LocalDate.parse(data, formatter);
    }
}


