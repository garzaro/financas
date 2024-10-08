package com.cleber.financas.jackson;

/*O SimpleModule é uma classe do Jackson que permite registrar serializadores e desserializadores personalizados
 deserializadorPersonalizado - nome do metodo de acordo com o que a classe SimpleMode faz*/

import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.exception.ValorInvalidoException;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
@RequiredArgsConstructor
@Configuration
public class DeserializadorBigDecimalCustomizado extends JsonDeserializer<BigDecimal> {

    private final ObjectMapper objectMapper;

    /*O desserializador personalizado CustomBigDecimalDeserializer será usado automaticamente.*/
    @PostConstruct
    public void setUp(){

        SimpleModule deserializadorPersonalizado = new SimpleModule();
        deserializadorPersonalizado.addDeserializer(BigDecimal.class,
                new DeserializadorBigDecimalCustomizado(objectMapper));

        objectMapper.registerModule(deserializadorPersonalizado);
    }

    @Override
    public BigDecimal deserialize(JsonParser jsonParser, DeserializationContext ctxt)
            throws IOException {
        try {
            String value = jsonParser.getText().replace(",", ".");
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new ValorInvalidoException("O valor [" + jsonParser.getText() + "] não é válido");
        }
    }






    /*@Override
    public BigDecimal deserialize(JsonParser jsonParser, DeserializationContext ctxt)
            throws IOException, JacksonException {
        String valorDigitado = jsonParser.getText();
        valorDigitado = valorDigitado.trim().replace(",", ".");
        DecimalFormatSymbols simboloDigitado = new DecimalFormatSymbols();
        simboloDigitado.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("#.##", simboloDigitado);
        decimalFormat.setParseBigDecimal(true);

        try {
            return (BigDecimal) decimalFormat.parse(valorDigitado);

        } catch (ParseException exception) {
            throw new ValorInvalidoException("O valor " + "[" + valorDigitado + "]" + " não válido");
        }
    }*/
}
