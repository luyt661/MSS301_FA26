package fu.de181018.user.repository;

import fu.de181018.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    boolean existsByUsername(String username);

    boolean existsByUsernameAndUserIdNot(String username, Long userId);

    Optional<User> findByUsername(String username);
}
