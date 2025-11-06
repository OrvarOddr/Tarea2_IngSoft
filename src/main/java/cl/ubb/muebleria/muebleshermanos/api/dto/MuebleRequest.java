package cl.ubb.muebleria.muebleshermanos.api.dto;

import cl.ubb.muebleria.muebleshermanos.domain.enums.EstadoMueble;
import cl.ubb.muebleria.muebleshermanos.domain.enums.Tamano;
import cl.ubb.muebleria.muebleshermanos.domain.enums.TipoMueble;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record MuebleRequest(
        @NotBlank(message = "El nombre del mueble es obligatorio")
        String nombre,

        @NotNull(message = "El tipo de mueble es obligatorio")
        TipoMueble tipo,

        @NotNull(message = "El precio base es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El precio base debe ser mayor que cero")
        BigDecimal precioBase,

        @NotNull(message = "El stock es obligatorio")
        @PositiveOrZero(message = "El stock no puede ser negativo")
        Integer stock,

        @NotNull(message = "El estado del mueble es obligatorio")
        EstadoMueble estado,

        @NotNull(message = "El tamaño es obligatorio")
        Tamano tamano,

        @NotBlank(message = "El material es obligatorio")
        String material
) {
}
