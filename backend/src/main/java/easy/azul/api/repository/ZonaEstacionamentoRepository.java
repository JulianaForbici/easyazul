package easy.azul.api.repository;

import easy.azul.api.entity.Enum.StatusZona;
import easy.azul.api.entity.ZonaEstacionamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZonaEstacionamentoRepository extends JpaRepository<ZonaEstacionamento, Long> {
    boolean existsByNomeIgnoreCase(String nome);

    Page<ZonaEstacionamento> findByStatus(StatusZona status, Pageable pageable);

    List<ZonaEstacionamento> findByStatus(StatusZona status);
}