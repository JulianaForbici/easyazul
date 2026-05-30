package easy.azul.api.infra.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GerarHash {
    public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder().encode("Admin@123"));
    }
}