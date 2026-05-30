package easy.azul.api.repository;

import easy.azul.api.entity.Enum.StatusTicket;
import easy.azul.api.entity.TicketEstacionamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketEstacionamentoRepository extends JpaRepository<TicketEstacionamento, Long> {

    Page<TicketEstacionamento> findByVeiculo_Dono_IdUsuario(Long idUsuario, Pageable pageable);

    boolean existsByVeiculo_IdAndStatus(Long idVeiculo, StatusTicket status);

    boolean existsByZona_IdZonaEstacionamentoAndStatus(Long idZonaEstacionamento, StatusTicket status);

    long countByZona_IdZonaEstacionamentoAndStatus(Long idZonaEstacionamento, StatusTicket status);

    long countByZona_IdZonaEstacionamentoAndStatusIn(Long idZonaEstacionamento, Collection<StatusTicket> status);

    boolean existsByZona_IdZonaEstacionamentoAndStatusIn(Long idZonaEstacionamento, Collection<StatusTicket> status);

    List<TicketEstacionamento> findByStatusAndAtivoTrueAndInicioTicketLessThanEqual(StatusTicket status, LocalDateTime limite);

    Optional<TicketEstacionamento> findTop1ByVeiculo_IdAndStatusOrderByFimTicketDesc(Long idVeiculo, StatusTicket status);
}