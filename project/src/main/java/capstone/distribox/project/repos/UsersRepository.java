package capstone.distribox.project.repos;

import capstone.distribox.project.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<User, String> {

	Optional<User> findById(String id);
}
