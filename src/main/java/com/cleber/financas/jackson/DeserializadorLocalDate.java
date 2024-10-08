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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
            String parteDoAno = parts[2];

            try {
                int ano = Integer.parseInt(parteDoAno.length() == 2 ? "20" + parteDoAno : parteDoAno);
                if (ano < 2024 || ano > 2100) {
                    throw new ErroDataException("O ANO [" + parteDoAno + "] não é valido");
                }
                /*// Fixar o dia e mês em 1, pois só estamos validando o ano*/
                return LocalDate.of(ano, 1, 1);
            } catch (NumberFormatException e) {
                throw new ErroDataException("O ANO [" + parteDoAno + "] nao é valisdo");
            }

        } else {
            throw new ErroDataException("A data [" + dma + "] não e valida, o ano deve conter 4 dígitos");
        }
    }
}


