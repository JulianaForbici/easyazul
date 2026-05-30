package easy.azul.api.service;

import easy.azul.api.dto.Zona.DadosAtualizacaoZona;
import easy.azul.api.dto.Zona.DadosCadastroZona;
import easy.azul.api.dto.Zona.DadosDetalhamentoZona;
import easy.azul.api.infra.exception.ValidacaoException;
import easy.azul.api.entity.Enum.StatusTicket;
import easy.azul.api.entity.Enum.StatusZona;
import easy.azul.api.entity.ZonaEstacionamento;
import easy.azul.api.repository.TicketEstacionamentoRepository;
import easy.azul.api.repository.ZonaEstacionamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("zonaEstacionamentoService")
public class ZonaEstacionamentoService {

    @Autowired
    private ZonaEstacionamentoRepository zonaEstacionamentoRepository;

    @Autowired
    private TicketEstacionamentoRepository ticketEstacionamentoRepository;

    @Autowired
    private MapaService mapaService;

    @Transactional
    public DadosDetalhamentoZona cadastrar(DadosCadastroZona dados) {
        String nomeCorreto = dados.nome().trim().replaceAll("\\s+", " ");

        if (zonaEstacionamentoRepository.existsByNomeIgnoreCase(nomeCorreto)) {
            throw new ValidacaoException("Zonas não podem ter nomes iguais");
        }

        if (!dados.horaInicio().isBefore(dados.horaFim())) {
            throw new ValidacaoException("Hora de início deve ser antes da hora do fim");
        }

        Double lat = dados.latitude();
        Double lon = dados.longitude();

        boolean temLatLon = lat != null && lon != null;
        boolean temEndereco = dados.endereco() != null && !dados.endereco().isBlank();

        if (!temLatLon) {
            if (!temEndereco) {
                throw new ValidacaoException("Informe latitude/longitude ou um endereço para localizar a zona");
            }
            var geo = mapaService.buscarPorEndereco(dados.endereco());
            lat = Double.valueOf(geo.getLat());
            lon = Double.valueOf(geo.getLon());
        }

        ZonaEstacionamento zona = new ZonaEstacionamento();
        zona.setNome(nomeCorreto);
        zona.setTarifa(dados.tarifa());

        if (dados.descricao() != null && !dados.descricao().isBlank()) {
            zona.setDescricao(dados.descricao().trim());
        } else {
            zona.setDescricao(null);
        }

        zona.setTempoMaximo(dados.tempoMaximo());
        zona.setHoraInicio(dados.horaInicio());
        zona.setHoraFim(dados.horaFim());

        zona.setLatitude(lat);
        zona.setLongitude(lon);

        if (dados.capacidadeVagas() == null || dados.capacidadeVagas() < 0) {
            throw new ValidacaoException("Capacidade de vagas é obrigatória e não pode ser negativa");
        }
        zona.setCapacidadeVagas(dados.capacidadeVagas());

        zona.setStatus(StatusZona.ATIVA);

        var salva = zonaEstacionamentoRepository.save(zona);
        return new DadosDetalhamentoZona(salva);
    }

    @Transactional
    public DadosDetalhamentoZona atualizar(Long id, DadosAtualizacaoZona dados) {
        ZonaEstacionamento zona = zonaEstacionamentoRepository.findById(id)
                .orElseThrow(() -> new ValidacaoException("Zona não encontrada"));

        if (zona.getStatus() == StatusZona.INATIVA) {
            throw new ValidacaoException("Zona inativa não pode ser atualizada");
        }

        if (dados.nome() != null && !dados.nome().isBlank()) {
            String novoNome = dados.nome().trim().replaceAll("\\s+", " ");
            if (!novoNome.equalsIgnoreCase(zona.getNome())
                    && zonaEstacionamentoRepository.existsByNomeIgnoreCase(novoNome)) {
                throw new ValidacaoException("Já existe uma zona de estacionamento com esse nome");
            }
            zona.setNome(novoNome);
        }

        if (dados.tarifa() != null) zona.setTarifa(dados.tarifa());

        if (dados.descricao() != null) {
            zona.setDescricao(dados.descricao().isBlank() ? null : dados.descricao().trim());
        }

        if (dados.tempoMaximo() != null) zona.setTempoMaximo(dados.tempoMaximo());
        if (dados.horaInicio() != null) zona.setHoraInicio(dados.horaInicio());
        if (dados.horaFim() != null) zona.setHoraFim(dados.horaFim());

        if (!zona.getHoraInicio().isBefore(zona.getHoraFim())) {
            throw new ValidacaoException("Hora de início deve ser antes da hora do fim");
        }

        if (dados.latitude() != null && dados.longitude() != null) {
            zona.setLatitude(dados.latitude());
            zona.setLongitude(dados.longitude());
        } else {
            boolean temEndereco = dados.endereco() != null && !dados.endereco().isBlank();
            if (temEndereco) {
                var geo = mapaService.buscarPorEndereco(dados.endereco());
                zona.setLatitude(Double.valueOf(geo.getLat()));
                zona.setLongitude(Double.valueOf(geo.getLon()));
            }
        }

        if (dados.capacidadeVagas() != null) {
            if (dados.capacidadeVagas() < 0) throw new ValidacaoException("Capacidade não pode ser negativa");
            zona.setCapacidadeVagas(dados.capacidadeVagas());
        }

        return new DadosDetalhamentoZona(zona);
    }

    @Transactional
    public void excluir(Long id) {
        ZonaEstacionamento zona = zonaEstacionamentoRepository.findById(id)
                .orElseThrow(() -> new ValidacaoException("Zona não encontrada"));

        if (zona.getStatus() == StatusZona.INATIVA) {
            throw new ValidacaoException("Zona já está inativa.");
        }

        boolean temAtivo = ticketEstacionamentoRepository
                .existsByZona_IdZonaEstacionamentoAndStatus(id, StatusTicket.ATIVO);

        if (temAtivo) {
            throw new ValidacaoException("Não é possível excluir. Há ticket ATIVO nessa zona");
        }

        zona.setStatus(StatusZona.INATIVA);
    }
}