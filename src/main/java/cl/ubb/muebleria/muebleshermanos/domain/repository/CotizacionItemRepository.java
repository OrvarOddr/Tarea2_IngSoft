package cl.ubb.muebleria.muebleshermanos.domain.repository;

import cl.ubb.muebleria.muebleshermanos.domain.model.CotizacionItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CotizacionItemRepository extends JpaRepository<CotizacionItem, Long> {
}
