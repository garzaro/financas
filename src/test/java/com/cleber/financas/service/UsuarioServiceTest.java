package com.cleber.financas.service;

import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import org.assertj.core.api.Assertions;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Test(expected = Test.None.class)
    public void deveValidarEmail(){
        /*cenario*/
        usuarioRepository.deleteAll();
        /*ação, sem verificação, só olha se existe o email*/
        usuarioService
                .validarEmailAndCpf("cleber@gmail.com","123456789-00");
    }
    @Test(expected = RegraDeNegocioException.class)
    public void deveLancarErroAoValidarQuandoExistirEmaiLCadastrado(){
        /*cenario*/
        Usuario cadastrarEmail = Usuario.builder()
                .usuario("garzaro74")
                .email("cleber@gmail.com")
                .build();
        usuarioRepository.save(cadastrarEmail);
        /*ação*/
        usuarioService
                .validarEmailAndCpf("cleber@gmail.com", "123456789-00");
    }
    @Test(expected = Test.None.class)
    public void deveAutenticarUmUsuarioComSucesso(){
        /*cenario*/
        Usuario usuario = Usuario.builder()
                .email("clebergarzaro@gmail.com")
                .senha("senha123456")
                .build();
        /*ação*/
        Usuario salvarUsuario = usuarioRepository.save(usuario);

        // Verificar se o método autenticarUsuario retorna o usuário autenticado corretamente
        Usuario usuarioAutenticado = usuarioService.autenticar(salvarUsuario.getEmail(), salvarUsuario.getSenha());

        /*verificação*/
        Assertions.assertThat(usuarioAutenticado).isNotNull();
        Assertions.assertThat(usuarioAutenticado.getEmail()).isEqualTo(salvarUsuario.getEmail());
        Assertions.assertThat(usuarioAutenticado.getSenha()).isEqualTo(salvarUsuario.getSenha());
    }
    @Test
    public void dadoUmaSenhaBrutaFornecida_QuandoCodificadaComArgon2_EntãoCorrespondeSenhaCodificada() {
        String senhaBruta = "Cleber";
        /*iteração, 32bits, uma thread, 600MB ram, custo te tempo 10 -configuração para o argon2*/
        Argon2PasswordEncoder arg2SpringSecurity = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);
        String springBouncyHash = arg2SpringSecurity.encode(senhaBruta);
        System.out.println("A senha hasheada é " + springBouncyHash);

        assertTrue(arg2SpringSecurity.matches(senhaBruta, springBouncyHash));
    }
    /*ver explicação no Baeeldung*/
    @Test
    public void givenSenhaBrutaESalt_QuandoAlgoritmoArgon2Eusado_EntaoHashEstaCorreto() {
        byte[] salt = generateSalt16Byte();
        String senha = "Baeldung";

        int iteracao = 2;
        int limiteMemoria = 66536;
        int tamanhoHash = 32;
        int parallelismo = 1;

        Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(iteracao)
                .withMemoryAsKB(limiteMemoria)
                .withParallelism(parallelismo)
                .withSalt(salt);

        Argon2BytesGenerator generate = new Argon2BytesGenerator();
        generate.init(builder.build());
        byte[] result = new byte[tamanhoHash];
        generate.generateBytes(senha.getBytes(StandardCharsets.UTF_8), result, 0, result.length);

        Argon2BytesGenerator verificador = new Argon2BytesGenerator();
        verificador.init(builder.build());
        byte[] testHash = new byte[tamanhoHash];
        verificador.generateBytes(senha.getBytes(StandardCharsets.UTF_8), testHash, 0, testHash.length);
        System.out.println("A senha verificada " + result + " e " + testHash);
        assertTrue(Arrays.equals(result, testHash));
    }
    private byte[] generateSalt16Byte() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);

        return salt;
    }
}
