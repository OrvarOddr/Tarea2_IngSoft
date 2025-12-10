package cl.ubb.muebleria.muebleshermanos.api.dto;

import cl.ubb.muebleria.muebleshermanos.domain.enums.EstadoCotizacion;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CotizacionResponse(
        Long id,
        EstadoCotizacion estado,
        LocalDateTime fechaCreacion,
        BigDecimal total,
        List<CotizacionItemResponse> items
) {
}
