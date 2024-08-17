package com.cleber.financas.controller.dto;

import com.sun.jdi.PrimitiveValue;
import lombok.Builder;


public class UsuarioDTO {

    private String nome;
    private String email;
    private String senha;

    /*BUILDER*/

    private UsuarioDTO(UsuarioDTO builder){
        this.nome = builder.name;
        this.email = builder.email;
        this.senha = builder.senha;
    }
    public String getNome(){
        return nome;
    }

    public String getEmail(){
        return email;
    }

    public String getSenha(){
        return senha;
    }

    public static class UsuarioDTO{
        private String nome;
        private String email;
        private String senha;
    }

    public UsuarioDTO setNome(String nome){
        this.name = name;
        return this;
    }

    public UsuarioDTO setEmail(String email){
        this.email = email;
        return  this;
    }

    public UsuarioDTO setSenha(String senha){
        this.senha = senha;
        return this
    }
    public UsuarioDTO buil(){
        return new UsuarioDTO(this);
    }

    /*GETTERS AND SETTERS*/

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
