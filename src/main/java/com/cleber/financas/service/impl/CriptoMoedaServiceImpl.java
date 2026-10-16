package com.cleber.financas.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.cleber.financas.exception.RegraDeNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cleber.financas.api.dto.CriptoMoedaDTO;
import com.cleber.financas.model.entity.CriptoMoeda;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.enums.StatusTransacao;
import com.cleber.financas.model.mapper.CriptoMoedaMapper;
import com.cleber.financas.model.repository.CriptoMoedaRepository;
import com.cleber.financas.service.CriptoMoedaService;
import com.cleber.financas.service.UsuarioService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CriptoMoedaServiceImpl implements CriptoMoedaService{

	private final CriptoMoedaRepository criptoMoedaRepository;
	private final CriptoMoedaMapper criptoMoedaMapper;
	private final UsuarioService usuarioService;

	@Override
	@Transactional
	public CriptoMoedaDTO salvarCriptomoeda(CriptoMoedaDTO criptoMoedaDTO, UUID usuarioAutenticado) {
		/* o dono da criptomoeda e derivado do access token, nao do corpo da requisicao */
		CriptoMoeda entidadeConvertida = criptoMoedaMapper.dtoToEntity(criptoMoedaDTO);

		/* associar o usuario a partir do token autenticado */
		Usuario usuario = usuarioService.obterUsuarioPorId(usuarioAutenticado)
				.orElseThrow(() -> new RegraDeNegocioException("Usuário não encontrado!"));
		entidadeConvertida.setUsuario(usuario);

		CriptoMoeda criptoMoedaSalva = criptoMoedaRepository.save(entidadeConvertida);

		return criptoMoedaMapper.entityToDto(criptoMoedaSalva);
	}

	@Override
	@Transactional
	public CriptoMoeda atualizarCriptomoeda(CriptoMoedaDTO criptoMoedaDTO) {
		Objects.requireNonNull(criptoMoedaDTO.getUuid(), "UUID da criptomoeda não pode ser nulo!");

		CriptoMoeda entidadeExistente = criptoMoedaRepository.findById(criptoMoedaDTO.getUuid())
				.orElseThrow(() -> new RegraDeNegocioException("Criptomoeda não encontrada!"));

		CriptoMoeda entidadeAtualizada = criptoMoedaMapper.dtoToEntity(criptoMoedaDTO);
		entidadeAtualizada.setUuid(entidadeExistente.getUuid());
		entidadeAtualizada.setUsuario(entidadeExistente.getUsuario());

		return criptoMoedaRepository.save(entidadeAtualizada);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CriptoMoeda> buscarCriptomoeda(CriptoMoeda criptoMoedaFiltro) {
		org.springframework.data.domain.Example example = org.springframework.data.domain.Example.of(criptoMoedaFiltro,
				org.springframework.data.domain.ExampleMatcher.matching()
						.withIgnoreCase()
						.withStringMatcher(org.springframework.data.domain.ExampleMatcher.StringMatcher.CONTAINING));
		return criptoMoedaRepository.findAll(example);
	}

	@Override
    @Transactional
	public void deletarCriptoMoeda(CriptoMoedaDTO criptoMoedaDTO) {
		/**Validar se o DTO não é nulo**/
        Objects.requireNonNull(criptoMoedaDTO, "CriptoMoedaDTO não pode ser nulo!");
		// Validar se o UUID da criptomoeda não é nulo
		UUID uuid = Objects.requireNonNull(criptoMoedaDTO.getUuid(), "UUID da criptomoeda não pode ser nulo!");
		// Buscar a criptomoeda pelo UUID e lançar uma exceção se não for encontrada
        CriptoMoeda criptoMoeda = criptoMoedaRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Criptomoeda não encontrada"));
		// excluir a entidade encontrada com o repository
        criptoMoedaRepository.delete(criptoMoeda);

	}

	@Override
	public void atualizarStatus(CriptoMoedaDTO criptoMoedaDTO, StatusTransacao stuStatusTransacao) {
		// TODO Auto-generated method stub

	}

	@Override
	public Optional<CriptoMoeda> obterCriptomoedaPorId(UUID uuid) {
		return criptoMoedaRepository.findById(uuid);
	}

	@Override
	public BigDecimal obterValorCriptomoedaPorUsuario(UUID uuid) {
		// TODO Auto-generated method stub
		return null;
	}
}
