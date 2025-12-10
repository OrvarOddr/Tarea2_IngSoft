package cl.ubb.muebleria.muebleshermanos.api.dto;

import cl.ubb.muebleria.muebleshermanos.domain.enums.EstadoMueble;
import cl.ubb.muebleria.muebleshermanos.domain.enums.Tamano;
import cl.ubb.muebleria.muebleshermanos.domain.enums.TipoMueble;
import java.math.BigDecimal;
import java.util.List;

public record MuebleResponse(
        Long id,
        String nombre,
        TipoMueble tipo,
        BigDecimal precioBase,
        Integer stock,
        EstadoMueble estado,
        Tamano tamano,
        String material,
        List<VariacionResponse> variaciones
) {
}
