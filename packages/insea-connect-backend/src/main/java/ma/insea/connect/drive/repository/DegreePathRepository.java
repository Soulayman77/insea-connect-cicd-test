package ma.insea.connect.drive.repository;

import ma.insea.connect.user.model.DegreePath;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DegreePathRepository extends JpaRepository<DegreePath, Long> {

    Optional<DegreePath> findByCycleAndMajorAndPathYear(String string, String string2, int i);
}