package cl.ubb.muebleria.muebleshermanos.api.dto;

import cl.ubb.muebleria.muebleshermanos.domain.enums.PriceStrategyType;
import java.math.BigDecimal;

public record VariacionResponse(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal valorAjuste,
        PriceStrategyType priceStrategyType,
        boolean activa
) {
}
