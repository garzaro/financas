# Exemplo de Uso - ErroResposta

## Visão Geral
O record `ErroResposta` foi criado para capturar e estruturar mensagens de erro de integridade e validação dos campos de forma padronizada.

## Estrutura

```java
public record ErroResposta(
    String mensagem,           // Mensagem geral do erro
    Integer status,            // Status HTTP (400, 409, 404, etc)
    Instant timestamp,         // Timestamp do erro
    List<CampoErro> erros     // Lista de erros específicos dos campos
)
```

### Record CampoErro (interno)
```java
public record CampoErro(
    String campo,              // Nome do campo com erro
    String mensagem,           // Mensagem de erro específica
    Object valorRejeitado      // Valor que foi rejeitado (opcional)
)
```

## Exemplos de Uso

### 1. Criação Simples
```java
// Apenas mensagem e status
ErroResposta erro = new ErroResposta("Email inválido", 400);
```

### 2. Com Factory Methods
```java
// Erro de validação (status 400)
ErroResposta validacao = ErroResposta.validacao("Campos obrigatórios não preenchidos");

// Erro de integridade (status 409)
ErroResposta integridade = ErroResposta.integridade("Email já cadastrado no sistema");

// Recurso não encontrado (status 404)
ErroResposta notFound = ErroResposta.naoEncontrado("Usuário não encontrado");
```

### 3. Adicionando Erros de Campos
```java
ErroResposta erro = new ErroResposta("Validação falhou", 400);

// Adicionar um erro de campo simples
erro.adicionarErro("email", "Email deve ser válido");

// Adicionar erro com valor rejeitado
erro.adicionarErro("cpf", "CPF inválido", "12345678901");

// Encadeamento de múltiplos erros
erro.adicionarErro("nome", "Nome não pode ser vazio")
    .adicionarErro("sobrenome", "Sobrenome não pode ser vazio")
    .adicionarErro("idade", "Idade deve ser maior que 18", 15);
```

### 4. Usando o Builder (Recomendado)
```java
ErroResposta erro = ErroResposta.builder()
    .mensagem("Validação de usuário falhou")
    .status(400)
    .adicionarErro("email", "Email já existe no sistema", "joao@email.com")
    .adicionarErro("cpf", "CPF já cadastrado", "12345678901")
    .adicionarErro("nomeUsuario", "Nome de usuário deve ter mínimo 5 caracteres", "ab")
    .construir();
```

### 5. Métodos Auxiliares
```java
ErroResposta erro = ErroResposta.validacao("Erro na validação");
erro.adicionarErro("email", "Email inválido");
erro.adicionarErro("senha", "Senha muito fraca");

// Verificar se há erros
if (erro.temErros()) {
    System.out.println("Há erros a corrigir");
}

// Quantidade de erros
int total = erro.quantidadeErros(); // retorna 2

// Campos com erro
List<String> campos = erro.obterCamposComErro(); // ["email", "senha"]

// Mensagem de um campo específico
String msg = erro.obterMensagemErro("email"); // "Email inválido"
```

### 6. Em Controllers (com CustomExceptionHandler)
```java
@PostMapping("/usuarios")
public ResponseEntity<UsuarioDTO> criarUsuario(@RequestBody UsuarioDTO dto) {
    // Validação personalizada
    if (!isEmailValido(dto.getEmail())) {
        ErroResposta erro = ErroResposta.builder()
            .mensagem("Falha na validação dos dados")
            .status(400)
            .adicionarErro("email", "Email deve ser válido", dto.getEmail())
            .construir();
        return ResponseEntity.badRequest().body(null); // Usar entity do erro
    }
    
    try {
        UsuarioDTO usuario = usuarioService.criar(dto);
        return ResponseEntity.ok(usuario);
    } catch (RegraDeNegocioException e) {
        // CustomExceptionHandler captura automaticamente
        throw e;
    }
}
```

### 7. Exemplo Completo de Validação
```java
@PostMapping("/usuarios")
public ResponseEntity<ErroResposta> validarUsuario(@RequestBody UsuarioDTO dto) {
    List<ErroResposta.CampoErro> erros = new ArrayList<>();
    
    if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
        erros.add(new ErroResposta.CampoErro("email", "Email não pode ser vazio", null));
    } else if (!isEmailValido(dto.getEmail())) {
        erros.add(new ErroResposta.CampoErro("email", "Email inválido", dto.getEmail()));
    }
    
    if (dto.getSenha() == null || dto.getSenha().length() < 8) {
        erros.add(new ErroResposta.CampoErro("senha", "Senha deve ter no mínimo 8 caracteres", 
                                             dto.getSenha() != null ? dto.getSenha().length() : 0));
    }
    
    if (!erros.isEmpty()) {
        ErroResposta erro = new ErroResposta(
            "Validação falhou",
            400,
            Instant.now(),
            erros
        );
        return ResponseEntity.badRequest().body(erro);
    }
    
    // Prosseguir com a criação
    return ResponseEntity.ok(null);
}
```

## JSON de Resposta

### Resposta com Erros
```json
{
  "mensagem": "Validação de cadastro falhou",
  "status": 400,
  "timestamp": "2024-06-11T15:30:45.123456Z",
  "erros": [
    {
      "campo": "email",
      "mensagem": "Email já existe no sistema",
      "valorRejeitado": "joao@email.com"
    },
    {
      "campo": "cpf",
      "mensagem": "CPF inválido",
      "valorRejeitado": "12345678901"
    }
  ]
}
```

### Resposta Sem Erros de Campo
```json
{
  "mensagem": "Usuário não encontrado",
  "status": 404,
  "timestamp": "2024-06-11T15:30:45.123456Z",
  "erros": []
}
```

## Métodos Disponíveis

| Método | Descrição | Retorno |
|--------|-----------|---------|
| `adicionarErro(campo, mensagem)` | Adiciona erro simples | `ErroResposta` |
| `adicionarErro(campo, mensagem, valor)` | Adiciona erro com valor rejeitado | `ErroResposta` |
| `adicionarErros(lista)` | Adiciona múltiplos erros | `ErroResposta` |
| `temErros()` | Verifica se há erros | `boolean` |
| `quantidadeErros()` | Retorna quantidade de erros | `int` |
| `obterCamposComErro()` | Lista campos com erro | `List<String>` |
| `obterMensagemErro(campo)` | Obtém mensagem de um campo | `String` |
| `builder()` | Factory para Builder | `Builder` |
| `validacao(msg)` | Factory para erro 400 | `ErroResposta` |
| `integridade(msg)` | Factory para erro 409 | `ErroResposta` |
| `naoEncontrado(msg)` | Factory para erro 404 | `ErroResposta` |

## Status HTTP Padrão

- **400**: Bad Request (validação simples)
- **404**: Not Found (recurso não encontrado)
- **409**: Conflict (erro de integridade de negócio)
- **401**: Unauthorized (erro de autenticação - vê ErroResponse)
- **500**: Internal Server Error (erros internos)

## Integração com CustomExceptionHandler

O `CustomExceptionHandler` agora captura automaticamente:
- `ErroValidacaoException` → 400
- `RegraDeNegocioException` → 409

Exemplo:
```java
if (emailJaExiste) {
    throw new RegraDeNegocioException("Email já cadastrado");
    // CustomExceptionHandler converte para ErroResposta com status 409
}
```

## Notas

- Use encadeamento quando adicionar múltiplos erros
- O timestamp é automaticamente definido como o momento atual
- A anotação `@JsonInclude(NON_EMPTY)` remove campos vazios do JSON
- O `valorRejeitado` é opcional e útil para debug
- Prefira o Builder para construções mais complexas

#### EXEMPLO A SER USADO NO ERROREPOSTA
/**
	 * Record interno para representar erro em um campo específico
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record CampoErro(
			String campo,
			String mensagem,
			Object valorRejeitado
	) {}
	
	/**
	 * Construtor padrão com mensagem e status
	 */
	public ErroResposta(String mensagem, Integer status) {
		this(mensagem, status, Instant.now(), new ArrayList<>());
	}
	
	/**
	 * Construtor com mensagem, status e timestamp
	 */
	public ErroResposta(String mensagem, Integer status, Instant timestamp) {
		this(mensagem, status, timestamp, new ArrayList<>());
	}
	
	

	/**
	 * Adiciona erro de campo à lista de erros
	 * @param campo nome do campo com erro
	 * @param mensagem mensagem de erro
	 * @return this para encadeamento
	 */
	public ErroResposta adicionarErro(String campo, String mensagem) {
		this.erros().add(new CampoErro(campo, mensagem, null));
		return this;
	}
	
	/**
	 * Adiciona erro de campo com valor rejeitado
	 * @param campo nome do campo com erro
	 * @param mensagem mensagem de erro
	 * @param valorRejeitado valor que foi rejeitado
	 * @return this para encadeamento
	 */
	public ErroResposta adicionarErro(String campo, String mensagem, Object valorRejeitado) {
		this.erros().add(new CampoErro(campo, mensagem, valorRejeitado));
		return this;
	}
	
	/**
	 * Adiciona múltiplos erros de uma vez
	 * @param errosDosCampos mapa com campo e mensagem
	 * @return this para encadeamento
	 */
	public ErroResposta adicionarErros(List<CampoErro> errosDosCampos) {
		if (errosDosCampos != null) {
			this.erros().addAll(errosDosCampos);
		}
		return this;
	}
	
	/**
	 * Verifica se há erros registrados
	 * @return true se há erros, false caso contrário
	 */
	public boolean temErros() {
		return this.erros() != null && !this.erros().isEmpty();
	}
	
	/**
	 * Retorna a quantidade de erros registrados
	 * @return quantidade de erros
	 */
	public int quantidadeErros() {
		return this.erros() != null ? this.erros().size() : 0;
	}
	
	/**
	 * Obtém todos os nomes dos campos com erro
	 * @return lista com nomes dos campos com erro
	 */
	public List<String> obterCamposComErro() {
		return this.erros() != null 
			? this.erros().stream().map(CampoErro::campo).toList()
			: new ArrayList<>();
	}
	
	/**
	 * Obtém mensagem de erro para um campo específico
	 * @param campo nome do campo
	 * @return mensagem do erro ou null se não encontrado
	 */
	public String obterMensagemErro(String campo) {
		return this.erros() != null
			? this.erros().stream()
				.filter(e -> e.campo().equals(campo))
				.map(CampoErro::mensagem)
				.findFirst()
				.orElse(null)
			: null;
	}
	
	/**
	 * Builder para construção fluente de ErroResposta
	 */
	public static class Builder {
		private String mensagem;
		private Integer status;
		private Instant timestamp = Instant.now();
		private List<CampoErro> erros = new ArrayList<>();
		
		public Builder mensagem(String mensagem) {
			this.mensagem = mensagem;
			return this;
		}
		
		public Builder status(Integer status) {
			this.status = status;
			return this;
		}
		
		public Builder timestamp(Instant timestamp) {
			this.timestamp = timestamp;
			return this;
		}
		
		public Builder adicionarErro(String campo, String mensagem) {
			this.erros.add(new CampoErro(campo, mensagem, null));
			return this;
		}
		
		public Builder adicionarErro(String campo, String mensagem, Object valorRejeitado) {
			this.erros.add(new CampoErro(campo, mensagem, valorRejeitado));
			return this;
		}
		
		public Builder adicionarErros(List<CampoErro> errosDosCampos) {
			if (errosDosCampos != null) {
				this.erros.addAll(errosDosCampos);
			}
			return this;
		}
		
		public ErroResposta construir() {
			if (this.mensagem == null || this.mensagem.isEmpty()) {
				throw new IllegalArgumentException("Mensagem não pode estar vazia");
			}
			if (this.status == null) {
				throw new IllegalArgumentException("Status HTTP é obrigatório");
			}
			return new ErroResposta(this.mensagem, this.status, this.timestamp, this.erros);
		}
	}
	
	/**
	 * Factory method para criar um builder
	 */
	public static Builder builder() {
		return new Builder();
	}
	
	/**
	 * Factory method para criar ErroResposta de validação
	 */
	public static ErroResposta validacao(String mensagem) {
		return new ErroResposta(mensagem, 400);
	}
	
	/**
	 * Factory method para criar ErroResposta de integridade
	 */
	public static ErroResposta integridade(String mensagem) {
		return new ErroResposta(mensagem, 409);
	}
	
	/**
	 * Factory method para criar ErroResposta de recurso não encontrado
	 */
	public static ErroResposta naoEncontrado(String mensagem) {
		return new ErroResposta(mensagem, 404);
	}
