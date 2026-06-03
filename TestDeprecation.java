import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;

public class TestDeprecation {
    public static void main(String[] args) throws Exception {
        System.out.println("setPasswordEncoder: " + DaoAuthenticationProvider.class.getMethod("setPasswordEncoder", PasswordEncoder.class).isAnnotationPresent(Deprecated.class));
        System.out.println("setUserDetailsService: " + DaoAuthenticationProvider.class.getMethod("setUserDetailsService", UserDetailsService.class).isAnnotationPresent(Deprecated.class));
    }
}
