package cl.ubb.muebleria.muebleshermanos.api.dto;

import cl.ubb.muebleria.muebleshermanos.domain.enums.PriceStrategyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record VariacionRequest(
        @NotBlank(message = "El nombre de la variación es obligatorio")
        String nombre,

        String descripcion,

        @NotNull(message = "El valor de ajuste es obligatorio")
        BigDecimal valorAjuste,

        @NotNull(message = "Debe especificar la estrategia de precio")
        PriceStrategyType priceStrategyType,

        @NotNull(message = "Debe indicar si la variación está activa")
        Boolean activa
) {
}
