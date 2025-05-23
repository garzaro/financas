package com.cleber.financas.service.impl;

import com.cleber.financas.config.PasswordEncoderConfig;
import com.cleber.financas.exception.ErroDeAutenticacao;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.service.UsuarioService;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.Encoder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/* caso tenhamos que usa o bcrypt private PasswordEncoder passwordEncoder;*/
//@NoArgsConstructor
@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    PasswordEncoderConfig passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        super();
        this.usuarioRepository = usuarioRepository;
    }
    /* lista de emails permitidos */
    private static final List<String> dominiosEmailPermitidos = List.of(
            "gmail.com", "outlook.com", "edu.br", "gov.br"
    );

    /*login, validação e autenticação*/
    @Override
    public Usuario autenticar(String email, String senha) {
        /*login, validando login*/
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        /*verificar a existencia de usuario na base de dados*/
        if (!usuario.isPresent()) {
            throw new ErroDeAutenticacao("Verifique seu email e tente novamente.");
        }
        if (!usuario.get().getSenha().equals(senha)) {
            throw new ErroDeAutenticacao("Senha incorreta. Tente novamente ou clique em \"Esqueceu a senha?\" para escolher outra.");
        }
        return usuario.get();
    }
    
    @Override
    @Transactional
    public Usuario salvarUsuario(Usuario usuario) {
        /*deve validar o email e o cpf, verificar se existe*/
        validarUsuario(usuario);
        /*se nao existir email e nem cpf, salva a instancia com o hash da senha*/
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario atualizarUsuario(Usuario usuario) {
        Objects.requireNonNull(usuario.getId());
        validarUsuario(usuario);
        return usuarioRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> obterUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> obterUsuarioPorCpf(String cpf) {
        return  usuarioRepository.findByCpf(cpf);
    }

    /*VALIDAÇÃO*/
    @Override
    public void validarUsuario(Usuario usuario) {
        /* preencher campos */
        if (usuario.getNome() == null || usuario.getNome().trim().equals("")) {
            throw new RegraDeNegocioException("O nome completo é obrigatório");
        }
        if (usuario.getCpf() == null || usuario.getCpf().trim().equals("")) {
            throw new RegraDeNegocioException("O CPF é obrigatório");
        }
        /*validação manual*/
        if (!Pattern.matches("^\\d{3}\\.\\d{3}\\.\\d{3}\\-\\d{2}$", usuario.getCpf())) {
            throw new RegraDeNegocioException("O CPF deve seguir o padrao 000.000.000-00");
        }
        if (usuario.getUsuario() == null || usuario.getUsuario().trim().equals("")) {
            throw new RegraDeNegocioException("O nome de usuário é obrigatório");
        }
        if (usuario.getEmail() == null || usuario.getEmail().trim().equals("")) {
            throw new RegraDeNegocioException("O email é obrigatório");
        }
        if (usuario.getEmail() == null || usuario.getEmail().trim().equals("")) {
            throw new RegraDeNegocioException("O email deve seguir o padrao email@seudominio.com");
        }
        String emailPermitido = usuario.getEmail().substring(usuario.getEmail().lastIndexOf("@") + 1);
        if (!dominiosEmailPermitidos.contains(emailPermitido)) { /*garante que o dominio extraido esteja na lista*/
            throw new RegraDeNegocioException("O email para cadastro deve constar da lista a seguir: " + dominiosEmailPermitidos);
        }
        if (usuario.getSenha() == null || usuario.getSenha().trim().equals("")) {
            throw new RegraDeNegocioException("informe a senha.");
        }
        /*validação manual
        if (!Pattern.matches("^[\\w-\\.]+@[\\w-\\.]+\\.[a-z]{2,}$", usuario.getEmail())) {
            throw new ErroValidacaoException("O email deve seguir o padrao email@seudominio.com (br).");
        }*/
        /*garante a nao duplicidade ao criar usuario*/
        validarDuplicidadeEmailCpf(usuario.getEmail(), usuario.getCpf());
    }

    public void validarDuplicidadeEmailCpf(String email, String cpf){
        validarEmail(email);
        validarCpf(cpf);
    }

    @Override
    public void validarEmail(String email) {
        /*ver se o email existe*/
        boolean existeUsuarioComEsseEmail = usuarioRepository.existsByEmail(email);
        if (existeUsuarioComEsseEmail) {
            throw new RegraDeNegocioException("Já existe um usuário com esse email");
        }
    }

    @Override
    public void validarCpf(String cpf) {
        /*ver se o cpf existe*/
        boolean existeUsuarioComCpf = usuarioRepository.existsByCpf(cpf);

        if (existeUsuarioComCpf) {
            throw new RegraDeNegocioException("Já existe um usuário com esse CPF");
        }
    }

    /*public void senhaCriptografada(Usuario usuario) {
    	String pegarSenha = usuario.getSenha();
    	String criptografar = encoder.encode(pegarSenha);
    	usuario.setSenha(criptografar);
    }*/

}
