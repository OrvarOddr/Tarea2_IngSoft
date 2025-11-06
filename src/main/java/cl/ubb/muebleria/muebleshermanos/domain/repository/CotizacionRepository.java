package cl.ubb.muebleria.muebleshermanos.domain.repository;

import cl.ubb.muebleria.muebleshermanos.domain.enums.EstadoCotizacion;
import cl.ubb.muebleria.muebleshermanos.domain.model.Cotizacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {

    List<Cotizacion> findByEstado(EstadoCotizacion estado);
}
