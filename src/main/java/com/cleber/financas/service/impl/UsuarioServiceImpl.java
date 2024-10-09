package com.cleber.financas.service.impl;

import com.cleber.financas.exception.ErroDeAutenticacao;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.repository.UsuarioRepository;
import com.cleber.financas.service.UsuarioService;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@NoArgsConstructor
@Service
public class UsuarioServiceImpl implements UsuarioService {
    @Autowired
    UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        super();
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario autenticarUsuario(String email, String senha) {
        /*login, validando login*/
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        /*verificar a existencia de usuario na base de dados*/
        if (!usuario.isPresent()) {
            throw new ErroDeAutenticacao("Usuario não encontrado pelo email informado");
        }
        if (!usuario.get().getSenha().equals(senha)) {
            throw new ErroDeAutenticacao("Senha inválida");
        }
        return usuario.get();
    }

    @Override
    @Transactional
    public Usuario persistirUsuarioNabaseDeDados(Usuario usuario) {

        /*deve validar o email, verificar se existe. (metodo do Service)*/
        validarEmailAndCadastroPessoaFisicaNaBaseDedados(
                usuario.getEmail(), usuario.getCadastroPessoaFisica());

        /*se nao existir email, salva a instancia*/
        return usuarioRepository.save(usuario);
    }
    AINDA NAO ESTA VALIDANDO O CPF

    @Override
    public void validarEmailAndCadastroPessoaFisicaNaBaseDedados(String email, String cadastroPessoaFisica) {
        /*ver se o email existe*/
        boolean verificarSeOEmaiLECadastroPessoaFisicaExistemNaBaseDeDados = usuarioRepository
                .existsByEmailAndCadastroPessoaFisica(email, cadastroPessoaFisica);

        if (verificarSeOEmaiLECadastroPessoaFisicaExistemNaBaseDeDados) {
            throw new RegraDeNegocioException("Já existe um usuário com esse email.");
        }
        if (verificarSeOEmaiLECadastroPessoaFisicaExistemNaBaseDeDados) {
            throw new RegraDeNegocioException("Já existe um usuário com esse CPF");
        }
    }

    /*@Override
    public void validarCadastroPessoaFisica(String cadastroPessoaFisica) {
        /*ver se existe o cpf
        boolean verificarCpf = usuarioRepository.
                existsByCadastroPessoaFisica(cadastroPessoaFisica);
        if (verificarCpf){
            throw new RegraDeNegocioException("Já existe um usuário com esse CPF");
        }
    }*/

    @Override
    public Optional<Usuario> obterUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }
}
