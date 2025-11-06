package cl.ubb.muebleria.muebleshermanos.api.mapper;

import cl.ubb.muebleria.muebleshermanos.api.dto.MuebleResponse;
import cl.ubb.muebleria.muebleshermanos.api.dto.VariacionResponse;
import cl.ubb.muebleria.muebleshermanos.domain.model.Mueble;
import cl.ubb.muebleria.muebleshermanos.domain.model.Variacion;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CatalogMapper {

    public MuebleResponse toResponse(Mueble mueble) {
        List<VariacionResponse> variaciones = mueble.getVariaciones()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new MuebleResponse(
                mueble.getId(),
                mueble.getNombre(),
                mueble.getTipo(),
                mueble.getPrecioBase(),
                mueble.getStock(),
                mueble.getEstado(),
                mueble.getTamano(),
                mueble.getMaterial(),
                variaciones
        );
    }

    public VariacionResponse toResponse(Variacion variacion) {
        return new VariacionResponse(
                variacion.getId(),
                variacion.getNombre(),
                variacion.getDescripcion(),
                variacion.getValorAjuste(),
                variacion.getPriceStrategyType(),
                variacion.isActiva()
        );
    }
}
