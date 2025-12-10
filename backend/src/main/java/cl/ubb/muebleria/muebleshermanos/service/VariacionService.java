package cl.ubb.muebleria.muebleshermanos.service;

import cl.ubb.muebleria.muebleshermanos.api.dto.VariacionRequest;
import cl.ubb.muebleria.muebleshermanos.domain.model.Mueble;
import cl.ubb.muebleria.muebleshermanos.domain.model.Variacion;
import cl.ubb.muebleria.muebleshermanos.domain.repository.VariacionRepository;
import cl.ubb.muebleria.muebleshermanos.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VariacionService {

    private final MuebleService muebleService;
    private final VariacionRepository variacionRepository;

    public VariacionService(MuebleService muebleService, VariacionRepository variacionRepository) {
        this.muebleService = muebleService;
        this.variacionRepository = variacionRepository;
    }

    public List<Variacion> listarPorMueble(Long muebleId) {
        Mueble mueble = muebleService.obtener(muebleId);
        return variacionRepository.findByMueble(mueble);
    }

    public Variacion crear(Long muebleId, VariacionRequest request) {
        Mueble mueble = muebleService.obtener(muebleId);
        Variacion variacion = new Variacion();
        variacion.setNombre(request.nombre());
        variacion.setDescripcion(request.descripcion());
        variacion.setValorAjuste(request.valorAjuste());
        variacion.setPriceStrategyType(request.priceStrategyType());
        variacion.setActiva(request.activa());
        mueble.addVariacion(variacion);
        return variacionRepository.save(variacion);
    }

    public Variacion actualizar(Long muebleId, Long variacionId, VariacionRequest request) {
        Variacion variacion = obtenerVariacion(muebleId, variacionId);
        variacion.setNombre(request.nombre());
        variacion.setDescripcion(request.descripcion());
        variacion.setValorAjuste(request.valorAjuste());
        variacion.setPriceStrategyType(request.priceStrategyType());
        variacion.setActiva(request.activa());
        return variacionRepository.save(variacion);
    }

    public void eliminar(Long muebleId, Long variacionId) {
        Variacion variacion = obtenerVariacion(muebleId, variacionId);
        variacion.getMueble().removeVariacion(variacion);
        variacionRepository.delete(variacion);
    }

    private Variacion obtenerVariacion(Long muebleId, Long variacionId) {
        Variacion variacion = variacionRepository.findById(variacionId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la variación con id " + variacionId));
        if (!variacion.getMueble().getId().equals(muebleId)) {
            throw new ResourceNotFoundException("La variación " + variacionId + " no pertenece al mueble " + muebleId);
        }
        return variacion;
    }
}
