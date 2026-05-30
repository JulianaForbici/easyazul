package easy.azul.api.repository;

import easy.azul.api.entity.Enum.StatusVeiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import easy.azul.api.entity.Veiculo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    boolean existsByPlaca(String placa);

    Page<Veiculo> findByDonoIdUsuario(Long idUsuario, Pageable pageable);

    Page<Veiculo> findByDonoIdUsuarioAndStatus(Long idUsuario, StatusVeiculo status, Pageable pageable);

}
