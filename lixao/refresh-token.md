Contexto:
Estamos ajustando o fluxo de autenticação da aplicação React para suportar Refresh Token e corrigir inconsistências identificadas de rota e reidratação de estado. A implementação ocorrerá em paralelo com o Backend Java Spring.

Objetivo Principal:
Refactorar `authProvider.jsx`, `authService.js` e criar interceptores no cliente HTTP (Axios) para realizar a renovação automática de sessão via Refresh Token e corrigir as rotas de redirecionamento.

---
O QUE VOCÊ DEVE SABER (ARQUITETURA & REGRAS):
- Rota Oficial de Tela de Login: `/sign-in` (definida em `rotas.jsx`).
- Usuários não autenticados DEVEM ser redirecionados para `/sign-in` (NÃO enviar para `/`).
- Contrato da API esperado:
  * Login: POST `/api/auth/sign-in` -> devolve `{ accessToken, refreshToken, user }`.
  * Refresh: POST `/api/auth/refresh` -> devolve `{ accessToken, refreshToken }`.

---
O QUE VOCÊ DEVE FAZER (TAREFAS TÉCNICAS):
1. Limpeza de Código e Depreciação:
   - Marcar como obsoleto o arquivo `authContext.jsx`, concentrando todo o estado e lógica unicamente em `authProvider.jsx`.
2. Correção da Tela de Login e Persistência:
   - Em `login.jsx`, corrigir a chamada de login para que passe o token recebido da API: `login(accessToken, user)`.
   - Garantir que o `accessToken` e o `refreshToken` sejam persistidos adequadamente (LocalStorage / Session / Cookies) conforme a diretriz definida.
3. Implementação do Interceptor de Refresh Token:
   - Criar um interceptor de resposta HTTP para capturar respostas `401 Unauthorized`.
   - Implementar uma fila de requisições pausadas (queue) caso um refresh já esteja em andamento, impedindo que múltiplos requests simultâneos façam chamadas repetidas a `/refresh`.
   - Se a chamada a `/refresh` for bem-sucedida, atualizar o header de autorização e re-executar os requests da fila.
   - Se o refresh falhar (401), realizar o logout completo (limpar armazenamento local) e navegar para `/sign-in`.
4. Reidratação da Sessão e Guards de Rota:
   - Atualizar a inicialização do `authProvider.jsx` para reidratar o usuário/token ao recarregar a página (F5).
   - Ajustar o componente de proteção de rotas privadas para direcionar acessos negados especificamente para a rota `/sign-in`.

---
ENTREGÁVEIS ESPERADOS:
1. `authProvider.jsx` e `authService.js` refatorados e integrados com o interceptor de refresh.
2. `login.jsx` e `rotas.jsx` com caminhos e payloads corrigidos.
3. Teste/Mock do fluxo funcionando mesmo antes da API backend estar 100% pronta.