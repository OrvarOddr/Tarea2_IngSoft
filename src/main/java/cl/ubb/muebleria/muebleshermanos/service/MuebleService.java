package cl.ubb.muebleria.muebleshermanos.service;

import cl.ubb.muebleria.muebleshermanos.api.dto.MuebleRequest;
import cl.ubb.muebleria.muebleshermanos.domain.enums.EstadoMueble;
import cl.ubb.muebleria.muebleshermanos.domain.model.Mueble;
import cl.ubb.muebleria.muebleshermanos.domain.repository.MuebleRepository;
import cl.ubb.muebleria.muebleshermanos.exception.BusinessException;
import cl.ubb.muebleria.muebleshermanos.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MuebleService {

    private final MuebleRepository muebleRepository;

    public MuebleService(MuebleRepository muebleRepository) {
        this.muebleRepository = muebleRepository;
    }

    public List<Mueble> listar(EstadoMueble estado) {
        if (estado != null) {
            return muebleRepository.findByEstado(estado);
        }
        return muebleRepository.findAll();
    }

    public Mueble obtener(Long id) {
        return muebleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el mueble con id " + id));
    }

    public Mueble crear(MuebleRequest request) {
        Mueble mueble = new Mueble();
        actualizarDatosMueble(mueble, request);
        return muebleRepository.save(mueble);
    }

    public Mueble actualizar(Long id, MuebleRequest request) {
        Mueble existente = obtener(id);
        actualizarDatosMueble(existente, request);
        return existente;
    }

    public Mueble cambiarEstado(Long id, EstadoMueble nuevoEstado) {
        Mueble mueble = obtener(id);
        if (mueble.getEstado() == nuevoEstado) {
            throw new BusinessException("El mueble ya se encuentra en estado " + nuevoEstado);
        }
        mueble.setEstado(nuevoEstado);
        return mueble;
    }

    private void actualizarDatosMueble(Mueble mueble, MuebleRequest request) {
        mueble.setNombre(request.nombre());
        mueble.setTipo(request.tipo());
        mueble.setPrecioBase(request.precioBase());
        mueble.setStock(request.stock());
        mueble.setEstado(request.estado());
        mueble.setTamano(request.tamano());
        mueble.setMaterial(request.material());
    }
}
