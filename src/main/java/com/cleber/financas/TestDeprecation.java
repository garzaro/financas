package com.cleber.financas;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import java.lang.reflect.Method;
public class TestDeprecation {
    public static void main(String[] args) throws Exception {
        Method m = Argon2PasswordEncoder.class.getMethod("defaultsForSpringSecurity_v5_8");
        if (m.isAnnotationPresent(Deprecated.class)) {
            System.out.println("defaultsForSpringSecurity_v5_8 is deprecated");
        } else {
            System.out.println("defaultsForSpringSecurity_v5_8 is NOT deprecated");
        }
    }
}
