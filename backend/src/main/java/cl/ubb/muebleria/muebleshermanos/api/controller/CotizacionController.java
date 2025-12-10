package cl.ubb.muebleria.muebleshermanos.api.controller;

import cl.ubb.muebleria.muebleshermanos.api.dto.CotizacionRequest;
import cl.ubb.muebleria.muebleshermanos.api.dto.CotizacionResponse;
import cl.ubb.muebleria.muebleshermanos.api.mapper.CotizacionMapper;
import cl.ubb.muebleria.muebleshermanos.domain.enums.EstadoCotizacion;
import cl.ubb.muebleria.muebleshermanos.domain.model.Cotizacion;
import cl.ubb.muebleria.muebleshermanos.service.CotizacionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cotizaciones")
public class CotizacionController {

    private final CotizacionService cotizacionService;
    private final CotizacionMapper cotizacionMapper;

    public CotizacionController(CotizacionService cotizacionService, CotizacionMapper cotizacionMapper) {
        this.cotizacionService = cotizacionService;
        this.cotizacionMapper = cotizacionMapper;
    }

    @GetMapping
    public List<CotizacionResponse> listar(@RequestParam(name = "estado", required = false) EstadoCotizacion estado) {
        return cotizacionService.listar(estado)
                .stream()
                .map(cotizacionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public CotizacionResponse obtener(@PathVariable Long id) {
        Cotizacion cotizacion = cotizacionService.obtener(id);
        return cotizacionMapper.toResponse(cotizacion);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CotizacionResponse crear(@Valid @RequestBody CotizacionRequest request) {
        Cotizacion cotizacion = cotizacionService.crearCotizacion(request);
        return cotizacionMapper.toResponse(cotizacion);
    }

    @PostMapping("/{id}/confirmar")
    public CotizacionResponse confirmar(@PathVariable Long id) {
        Cotizacion cotizacion = cotizacionService.confirmarCotizacion(id);
        return cotizacionMapper.toResponse(cotizacion);
    }

    @PostMapping("/{id}/cancelar")
    public CotizacionResponse cancelar(@PathVariable Long id) {
        Cotizacion cotizacion = cotizacionService.cancelarCotizacion(id);
        return cotizacionMapper.toResponse(cotizacion);
    }
}
