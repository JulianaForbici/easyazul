package easy.azul.api.service;

import easy.azul.api.dto.Pagamento.DadosDetalhamentoPagamento;
import easy.azul.api.entity.Pagamento;
import easy.azul.api.entity.Enum.StatusPagamento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.ArrayList;

@Service
public class PagamentoService {

    @Transactional
    public DadosDetalhamentoPagamento registrarPagamento(easy.azul.api.dto.Pagamento.DadosCadastroPagamento dados) {
        Pagamento pagamento = new Pagamento();
        pagamento.setStatus(StatusPagamento.PENDENTE);
        return new DadosDetalhamentoPagamento(pagamento);
    }

    public List<DadosDetalhamentoPagamento> listarTodos() {
        return new ArrayList<DadosDetalhamentoPagamento>();
    }

    public Page<DadosDetalhamentoPagamento> listarMeus(Pageable paginacao) {
        return Page.empty();
    }

    public Page<DadosDetalhamentoPagamento> listar(java.lang.Long id, Pageable paginacao) {
        return Page.empty();
    }

    public DadosDetalhamentoPagamento buscarPorId(Long id) {
        return new DadosDetalhamentoPagamento(new Pagamento());
    }

    @Transactional
    public void confirmarPagamento(Long id) {
    }

    @Transactional
    public void cancelarPagamento(Long id) {
    }

    @Transactional
    public void cancelar(java.lang.Long id, java.lang.String motivo) {
    }
}
