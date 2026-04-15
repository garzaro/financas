package com.cleber.financas.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import com.cleber.financas.api.dto.CriptoMoedaDTO;
import com.cleber.financas.model.entity.CriptoMoeda;
import com.cleber.financas.model.entity.Usuario;

/**
 * TO-DO list
 * 
 * [] configurar o Mapper para apenas realizar o mapeamento se o valor de origem não for nulo. Isso é feito na anotação @Mapper
 *    para evitar NullPointerException em mapeamentos aninhados (como usuario.getId()).
 *    "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
 * []     
 * **/

@Component
@Mapper(componentModel = "spring") // Permite injetar o mapper com @Autowired/@RequiredArgsConstructor
public interface CriptoMoedaMapper {

//	List<CriptoMoedaDTO> criptomoedaToDto (List<CriptoMoeda> critpomoeda);	
	
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
	
	/**toEntity - O MapStruct usará este método para converter o Long usuario em Usuario entidade**/
		default Usuario map(Long id) {
		    if (id == null) {
		        return null;
		    }
		    Usuario usuario = new Usuario();
		    usuario.setId(id);
		    return usuario;
		}	
	
	/**response - frontend**/ 
	@Mapping(source = "criptomoeda", target = "ativo")
	@Mapping(source = "valorAtual", target = "valorAtualAtivo")
	@Mapping(source = "fracao", target = "fracaoAtivo")
	@Mapping(source = "alavancagem", target = "alavancagem", defaultValue = "")
	@Mapping(source = "statusTransacao", target = "statusTransacao", defaultValue = "ANALISAR")
	@Mapping(source = "tipoTransacao", target = "tipoTransacao", defaultValue = "DECIDIR")
	//	@Mapping(source = "usuario", target = "id")
	CriptoMoedaDTO entityToDto (CriptoMoeda criptoMoeda);	
	
	/**toDto - O MapStruct chamará este método para converter o objeto Usuario em Long**/ 
    default Long map(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return usuario.getId();
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









