package config;

import domain.AppUser;
import domain.Role;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import repository.AppUserRepository;

@Configuration
public class DataSeeder {

    @Bean
    ApplicationRunner seedUsers(AppUserRepository users, PasswordEncoder passwordEncoder) {
        return args -> {
            saveIfMissing(users, passwordEncoder, "admin", "admin123", Role.ADMIN);
            saveIfMissing(users, passwordEncoder, "manager", "manager123", Role.MANAGER);
            saveIfMissing(users, passwordEncoder, "procurement", "procurement123", Role.PROCRUEMENT);
            saveIfMissing(users, passwordEncoder, "finance", "finance123", Role.FINANCE);
        };
    }

    private void saveIfMissing(AppUserRepository users, PasswordEncoder passwordEncoder, String username, String password, Role role) {
        if (users.findByUsername(username).isPresent()) {
            return;
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        users.save(user);
    }
}
