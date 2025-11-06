package cl.ubb.muebleria.muebleshermanos.api.controller;

import cl.ubb.muebleria.muebleshermanos.api.dto.VariacionRequest;
import cl.ubb.muebleria.muebleshermanos.api.dto.VariacionResponse;
import cl.ubb.muebleria.muebleshermanos.api.mapper.CatalogMapper;
import cl.ubb.muebleria.muebleshermanos.service.VariacionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/muebles/{muebleId}/variaciones")
public class VariacionController {

    private final VariacionService variacionService;
    private final CatalogMapper catalogMapper;

    public VariacionController(VariacionService variacionService, CatalogMapper catalogMapper) {
        this.variacionService = variacionService;
        this.catalogMapper = catalogMapper;
    }

    @GetMapping
    public List<VariacionResponse> listar(@PathVariable Long muebleId) {
        return variacionService.listarPorMueble(muebleId)
                .stream()
                .map(catalogMapper::toResponse)
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VariacionResponse crear(@PathVariable Long muebleId, @Valid @RequestBody VariacionRequest request) {
        return catalogMapper.toResponse(variacionService.crear(muebleId, request));
    }

    @PutMapping("/{variacionId}")
    public VariacionResponse actualizar(@PathVariable Long muebleId,
                                        @PathVariable Long variacionId,
                                        @Valid @RequestBody VariacionRequest request) {
        return catalogMapper.toResponse(variacionService.actualizar(muebleId, variacionId, request));
    }

    @DeleteMapping("/{variacionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long muebleId, @PathVariable Long variacionId) {
        variacionService.eliminar(muebleId, variacionId);
    }
}
