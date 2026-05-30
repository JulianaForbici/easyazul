package easy.azul.api.repository;

import easy.azul.api.entity.Enum.StatusPagamento;
import easy.azul.api.entity.Pagamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    Page<Pagamento> findByTicket_IdTicket(Long idTicket, Pageable pageable);

    Page<Pagamento> findByUsuario_IdUsuario(Long idUsuario, Pageable pageable);

    boolean existsByTicket_IdTicketAndStatusIn(Long idTicket, List<StatusPagamento> status);

    boolean existsByTicket_IdTicketAndStatusIn(Long idTicket, Collection<StatusPagamento> status);
}