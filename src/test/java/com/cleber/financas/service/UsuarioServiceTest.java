package com.cleber.financas.service;

import com.cleber.financas.model.repository.UsuarioRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    UsuarioService usuarioService;

    @Test(expected = Test.None.class)
    public void deveValidarEmail(){
        /*cenario*/
        usuarioRepository.deleteAll();
        /*ação*/
        usuarioService.validarEmailNaBaseDedados("cleber@gmail.com");
    }




}
