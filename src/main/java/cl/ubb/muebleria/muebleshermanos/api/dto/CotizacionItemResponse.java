package cl.ubb.muebleria.muebleshermanos.api.dto;

import java.math.BigDecimal;

public record CotizacionItemResponse(
        Long id,
        Long muebleId,
        String muebleNombre,
        Long variacionId,
        String variacionNombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
}
