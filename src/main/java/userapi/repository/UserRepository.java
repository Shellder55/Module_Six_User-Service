package userapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import userapi.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}