package cl.ubb.muebleria.muebleshermanos.domain.repository;

import cl.ubb.muebleria.muebleshermanos.domain.model.Mueble;
import cl.ubb.muebleria.muebleshermanos.domain.model.Variacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariacionRepository extends JpaRepository<Variacion, Long> {

    List<Variacion> findByMueble(Mueble mueble);

    List<Variacion> findByMuebleAndActivaTrue(Mueble mueble);
}
