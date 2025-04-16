package com.cleber.financas.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

/*para estruturar a mensagem de erro no front - login com email inexistente*/

public class ErroResponse {
    private String mensagem;
    
  /*
    public ErroResponse(String message) {
    }
  public ErroResponse(String mensagem) {
        this.mensagem = mensagem;
    }
    public String getMensagem() {
        return mensagem;
    }
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }*/
    
}
