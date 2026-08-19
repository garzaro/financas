# Financas API

API de finanças pessoais responsável por autenticação, cadastro de usuários,
gestão de lançamentos financeiros e registro de criptoativos.

## 1. Visão Geral do Projeto

O backend atende o domínio de finanças pessoais com foco em:

- autenticação e autorização via JWT;
- cadastro e manutenção de usuários;
- controle de lançamentos (receitas e despesas), incluindo status;
- cálculo de saldo por usuário;
- persistência auditável de entidades de negócio.

A aplicação foi estruturada para operar como serviço stateless, com regras de
negócio centralizadas na camada de serviço e exposição via API REST.

## 2. Tecnologias & Stack

- Java 21
- Spring Boot 3.5.x
- Spring Web (API REST)
- Spring Security (JWT + policy stateless)
- Spring Data JPA / Hibernate
- PostgreSQL (principal)
- Flyway (migrações)
- Hibernate Envers (auditoria)
- MapStruct (mapeamento DTO <-> entidade)
- Lombok
- Maven
- H2 (suporte a testes)
- JUnit 5 / Spring Boot Test / MockMvc

## 3. Arquitetura & Estrutura de Pastas

Organização principal em `src/main/java/com/cleber/financas`:

- `api/resource`: controladores REST (endpoints HTTP)
- `api/dto`: contratos de entrada/saída da API
- `api/converter`: conversão entre DTOs e entidades
- `service`: interfaces de regras de negócio
- `service/impl`: implementações dos casos de uso
- `model/entity`: entidades JPA
- `model/repository`: repositórios Spring Data
- `model/enums`: enums de domínio
- `model/mapper`: mappers MapStruct
- `security`: filtro JWT e contexto de autenticação
- `config`: segurança, validação e beans de infraestrutura
- `exception`: exceções de domínio e respostas de erro

Recursos e configuração em `src/main/resources`:

- `application.properties`
- `application-test.properties`
- `db/migration` (scripts Flyway)

## 4. Pré-requisitos & Instalação

### Pré-requisitos

- JDK 21
- Maven 3.9+
- PostgreSQL 14+ (ou compatível)
- Docker (opcional)

### 4.1 Banco de dados local (PostgreSQL)

Exemplo usando Docker:

```bash
docker run --name financas-postgres \
  -e POSTGRES_USER=user \
  -e POSTGRES_PASSWORD=user \
  -e POSTGRES_DB=db \
  -p 5432:5432 \
  -d postgres:16
```

### 4.2 Variáveis de ambiente

O projeto lê `.env` via `spring.config.import`. Configure no mínimo:

```env
JWT_SECRET=<segredo_forte_para_assinatura_jwt>
JWT_EXPIRATION=3600000
FLYWAY_LOCATIONS=classpath:db/migration
```

Observação: as credenciais de datasource padrão estão em
`src/main/resources/application.properties` (localhost:5432, banco `banco`,
usuário `user`, senha `senha`).

### 4.3 Executar a aplicação

```bash
mvn clean spring-boot:run
```

API disponível em:

- `http://localhost:8080`

### 4.4 Build e execução via Docker

```bash
docker build -t financas-api .
docker run --rm -p 8080:8080 --env-file .env financas-api
```

## 5. Documentação da API

Rotas de referência (principais):

- `POST /api/auth/sign-in` - autenticação e emissão de JWT
- `POST /api/auth/join/sign-up` - cadastro de usuário
- `PUT /api/auth/{id}` - atualização de usuário
- `GET /api/auth/{id}/saldo` - saldo consolidado do usuário
- `POST /api/Fp/lancamento` - criação de lançamento
- `PUT /api/Fp/lancamento/{id}` - atualização de lançamento
- `PUT /api/Fp/lancamento/{id}/atualizar-statusLancamento` - mudança de status
- `GET /api/Fp/lancamento/{id}` - detalhe de lançamento
- `GET /api/Fp/lancamento` - busca por filtros
- `DELETE /api/Fp/lancamento/{id}` - remoção de lançamento
- `POST /api/criptomoeda` - cadastro de criptoativo

Swagger/OpenAPI (conforme `application.properties`):

- UI: `http://localhost:8080/swagger-ui/index.html`
- JSON OpenAPI: `http://localhost:8080/v3/api-docs`

## 6. Testes Automatizados

Executar toda a suíte:

```bash
mvn clean test -Dspring.profiles.active=test
```

Executar uma classe específica:

```bash
mvn -Dtest=UsuarioResourceRestTest test -Dspring.profiles.active=test
```

O perfil `test` utiliza configuração dedicada em
`src/main/resources/application-test.properties` (H2 em memória), útil para
testes de integração sem dependência externa de banco.

## 7. Padrões de Código e Diretrizes

- Tratamento global de erros centralizado em
  `api/resource/common/GlobalExceptionHandler`.
- Regras de negócio devem residir na camada `service`, não nos controllers.
- DTOs e conversores devem isolar contratos HTTP do modelo de persistência.
- Segurança com JWT stateless: rotas públicas mínimas; demais rotas protegidas.
- Logs com SLF4J/Logback, priorizando mensagens objetivas e contexto técnico.
- Migrações de banco devem ser versionadas via Flyway em `db/migration`.
- Mudanças em entidades críticas devem preservar rastreabilidade de auditoria.

