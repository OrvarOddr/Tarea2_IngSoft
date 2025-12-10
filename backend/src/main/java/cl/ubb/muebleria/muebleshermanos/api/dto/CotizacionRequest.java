package cl.ubb.muebleria.muebleshermanos.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CotizacionRequest(
        @NotEmpty(message = "Debe registrar al menos un mueble en la cotización")
        List<@Valid CotizacionItemRequest> items
) {
}
