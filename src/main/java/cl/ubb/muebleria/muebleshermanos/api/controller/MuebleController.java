package cl.ubb.muebleria.muebleshermanos.api.controller;

import cl.ubb.muebleria.muebleshermanos.api.dto.MuebleRequest;
import cl.ubb.muebleria.muebleshermanos.api.dto.MuebleResponse;
import cl.ubb.muebleria.muebleshermanos.api.mapper.CatalogMapper;
import cl.ubb.muebleria.muebleshermanos.domain.enums.EstadoMueble;
import cl.ubb.muebleria.muebleshermanos.domain.model.Mueble;
import cl.ubb.muebleria.muebleshermanos.service.MuebleService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/muebles")
public class MuebleController {

    private final MuebleService muebleService;
    private final CatalogMapper catalogMapper;

    public MuebleController(MuebleService muebleService, CatalogMapper catalogMapper) {
        this.muebleService = muebleService;
        this.catalogMapper = catalogMapper;
    }

    @GetMapping
    public List<MuebleResponse> listar(@RequestParam(name = "estado", required = false) EstadoMueble estado) {
        return muebleService.listar(estado)
                .stream()
                .map(catalogMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public MuebleResponse obtener(@PathVariable Long id) {
        Mueble mueble = muebleService.obtener(id);
        return catalogMapper.toResponse(mueble);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MuebleResponse crear(@Valid @RequestBody MuebleRequest request) {
        Mueble creado = muebleService.crear(request);
        return catalogMapper.toResponse(creado);
    }

    @PutMapping("/{id}")
    public MuebleResponse actualizar(@PathVariable Long id, @Valid @RequestBody MuebleRequest request) {
        Mueble actualizado = muebleService.actualizar(id, request);
        return catalogMapper.toResponse(actualizado);
    }

    @PatchMapping("/{id}/estado")
    public MuebleResponse cambiarEstado(@PathVariable Long id, @RequestParam("estado") EstadoMueble estado) {
        Mueble actualizado = muebleService.cambiarEstado(id, estado);
        return catalogMapper.toResponse(actualizado);
    }
}
