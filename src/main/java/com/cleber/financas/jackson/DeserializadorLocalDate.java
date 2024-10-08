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
import com.cleber.financas.exception.ErroDataException;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;

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

        String dma = jsonParser.getText();
        String[] parts = dma.split("-");

        if (parts.length == 3) {
            String dayPart = parts[0];
            String monthPart = parts[1];
            String yearPart = parts[2];

            try {
                int year = Integer.parseInt(yearPart.length() == 2 ? "20" + yearPart : yearPart);
                int month = Integer.parseInt(monthPart);
                int day = Integer.parseInt(dayPart);

                if (year < 1900 || year > 2100) {
                    throw new ErroDataException("O ano [" + yearPart + "] não é válido");
                }

                // Verifica se o dia e o mês são válidos
                LocalDate date = LocalDate.of(year, month, day);
                return date;

            } catch (NumberFormatException | DateTimeException e) {
                throw new ErroDataException("A data [" + dma + "] não é válida");
            }
        } else {
            throw new ErroDataException("A data [" + dma + "] não é válida");
        }
    }
}



