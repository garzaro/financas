# 📜 spec.md — Correção de Persistência e Reidratação de Sessão ao Recarregar a Página (F5)

## 1. Visão Geral
Atualmente, quando um usuário autenticado recarrega a página (`F5`) estando dentro de uma rota protegida da área logada, a aplicação perde temporariamente o estado de autenticação e redireciona o usuário para a tela inicial/login (`/`). 

O objetivo desta especificação é garantir que o estado de autenticação seja reidratado corretamente antes da avaliação das rotas protegidas, mantendo o usuário na página atual.

---

## 2. Histórias de Usuário (User Stories) & Critérios de Aceite

### US-01: Permanência na Área Logada após F5
> **Como** usuário logado na aplicação,  
> **Quero** recarregar a página (`F5`) enquanto navego por rotas protegidas,  
> **Para que** eu continue na mesma página sem ser deslogado ou redirecionado para a tela de login.

#### Critérios de Aceite:
- [ ] O estado de autenticação (sessão via Cookies) deve ser revalidado no backend/reidratado na aplicação antes do render das rotas protegidas.
- [ ] Enquanto o estado de autenticação está sendo reidratado, a aplicação **deve** exibir um indicador de carregamento (*loading spinner/screen*).
- [ ] A rota protegida **não pode** redirecionar para `/login` ou '/' até que o processo de reidratação/inicialização (`loading = false`) tenha terminado.
- [ ] Caso a sessão/Cookie seja válida após a checagem, a rota solicitada pelo usuário deve ser exibida normalmente.
- [ ] Caso o Cookie esteja ausente ou expirado após o término do carregamento, o usuário deve ser redirecionado para a tela de login.

---

## 3. Requisitos Funcionais e Técnicos

### 3.1. Gerenciamento do Estado Global de Autenticação (`AuthContext` / `AuthStore`)
1. **Estado de Inicialização (`isLoading`):**
   - O estado global de autenticação deve conter a propriedade booleana `isLoading` iniciada como `true`.
2. **Ciclo de Vida do Boot (Reidratação via Cookies):**
   - No carregamento inicial do app (`useEffect` / montagem da aplicação):
     - - Executar uma chamada de verificação de sessão ao backend (ex: `GET /api/auth/...`), garantindo o envio dos cookies (`withCredentials: true`).
     - Se o backend responder `200 OK`, definir `isAuthenticated = true` e atualizar os dados do usuário no estado global.
     - Se o backend responder `401 Unauthorized` ou houver falha de rede, definir `isAuthenticated = false`.
     - Definir `isLoading = false` obrigatoriamente no bloco `finally` (independente de sucesso ou erro).

### 3.2. Rota Protegida (`ProtectedRoute` / `PrivateRoute`)
1. **Bloqueio de Avaliação Precoce:**
   - O componente de rota protegida deve checar primeiramente a flag `isLoading`.
   - Se `isLoading === true`, o componente deve retornar um indicador de carregamento (ex: `<LoadingScreen/>`) e **nunca** renderizar o `<Navigate to="/login"/>`.
   - Apenas quando `isLoading === false`, avaliar:
     - Se `isAuthenticated === true` -> Renderizar a rota (`<Outlet/>` ou `children`).
     - Se `isAuthenticated === false` -> Redirecionar para `/login`.

### 3.3. Configuração do Cliente e Interceptores HTTP (Axios)
1. **Envio Automático de Cookies:**
   - Garantir que a instância do Axios / Fetch esteja configurada globalmente com `withCredentials: true` para que o navegador inclua os cookies de sessão em todas as requisições, inclusive no boot da aplicação.
2. **Tratamento de 401 durante a Inicialização:**
   - Interceptores não devem forçar redirecionamento visual enquanto `isInitializing` for `true`. Apenas a própria rota protegida deve decidir o redirecionamento após a resolução da promessa de verificação.

3  **Interceptor de Requisição (Request Interceptor):**
   - Garantir a propriedade withCredentials: true para que o navegador envie automaticamente os Cookies HTTP-Only de sessão em todas as requisições para o backend.
   -
   **Interceptor de Resposta (Response Interceptor):**
   - Tratamento de 401 (Unauthorized):
   - Se o backend retornar 401 Unauthorized em qualquer requisição normal do app, o interceptor deve notificar o AuthContext para atualizar isAuthenticated = false e limpar dados do usuário.
   - Bloqueio durante a Inicialização: Durante a chamada inicial de boot (/auth/me), o interceptor não deve disparar um redirecionamento forçado via window.location. Ele deve apenas repassar o erropara        a promessa de reidratação ser tratada no AuthContext.
   - Refresh Token, se for o caso (Opcional se aplicável): Caso utilize a estratégia de Cookie de Refresh Token, ao receber 401, o interceptor deve tentar renovar o token via POST /auth/refresh antes de rejeitar a requisição original.

---

## 4. Plano de Testes

| Caso de Teste | Ação | Resultado Esperado |
| :--- | :--- | :--- |
| **CT-01** | Fazer login e dar `F5` na página do Dashboard. | Tela de carregamento curta é exibida enquanto `/auth/me` executa; o Dashboard é mantido após o retorno `200 OK`. |
| **CT-02** | Deletar o Cookie de sessão nas ferramentas de desenvolvedor do navegador e dar `F5`. | A verificação `/auth/me` retorna `401` e o usuário é redirecionado para `/login`. |
| **CT-03** | Tentar acessar uma rota protegida diretamente pela URL sem estar logado. | A verificação falha e o usuário é redirecionado para `/login`. |
| **CT-04** | Dar `F5` com o Cookie expirado no servidor. | A chamada da API retorna `401`, limpa o estado e o usuário é redirecionado para `/login`. |

---

## 5. Matriz de Mapeamento de Arquivos Afetados

- `src/contexts/AuthContext.tsx` (ou arquivo equivalente da sua store: Zustand/Redux) — *Implementar o `useEffect` com chamada `/auth/me` usando `withCredentials: true` e controle da flag `isInitializing`.*
- `src/routes/ProtectedRoute.tsx` — *Implementar o bloqueio de renderização do `<Navigate/>` enquanto `isInitializing` for true.*
- `src/services/api.ts` — *Garantir `withCredentials: true` na instância padrão de requisições.*