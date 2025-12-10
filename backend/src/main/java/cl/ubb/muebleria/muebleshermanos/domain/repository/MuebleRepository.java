package cl.ubb.muebleria.muebleshermanos.domain.repository;

import cl.ubb.muebleria.muebleshermanos.domain.enums.EstadoMueble;
import cl.ubb.muebleria.muebleshermanos.domain.model.Mueble;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MuebleRepository extends JpaRepository<Mueble, Long> {

    List<Mueble> findByEstado(EstadoMueble estado);
}
