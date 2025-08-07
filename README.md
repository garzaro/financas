# Sistema de finança pessoal - React + Spring Boot

Este projeto implementa um sistema completo de financas pessoais com frontend em React usando PrimeReact e backend em Spring Boot com PostgreSQL.

## 🏗️ Estrutura do Projeto

```
├── financas-backend/           # Backend Spring Boot
│   ├── src/main/java/com/financas/
│   │   ├── model/              # Entidades JPA
│   │   ├── repository/         # Repositórios Spring Data
│   │   ├── service/            # Interfaces de serviço
│   │   ├── implementação)      # Implementação de serviço
│   │   ├── controller/         # Controllers REST
│   │   └── config/             # Configurações
│   ├── src/main/resources/     # Arquivos de configuração
│   └── pom.xml                 # Dependências Maven
└── formulario-frontend/         # Frontend React
    ├── src/
    │   ├── components/         # Componentes React
    │   ├── services/           # Serviços de API
    │   ├── views/              # Telas
    │   └── assets/             # Recursos estáticos
    └── package.json            # Dependências npm , yarn
```

## 🚀 Tecnologias Utilizadas

### Backend
- **Spring Boot 3.3.0** - Framework principal
- **Spring Data JPA** - Persistência de dados
- **PostgreSQL** - Banco de dados
- **Maven** - Gerenciamento de dependências
- **Spring Boot Validation** - Validação de dados

### Frontend
- **React 18** - Biblioteca de interface
- **PrimeReact** - Componentes de UI
- **Axios** - Cliente HTTP
- **Tailwind CSS** - Estilização
- **CRA** - Build tool

## 📋 Funcionalidades

### Formulário com campos para criação de usuario:
1. **Nome** (obrigatório) - Texto até 100 caracteres
2. **Email** (obrigatório) - Email válido até 150 caracteres
3. **CPF** (obrigatório) - Texto até 14 caracteres
4. **Usuario** (obrigatório) - Texto até 100 caracteres
5. **Senha** (obrigatório) - Texto até 100 caracteres

### Formulário com campos para criação de lancamentos:
1. **Descrição** (obrigatório) - Texto até 150 caracteres
2. **Mes** (obrigatório) - Select
3. **Ano** (obrigatório) - Select
4. **Valor** (obrigatório) - Decimal
5. **Data Criacao** (automatico) - Padrao Date
5. **Tipo Lancamento** (obrigatório) - Enum - DESPESA - RECEITA
5. **Status Lancamento** (obrigatório) - Enum - CANCELADO - EFETIVADO - PENDENTE


### Formulário para login:
1. **Nome** (obrigatório) - Texto criado na inscrição
2. **Email** (obrigatório) - Email criado nao inscrição


### Funcionalidades:
- ✅ Validação completa de campos
- ✅ Botões Salvar e Cancelar
- ✅ Integração com backend via API REST
- ✅ Mensagens de sucesso e erro
- ✅ Interface responsiva
- ✅ Verificação de email duplicado
- ✅ Verificação de CPF duplicado


## 🛠️ Configuração e Execução

### Pré-requisitos
- Java 17+
- Node.js 18+
- PostgreSQL 12+
- Maven 3.6+

### 1. Configuração do Banco de Dados

```sql
-- Criar banco de dados
CREATE DATABASE financa_db;

-- Criar usuário (opcional)
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE financa_db TO postgres;
```

### 2. Executar o Backend

```bash
cd financa

# Compilar o projeto
mvn clean compile

# Executar a aplicação
mvn spring-boot:run
```

O backend estará disponível em: `http://localhost:8080`

### 3. Executar o Frontend

```bash
cd financa-app

# Instalar dependências
yarn add

# Executar em modo desenvolvimento
yarn start
```

O frontend estará disponível em: `http://localhost:3000`

## 📡 API Endpoints

### Formulários
- `POST /api/usuarios` - Criar novo usuario
- `GET /api/usuarios/{id}` - Buscar usuarios por ID
- `GET /api/usuarios` - Buscar por email
- `GET /api/usuarios` - Verificar se email existe
- `POST /api/lancamentos` - Criar novo lancamento
- `GET /api/lancamentos/{id}` - Buscar lancamento por ID
- `PUT /api/lancamentos/{id}` - Atualizar lancamentos
- `DELETE /api/lancamentos/{id}` - Deletar lancamentos
- `Busca por múltiplos critérios` - ()

## 🏛️ Arquitetura do Backend

### Camadas Implementadas:
1. **Model** - Entidade JPA com validações
2. **Repository** - Interface Spring Data JPA
3. **Service** - Interface de serviço
4. **ServiceImpl** - Implementação dos serviços
5. **Controller** - Endpoints REST com CORS

### Validações:
- Campos obrigatórios
- Formato de email
- Formato de senha
- Tamanho máximo dos campos
- Email único no sistema
- CPF único no sistema

## 🎨 Interface do Usuário

- Design responsivo com PrimeReact
- Validação em tempo real
- Mensagens de feedback
- Calendário para seleção de data
- Contador de caracteres
- Loading states

## Controle de versionamento
- Migration - flayway

## Auditria de dados
- Historico de entidade - Envers 

## 🔧 Configurações


### Backend (application.properties)
```properties
# Banco de dados
spring.datasource.url=jdbc:postgresql://localhost:5432/financa_db
spring.datasource.username=postgres
spring.datasource.password=postgres

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# CORS
spring.web.cors.allowed-origins=*
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
spring.web.cors.allowed-headers=*
```

### Frontend (src/services/api.js)
```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

## 📝 Exemplo de Uso

1. Acesse `http://localhost:3000`
2. Preencha todos os campos obrigatórios
3. Clique em "Salvar" para enviar os dados
4. Use "Cancelar" para limpar formulário

## 🚀 Deploy

Para deploy em produção:

1. **Backend**: Gerar JAR com `mvn clean package`
2. **Frontend**: Build com `pnpm run build`
3. Configurar variáveis de ambiente para produção
4. Configurar banco PostgreSQL em produção

## 📄 Licença

Este projeto foi desenvolvido com integração React + Spring Boot.

