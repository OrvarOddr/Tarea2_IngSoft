package cl.ubb.muebleria.muebleshermanos.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CotizacionItemRequest(
        @NotNull(message = "Debe indicar el mueble")
        Long muebleId,

        Long variacionId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima es 1")
        Integer cantidad
) {
}
