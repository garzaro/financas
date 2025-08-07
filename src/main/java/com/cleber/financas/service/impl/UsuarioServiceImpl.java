package com.cleber.financas.service.impl;

import com.cleber.financas.config.PasswordEncoderConfig;
import com.cleber.financas.exception.ErroDeAutenticacao;
import com.cleber.financas.exception.ErroValidacaoException;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.service.UsuarioService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        super();
        this.usuarioRepository = usuarioRepository;
    }
    /* lista de emails permitidos */
    private static final List<String> dominiosEmailPermitidos = List.of(
            "gmail.com", "edu.br", "gov.br"
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

    /*VALIDAÇÃO*/
    @Override
    public void validarUsuario(Usuario usuario) {
        /*preencher campos*/
    	//System.out.println("Nome do usuário: " + usuario.getNome());
        if (usuario.getNome() == null || usuario.getNome().trim().equals("")) {
            throw new ErroValidacaoException("O nome completo é obrigatório");
        }
        if (!Pattern.matches("^[a-zA-Z\\s]+$", usuario.getNome())) {
            throw new ErroValidacaoException("O nome completo deve conter apenas letras e espaços");
        }

        if (usuario.getCpf() == null || usuario.getCpf().trim().equals("")) {
            throw new ErroValidacaoException("O CPF é obrigatório");
        }
        /*validação manual*/
        if (!Pattern.matches("^\\d{3}\\.\\d{3}\\.\\d{3}\\-\\d{2}$", usuario.getCpf())) {
            throw new ErroValidacaoException("O CPF deve seguir o padrao 000.000.000-00");
        }
        if (usuario.getUsuario() == null || usuario.getUsuario().trim().equals("")) {
            throw new ErroValidacaoException("O nome de usuário é obrigatório");
        }
        if (usuario.getEmail() == null || usuario.getEmail().trim().equals("")) {
            throw new ErroValidacaoException("O email é obrigatório");
        }
        if (usuario.getEmail() == null || usuario.getEmail().trim().equals("")) {
            throw new ErroValidacaoException("O email deve seguir o padrao email@seudominio.com");
        }
        /*validação manual*/
        if (!Pattern.matches("^[\\w-\\.]+@[\\w-\\.]+\\.[a-z]{2,}$", usuario.getEmail())) {
            throw new ErroValidacaoException("O email deve seguir o padrao email@seudominio.com (br).");
        }
        String emailPermitido = usuario.getEmail().substring(usuario.getEmail().lastIndexOf("@") + 1);
        if (!dominiosEmailPermitidos.contains(emailPermitido)) { /*garante que o dominio extraido esteja na lista*/
            throw new ErroValidacaoException("O email para cadastro deve constar da lista a seguir: " + dominiosEmailPermitidos);
        }
        if (usuario.getSenha() == null || usuario.getSenha().trim().equals("")) {
            throw new ErroValidacaoException("informe a senha.");
        }
        /*não deixa duplica email e cpf ao criar  usuario*/
        validarEmailCpf(usuario.getEmail(), usuario.getCpf());
    }
        
    public void validarEmailCpf(String email, String cpf) {
        /*ver se o email existe*/
        boolean existeUsuarioComEsseEmail = usuarioRepository.existsByEmail(email);
        if (existeUsuarioComEsseEmail) {
            throw new RegraDeNegocioException("Esse email já está em uso");
        }
        
        boolean existeUsuarioComEsseCpf = usuarioRepository.existsByCpf(cpf);
        if (existeUsuarioComEsseCpf) {
        	throw new RegraDeNegocioException("Esse CPF já está em uso");
        }
    }
    
    @Override
    public Optional<Usuario> obterUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Optional<Usuario> obterUsuarioPorCpf(String cpf) {
        return  usuarioRepository.findByCpf(cpf);
    }

    /*public void senhaCriptografada(Usuario usuario) {
    	String pegarSenha = usuario.getSenha();
    	String criptografar = encoder.encode(pegarSenha);
    	usuario.setSenha(criptografar);
    }*/

}
