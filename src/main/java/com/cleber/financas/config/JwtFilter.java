package com.cleber.financas.config;

/*
* A ressalva do Contexto de Segurança (SecurityContext): Na sua frase, você mencionou "retorna para
*  o AuthenticationManager inserir no contexto de segurança". Como você está usando sessões STATELESS
*   com JWT, no momento do login (no seu AuthController), o que acontece é: o AuthenticationManager 
*   te devolve esse "crachá" de autenticado. Você pega esse crachá, gera um token JWT com base nele, 
*   e devolve o JWT para o usuário (front-end). Quem de fato insere o usuário no "Contexto de Segurança"
*   (SecurityContext) a cada requisição futura é o seu JwtFilter, quando ele intercepta a requisição, 
*   valida a assinatura do JWT e diz: "opa, esse JWT é válido, vou colocar esse usuário no contexto de 
*   segurança para o Spring liberar a rota".
*/
public class JwtFilter {

}
