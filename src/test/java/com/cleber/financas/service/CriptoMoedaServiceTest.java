package com.cleber.financas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.cleber.financas.api.dto.CriptoMoedaDTO;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.CriptoMoeda;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.enums.StatusTransacao;
import com.cleber.financas.model.enums.TipoTransacao;
import com.cleber.financas.model.repository.CriptoMoedaRepository;
import com.cleber.financas.model.repository.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Testes de integração para a camada de Service relacionada a CriptoMoeda.
 *
 * "PENSAR PRIMEIRO, IMPLEMENTAR DEPOIS". Clairton
 *
 */
@SpringBootTest                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
@AutoConfigureTestEntityManager
public class CriptoMoedaServiceTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CriptoMoedaService criptoMoedaService; // o spring cuida implementação - @Service                                                                                                                                                                                                                                                                                                                           

    @Autowired
    private CriptoMoedaRepository criptoMoedaRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    public void setup() {
        /**
         * O metodo criarUsuario agora salva o usuário, então não precisamos
         * chamá-lo aqui a menos que queiramos um usuário disponível para todos
         * os testes. Mas os testes já chamam criarCriptomoeda que por sua vez
         * chama criarUsuario.
         **/
    }

    @Test
    @DisplayName("Deve criar um usuario")
    @Transactional
    @Rollback(false)
    void deveCriarUmUsuarioEGarantirQueElePossuiUmId() {
        /**cenario**/
        Usuario usuario = Usuario.builder()
        		.id(UUID.randomUUID())
                .nomeCompleto("Usuário Teste")
                .cpf("12345678900")
                .nomeUsuario("usuario_teste" + Instant.now().toEpochMilli())
                .email("usuario_teste" + Instant.now().toEpochMilli() + "@gmail.com")
                .senha("Senha@123")
                .dataCadastro(Instant.now())
                .build();
        /**execução**/
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        /**verificação**/
        assertThat(usuarioSalvo.getId()).isNotNull();
    }

    /**Objetivo 1: salvar múltiplas entidades e verificar contagem total com findAll()**/
    @Test
    @DisplayName("deve retornar contagem correta ao salvar múltiplas entidades")
    @Transactional
    @Rollback(false)
    void deveRetornarContagemCorretaQuandoSalvarMultiplasMoedas() {
        /**cenario**/
        CriptoMoedaDTO sol = criarCriptomoeda();
        sol.setAtivo("SOL");
        sol.setTipoTransacao(TipoTransacao.COMPRA);

        CriptoMoedaDTO bnb = criarCriptomoeda();
        bnb.setAtivo("BNB");
        bnb.setTipoTransacao(TipoTransacao.COMPRA);
        bnb.setStatusTransacao(StatusTransacao.ABERTA);

        CriptoMoedaDTO xrp = criarCriptomoeda();
        xrp.setAtivo("XRP");
        xrp.setTipoTransacao(TipoTransacao.COMPRA);

        /**ação**/
        criptoMoedaService.salvarCriptomoeda(sol);
        criptoMoedaService.salvarCriptomoeda(bnb);
        criptoMoedaService.salvarCriptomoeda(xrp);

        /**verificação**/
        List<CriptoMoeda> todas = criptoMoedaRepository.findAll();
        assertThat(todas).isNotNull();
        assertThat(todas).hasSize(3);
    }

    /**Objetivo 2: salvar entidade válida e verificar geração de ID**/
    @Test
    @DisplayName("deve gerar ID automaticamente quando salvar entidade válida")
    @Transactional
    @Rollback(false)
    void deveGerarIdAoSalvarEntidadeValidaQuandoSalvar() {
        /**cenario**/
        CriptoMoedaDTO dto = criarCriptomoeda(); // ja cria o usuario

        /**ação**/
        CriptoMoedaDTO salvar = criptoMoedaService.salvarCriptomoeda(dto);

        /**verificação**/
        assertThat(salvar).isNotNull();
        assertThat(salvar.getUuid()).isNotNull();
    }

    /**Objetivo 3: salvar e recuperar por ID com todos os campos corretos**/
    @Test
    @DisplayName("deve persistir e recuperar por ID com todos os campos corretos")
    @Transactional
    @Rollback(false)
    void devePersistirERetornarPorIdQuandoSalvar() {
        /**cenario**/
        CriptoMoedaDTO dto = criarCriptomoeda();

        /**ação**/
        CriptoMoedaDTO salvar = criptoMoedaService.salvarCriptomoeda(dto);

        /**verificação**/
        CriptoMoeda moedaEncontrada = criptoMoedaRepository.findById(salvar.getUsuario()).orElseThrow();
        assertThat(moedaEncontrada).isNotNull();
        assertThat(moedaEncontrada.getCriptomoeda()).isEqualTo(dto.getAtivo());
        assertThat(moedaEncontrada.getValorAtual()).isEqualByComparingTo(dto.getValorAtualAtivo());
        assertThat(moedaEncontrada.getValorInvestido()).isEqualByComparingTo(dto.getValorInvestido());
        assertThat(moedaEncontrada.getMes()).isEqualTo(dto.getMes());
    }

    /**Objetivo 4: verificar que dataAtualizacao é preenchida automaticamente ao salvar**/
    @Test
    @DisplayName("deve preencher dataAtualizacao automaticamente ao salvar - JA SALVA MAS EU QUIS FAZER")
    void devePreencherDataAtualizacaoQuandoSalvar() {
        /**cenario**/
        CriptoMoedaDTO dto = criarCriptomoeda();

        /**ação**/
        CriptoMoedaDTO salvo = criptoMoedaService.salvarCriptomoeda(dto);

        /**verificação**/
        assertThat(salvo).isNotNull();
        assertThat(salvo.getDataAtualizacao()).isNotNull();
        assertThat(salvo.getDataAtualizacao()).isInstanceOf(Instant.class);
    }

    /**Objetivo 5: deve lancar uma excessao quando tentar salvar criptomoeda com valor nulo**/
    @Test
    @DisplayName("deve lançar exceção quando tentar salvar com criptomoeda nula")
    void deveLancarExcecaoQuandoSalvarComCriptomoedaNula() {
        /**cenario**/
        CriptoMoedaDTO dto = criarCriptomoeda();
        dto.setAtivo(null);

        /**ação**/ /**verificação**/
        assertThatThrownBy(() -> {
            criptoMoedaService.salvarCriptomoeda(dto);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    /**Objetivo 6: deve lancar uma excessao quando tentar salvar usuario com valor nulo**/
    @Test
    @DisplayName("deve lançar exceção quando tentar salvar com id de usuário nulo")
    void deveLancarExcecaoQuandoSalvarComUsuarioNulo() {
        /**cenario**/
        CriptoMoedaDTO dto = criarCriptomoeda();
        dto.setUsuario(null);

        /**ação**/ /**verificação**/
        assertThatThrownBy(() -> {
            criptoMoedaService.salvarCriptomoeda(dto);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    /**Objetivo 7: lançar exceção ao salvar com valorAtual nulo**/
    @Test
    @DisplayName("deve lançar exceção quando tentar salvar com valorAtual nulo")
    @Transactional
    void deveLancarExcecaoQuandoSalvarComValorAtualNulo() {
        /**cenario**/
        CriptoMoedaDTO dto = criarCriptomoeda();
        dto.setValorAtualAtivo(null);

        /**ação**/ /**verificação**/
        assertThatThrownBy(() -> {
            criptoMoedaService.salvarCriptomoeda(dto);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    /**Objetivo 8: buscar por criptomoeda existente e retornar entidade correta**/
    @Test
    @DisplayName("deve retornar entidade correta ao buscar por criptomoeda existente")
    @Transactional
    @Rollback(false)
    void deveRetornarEntidadeAoBuscarPorCriptomoedaExistente() {
        /**cenario**/
        CriptoMoedaDTO dto = criarCriptomoeda();
        dto.setAtivo("ETH");
        CriptoMoedaDTO salvo = criptoMoedaService.salvarCriptomoeda(dto);

        /**ação**/
        var opt = criptoMoedaRepository.findByCriptomoeda("ETH");

        /**verificação**/
        assertThat(opt).isPresent();
        assertThat(opt.get().getUuid()).isEqualTo(salvo.getUuid());
    }

    /**Objetivo 9: buscar por criptomoeda inexistente e retornar Optional.empty()**/
    @Test
    @DisplayName("deve retornar Optional.empty ao buscar por criptomoeda inexistente")
    void deveRetornarEmptyQuandoBuscarPorCriptomoedaInexistente() {
        /**cenario**/
        // nenhum dado salvo

        /**ação**/
        var optional = criptoMoedaRepository.findByCriptomoeda("MOEDA_INEXISTENTE");

        /**verificação**/
        assertThat(optional).isEmpty();
    }

    /**Objetivo 10: buscar por ID existente e retornar entidade correta**/
    @Test
    @DisplayName("deve retornar entidade correta ao buscar por ID existente")
    void deveRetornarEntidadeAoBuscarPorIdExistente() {
        /**cenario**/
        CriptoMoedaDTO dto = criarCriptomoeda();
        CriptoMoedaDTO salva = criptoMoedaService.salvarCriptomoeda(dto);

        /**ação**/
        var optional = criptoMoedaRepository.findById(salva.getUuid());

        /**verificação**/
        assertThat(optional).isPresent();
        assertThat(optional.get().getCriptomoeda()).isEqualTo(dto.getAtivo());
    }

    /**Objetivo 11: buscar por ID inexistente e retornar Optional.empty()**/
    @Test
    @DisplayName("deve retornar Optional.empty ao buscar por ID inexistente")
    void deveRetornarEmptyQuandoBuscarPorIdInexistente() {
        /**cenario**/
        //nehum dado salvo
        /**ação**/
        var optional = criptoMoedaRepository.findById(java.util.UUID.randomUUID());

        /**verificação**/
        assertThat(optional).isEmpty();
    }

    /**Objetivo 12: retornar todos ao objetos marcados com enums específicos**/
    @Test
    @DisplayName("deve retornar apenas objetos do StatusTransacao/TipoTransacao especificados")
    @Transactional
    @Rollback(false)
    void deveRetornarEntidadesPorEnumsQuandoBuscar() {
        /**cenario**/
        CriptoMoedaDTO criptomoedaA = criarCriptomoeda();
        criptomoedaA.setAtivo("ETH");
        criptomoedaA.setStatusTransacao(StatusTransacao.ANALISAR);

        CriptoMoedaDTO criptomoedaB = criarCriptomoeda();
        criptomoedaB.setAtivo("SOL");
        criptomoedaB.setTipoTransacao(TipoTransacao.COMPRA);

        criptoMoedaService.salvarCriptomoeda(criptomoedaA);
        criptoMoedaService.salvarCriptomoeda(criptomoedaB);

        /**ação**/
        List<CriptoMoeda> analisar = criptoMoedaRepository.findByStatusTransacao(StatusTransacao.ANALISAR);
        List<CriptoMoeda> compra = criptoMoedaRepository.findByTipoTransacao(TipoTransacao.COMPRA);

        /**verificação**/
        assertThat(analisar).isNotNull();
        assertThat(analisar).anyMatch(criptoMoeda -> criptoMoeda.getCriptomoeda().equals("ETH"));
        
        assertThat(compra).isNotNull();
        assertThat(compra).hasSize(1);
        assertThat(compra.get(0).getCriptomoeda()).isEqualTo("SOL");
    }

    /**Objetivo 13: existsByCriptomoeda retorna true para existente**/
    @Test
    @DisplayName("deve retornar true para criptomoeda existente usando findByCriptomoeda como verificação")
    void deveRetornarTrueQuandoCriptomoedaExistir() {
        /**cenario**/
        CriptoMoedaDTO dto = criarCriptomoeda();
        dto.setAtivo("EXISTE");
        criptoMoedaService.salvarCriptomoeda(dto);

        /**ação**/
        boolean existe = criptoMoedaRepository.findByCriptomoeda("EXISTE").isPresent();

        /**verificação**/
        assertThat(existe).isTrue();
    }

    /**Objetivo 14: existsByCriptomoeda retorna false para inexistente**/
    @Test
    @DisplayName("deve retornar false para criptomoeda inexistente usando findByCriptomoeda como verificação")
    void deveRetornarFalseQuandoCriptomoedaNaoExistir() {
        /**cenario**/
        // nada foi salvo
        /**ação**/
        boolean existe = criptoMoedaRepository.findByCriptomoeda("NAO_EXISTE").isPresent();

        /**verificação**/
        assertThat(existe).isFalse();
    }

    /**Objetivo 15: atualizar preço do objeto existente e persistir corretamente**/
    @Test
    @DisplayName("deve atualizar preço e persistir corretamente quando o objeto existe")
    @Transactional
    @Rollback(false)
    void deveAtualizarPrecoQuandoObjetoExistir() {
        /**cenario**/
        CriptoMoedaDTO dto = criarCriptomoeda();
        CriptoMoedaDTO salvar = criptoMoedaService.salvarCriptomoeda(dto);

        /**ação**/
        CriptoMoedaDTO dtoParaAtualizar = salvar;
        dtoParaAtualizar.setValorAtualAtivo(BigDecimal.valueOf(5555));
        criptoMoedaService.atualizarCriptomoeda(dtoParaAtualizar);

        /**verificação**/
        CriptoMoeda atualizada = criptoMoedaService.obterCriptomoedaPorId(salvar.getUuid()).orElseThrow(); // buscar depois da atualização
        assertThat(atualizada.getValorAtual()).isEqualByComparingTo(BigDecimal.valueOf(5555));
    }

    /**Objetivo 16: atualizar tipoTransacao e verificar persistência**/
    @Test
    @DisplayName("deve atualizar tipoTransacao e persistir corretamente quando objeto existe")
    @Transactional
    @Rollback(false)
    void deveAtualizarTipoTransacaoQuandoObjetoExistir() {
        /**cenario**/
        CriptoMoedaDTO dto = criarCriptomoeda();
        CriptoMoedaDTO salvar = criptoMoedaService.salvarCriptomoeda(dto);

        /**ação**/
        CriptoMoedaDTO dtoParaAtualizar = salvar;
        dtoParaAtualizar.setTipoTransacao(TipoTransacao.COMPRA);
        criptoMoedaService.atualizarCriptomoeda(dtoParaAtualizar);

        /**verificação**/
        CriptoMoeda atualizada = criptoMoedaService.obterCriptomoedaPorId(salvar.getUuid()).orElseThrow(); // busca depois da atualização
        assertThat(atualizada.getTipoTransacao()).isEqualTo(TipoTransacao.COMPRA);
    }

    /**Objetivo 17: lançar exceção ao tentar atualizar objeto com ID inexistente**/
    @Test
    @DisplayName("deve lançar exceção ao tentar atualizar objeto com ID inexistente")
    @Transactional
    void deveLancarExcecaoQuandoAtualizarIdInexistente() {
        /**cenario**/
         CriptoMoedaDTO dto = criarCriptomoeda();
         dto.setUuid(UUID.randomUUID());
        /**ação**/ /**verificação**/
        assertThatThrownBy(() -> criptoMoedaService.atualizarCriptomoeda(dto))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    /**Objetivo 18: deletar por ID existente e verificar ausência no banco**/
    @Test
    @DisplayName("deve deletar por ID existente e verificar ausência")
    @Transactional
    @Rollback(false)
    void deveDeletarPorIdQuandoExistir() {
        /**cenario**/
        CriptoMoedaDTO dto = criarCriptomoeda();
        CriptoMoedaDTO salvo = criptoMoedaService.salvarCriptomoeda(dto);

        /**ação**/
        CriptoMoedaDTO dtoParaDeletar = salvo;
        criptoMoedaService.deletarCriptoMoeda(dtoParaDeletar);

        /**verificação**/
        assertThat(criptoMoedaService.obterCriptomoedaPorId(salvo.getUuid())).isEmpty();
    }

    /**Objetivo 19: lançar exceção ao tentar deletar ID inexistente**/
    @Test
    @DisplayName("deve lançar exceção ao tentar deletar ID inexistente")
    @Transactional
    void deveLancarExcecaoQuandoDeletarIdInexistente() {
        /**cenario**/
        CriptoMoedaDTO idInexistente = criarCriptomoeda();
        idInexistente.setUuid(UUID.randomUUID());
        /**ação**/ /**verificação**/
        assertThatThrownBy(() -> criptoMoedaService.deletarCriptoMoeda(idInexistente))
                .isInstanceOf(EntityNotFoundException.class);
    }

    /**METODOS AUXILIARES**/

    /** auxiliar exigido: retorna DTO válido pronto para salvar */
    public CriptoMoedaDTO criarCriptomoeda() {
        Usuario usuario = persistirUsuario(); // agora persiste o usuario para ter um ID real
        return CriptoMoedaDTO.builder()
                .dataEntrada(LocalDate.now())
                .mes(LocalDate.now().getMonthValue())
                .corretora("Binance")
                .ativo("BTC")
                .alavancagem(null)
                .moedaCorrente("USD")
                .valorInvestido(BigDecimal.valueOf(1000))
                .valorAtualAtivo(BigDecimal.valueOf(1100))
                .fracaoAtivo(BigDecimal.valueOf(0.5))
                .dataSaida(null)
                .usuario(usuario.getId())
                .statusTransacao(null)
                .tipoTransacao(null)
                .build();
    }

    /**cria e persiste um Usuario mínimo para satisfazer FK **/
   public Usuario persistirUsuario() {
       Usuario usuario = Usuario.builder()
               .nomeCompleto("Usuário")
               .cpf("12345678900")
               .nomeUsuario("usuario" + Instant.now().toEpochMilli()) // Evitar duplicidade se houver unique constraints
               .email("usuario" + Instant.now().toEpochMilli() + "@gmail.com")
               .senha("Senha@123")
               .dataCadastro(Instant.now())
               .build();
       return usuarioRepository.save(usuario);
   }

    public static Usuario criarUsuario() {
        return Usuario.builder()
//                .id(1L)
                .nomeCompleto("Usuário")
                .cpf("12345678900")
                .nomeUsuario("usuario")
                .email("usuario@gmail.com")
                .senha("Senha@123")
                .dataCadastro(Instant.now())
                .build();
    }
}
