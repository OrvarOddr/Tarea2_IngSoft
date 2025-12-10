package cl.ubb.muebleria.muebleshermanos.api.mapper;

import cl.ubb.muebleria.muebleshermanos.api.dto.CotizacionItemResponse;
import cl.ubb.muebleria.muebleshermanos.api.dto.CotizacionResponse;
import cl.ubb.muebleria.muebleshermanos.domain.model.Cotizacion;
import cl.ubb.muebleria.muebleshermanos.domain.model.CotizacionItem;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CotizacionMapper {

    public CotizacionResponse toResponse(Cotizacion cotizacion) {
        List<CotizacionItemResponse> itemResponses = cotizacion.getItems()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        BigDecimal total = itemResponses.stream()
                .map(CotizacionItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CotizacionResponse(
                cotizacion.getId(),
                cotizacion.getEstado(),
                cotizacion.getFechaCreacion(),
                total,
                itemResponses
        );
    }

    private CotizacionItemResponse toResponse(CotizacionItem item) {
        Long variacionId = item.getVariacion() != null ? item.getVariacion().getId() : null;
        String variacionNombre = item.getVariacion() != null ? item.getVariacion().getNombre() : null;

        return new CotizacionItemResponse(
                item.getId(),
                item.getMueble().getId(),
                item.getMueble().getNombre(),
                variacionId,
                variacionNombre,
                item.getCantidad(),
                item.getPrecioUnitario(),
                item.getSubtotal()
        );
    }
}
