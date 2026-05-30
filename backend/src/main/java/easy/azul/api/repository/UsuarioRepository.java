package easy.azul.api.repository;

import easy.azul.api.entity.Enum.StatusUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import easy.azul.api.entity.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByEmail(String email);

    Page<Usuario> findAllByStatus(StatusUsuario status, Pageable pageable);

    Optional<Usuario> findByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsByCnpj(String cnpj);

    boolean existsByTelefone(String telefone);
}