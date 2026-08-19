package com.cleber.financas.security.crypto;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * PasswordEncoder sobre a lib argon2-jvm (de.mkammerer), variante Argon2id.
 * Decisão explícita: NÃO usar o Argon2PasswordEncoder do Spring Security,
 * que embrulha a implementação Argon2 do Bouncy Castle — libs diferentes,
 * parâmetros/formatos de encoding não são intercambiáveis. Se o projeto já
 * tem hashes gravados com uma delas, trocar de lib invalida logins
 * existentes (todo usuário precisaria resetar senha) — não é refactor
 * transparente.
 *
 * argon2.hash(...) já retorna a string encoded no formato
 * $argon2id$v=19$m=...,t=...,p=...$salt$hash — parâmetros embutidos,
 * então matches() não precisa (nem deve) receber os parâmetros de novo.
 */
public class Argon2JvmPasswordEncoder implements PasswordEncoder {

    // Ponto de partida OWASP para Argon2id (server-side, sem hardware dedicado).
    // NÃO são valores definitivos — meça o tempo de hash médio sob a carga
    // real do seu servidor (alvo comum: 250ms–1s por hash) e ajuste. Memory
    // alta é a defesa mais forte contra GPU cracking; não reduza sem motivo.
    private static final int SALT_LENGTH = 16;   // bytes
    private static final int HASH_LENGTH = 32;   // bytes
    private static final int PARALLELISM = 1;
    private static final int MEMORY_KIB = 1 << 16; // 64 MiB
    private static final int ITERATIONS = 3;

    private final Argon2 argon2 = Argon2Factory.create(
            Argon2Factory.Argon2Types.ARGON2id, SALT_LENGTH, HASH_LENGTH);

    @Override
    public String encode(CharSequence rawPassword) {
        char[] password = toCharArray(rawPassword);
        try {
            return argon2.hash(ITERATIONS, MEMORY_KIB, PARALLELISM, password);
        } finally {
            argon2.wipeArray(password); // zera a senha em memória — não deixa em heap até o GC
        }
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        char[] password = toCharArray(rawPassword);
        try {
            return argon2.verify(encodedPassword, password);
        } finally {
            argon2.wipeArray(password);
        }
    }

    private char[] toCharArray(CharSequence cs) {
        char[] arr = new char[cs.length()];
        for (int i = 0; i < cs.length(); i++) {
            arr[i] = cs.charAt(i);
        }
        return arr;
    }
}
