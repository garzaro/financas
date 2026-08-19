package com.cleber.financas.model.mapper;

import com.cleber.financas.api.dto.CriptoMoedaDTO;
import com.cleber.financas.model.entity.CriptoMoeda;
import com.cleber.financas.model.enums.StatusTransacao;
import com.cleber.financas.model.enums.TipoTransacao;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-17T17:07:59-0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 25.0.2 (Eclipse Adoptium)"
)
@Component
public class CriptoMoedaMapperImpl implements CriptoMoedaMapper {

    @Override
    public CriptoMoeda dtoToEntity(CriptoMoedaDTO dto) {
        if ( dto == null ) {
            return null;
        }

        CriptoMoeda.CriptoMoedaBuilder criptoMoeda = CriptoMoeda.builder();

        criptoMoeda.criptomoeda( dto.getAtivo() );
        criptoMoeda.valorAtual( dto.getValorAtualAtivo() );
        criptoMoeda.fracao( dto.getFracaoAtivo() );
        if ( dto.getAlavancagem() != null ) {
            criptoMoeda.alavancagem( dto.getAlavancagem() );
        }
        else {
            criptoMoeda.alavancagem( "alavancagem não definida" );
        }
        criptoMoeda.corretora( dto.getCorretora() );
        criptoMoeda.dataAtualizacao( dto.getDataAtualizacao() );
        criptoMoeda.dataEntrada( dto.getDataEntrada() );
        criptoMoeda.dataSaida( dto.getDataSaida() );
        criptoMoeda.mes( dto.getMes() );
        criptoMoeda.moedaCorrente( dto.getMoedaCorrente() );
        criptoMoeda.usuario( map( dto.getUsuario() ) );
        criptoMoeda.uuid( dto.getUuid() );
        criptoMoeda.valorInvestido( dto.getValorInvestido() );

        criptoMoeda.tipoTransacao( dto.getTipoTransacao() == null ? TipoTransacao.DECIDIR : dto.getTipoTransacao() );
        criptoMoeda.statusTransacao( dto.getStatusTransacao() == null ? StatusTransacao.ANALISAR : dto.getStatusTransacao() );

        return criptoMoeda.build();
    }

    @Override
    public CriptoMoedaDTO entityToDto(CriptoMoeda criptoMoeda) {
        if ( criptoMoeda == null ) {
            return null;
        }

        CriptoMoedaDTO.CriptoMoedaDTOBuilder criptoMoedaDTO = CriptoMoedaDTO.builder();

        criptoMoedaDTO.ativo( criptoMoeda.getCriptomoeda() );
        criptoMoedaDTO.valorAtualAtivo( criptoMoeda.getValorAtual() );
        criptoMoedaDTO.fracaoAtivo( criptoMoeda.getFracao() );
        if ( criptoMoeda.getAlavancagem() != null ) {
            criptoMoedaDTO.alavancagem( criptoMoeda.getAlavancagem() );
        }
        else {
            criptoMoedaDTO.alavancagem( "" );
        }
        if ( criptoMoeda.getStatusTransacao() != null ) {
            criptoMoedaDTO.statusTransacao( criptoMoeda.getStatusTransacao() );
        }
        else {
            criptoMoedaDTO.statusTransacao( StatusTransacao.ANALISAR );
        }
        if ( criptoMoeda.getTipoTransacao() != null ) {
            criptoMoedaDTO.tipoTransacao( criptoMoeda.getTipoTransacao() );
        }
        else {
            criptoMoedaDTO.tipoTransacao( TipoTransacao.DECIDIR );
        }
        criptoMoedaDTO.corretora( criptoMoeda.getCorretora() );
        criptoMoedaDTO.dataAtualizacao( criptoMoeda.getDataAtualizacao() );
        criptoMoedaDTO.dataEntrada( criptoMoeda.getDataEntrada() );
        criptoMoedaDTO.dataSaida( criptoMoeda.getDataSaida() );
        criptoMoedaDTO.mes( criptoMoeda.getMes() );
        criptoMoedaDTO.moedaCorrente( criptoMoeda.getMoedaCorrente() );
        criptoMoedaDTO.usuario( map( criptoMoeda.getUsuario() ) );
        criptoMoedaDTO.uuid( criptoMoeda.getUuid() );
        criptoMoedaDTO.valorInvestido( criptoMoeda.getValorInvestido() );

        return criptoMoedaDTO.build();
    }
}
