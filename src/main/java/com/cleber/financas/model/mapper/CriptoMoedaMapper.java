package com.cleber.financas.model.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import com.cleber.financas.api.dto.CriptoMoedaDTO;
import com.cleber.financas.model.entity.CriptoMoeda;
import com.cleber.financas.model.entity.Usuario;

/**
 * TO-DO list
 * []
 * **/

@Component
@Mapper(componentModel = "spring") // Permite injetar o mapper com @Autowired/@RequiredArgsConstructor
public interface CriptoMoedaMapper {

	/**request - server**/
	@Mapping(source = "ativo", target = "criptomoeda")
	@Mapping(source = "valorAtualAtivo", target = "valorAtual")
	@Mapping(source = "fracaoAtivo", target = "fracao")
	@Mapping(source = "alavancagem", target = "alavancagem", defaultValue = "alavancagem não definida")
	@Mapping(target = "tipoTransacao", expression = "java(dto.getTipoTransacao() == null ? TipoTransacao.DECIDIR : dto.getTipoTransacao())")
	@Mapping(target = "statusTransacao", expression = "java(dto.getStatusTransacao() == null ? StatusTransacao.ANALISAR : dto.getStatusTransacao())")
//	@Mapping(source = "statusTransacao", target = "statusTransacao", defaultValue = "ANALISAR")
//	@Mapping(source = "id", target = "usuario")
	CriptoMoeda dtoToEntity (CriptoMoedaDTO dto);

	/**toEntity - O MapStruct usará este metodo para converter o Long usuario em Usuario entidade**/
		default Usuario map(UUID uuid) {
		    if (uuid == null) {
		        return null;
		    }
		    Usuario usuario = new Usuario();
		    usuario.setUuid(uuid);
		    return usuario;
		}

	/**response - frontend**/
	@Mapping(source = "criptomoeda", target = "ativo")
	@Mapping(source = "valorAtual", target = "valorAtualAtivo")
	@Mapping(source = "fracao", target = "fracaoAtivo")
	@Mapping(source = "alavancagem", target = "alavancagem", defaultValue = "")
	@Mapping(source = "statusTransacao", target = "statusTransacao", defaultValue = "ANALISAR")
	@Mapping(source = "tipoTransacao", target = "tipoTransacao", defaultValue = "DECIDIR")
	@Mapping(source = "usuario", target = "usuarioUuid")
	CriptoMoedaDTO entityToDto (CriptoMoeda criptoMoeda);

	/**toDto - O MapStruct chamará este metodo para converter o objeto Usuario em Long**/
    default UUID map(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return usuario.getUuid();
    }
}



//2. Usar uma expressão Java no Mapper
//lógica mais dinâmica ou garantir que o valor seja setado independentemente do que venha no DTO, use expression:
//java
//@Mapping(target = "statusTransacao", expression = "java(dto.getstatusTransacao() == null ? statusTransacao.PENDENTE : dto.getstatusTransacao())")
//Compra toEntity(CompraDTO dto);


//para ignorar o mapeamento caso venha nulo (manter o que já está na entidade)
//@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
//void updateEntityFromDto(CriptoMoedaDTO dto, @MappingTarget CriptoMoeda entity);









