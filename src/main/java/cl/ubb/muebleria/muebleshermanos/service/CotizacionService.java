package cl.ubb.muebleria.muebleshermanos.service;

import cl.ubb.muebleria.muebleshermanos.api.dto.CotizacionItemRequest;
import cl.ubb.muebleria.muebleshermanos.api.dto.CotizacionRequest;
import cl.ubb.muebleria.muebleshermanos.domain.enums.EstadoCotizacion;
import cl.ubb.muebleria.muebleshermanos.domain.enums.EstadoMueble;
import cl.ubb.muebleria.muebleshermanos.domain.enums.PriceStrategyType;
import cl.ubb.muebleria.muebleshermanos.domain.model.Cotizacion;
import cl.ubb.muebleria.muebleshermanos.domain.model.CotizacionItem;
import cl.ubb.muebleria.muebleshermanos.domain.model.Mueble;
import cl.ubb.muebleria.muebleshermanos.domain.model.Variacion;
import cl.ubb.muebleria.muebleshermanos.domain.repository.CotizacionRepository;
import cl.ubb.muebleria.muebleshermanos.domain.repository.MuebleRepository;
import cl.ubb.muebleria.muebleshermanos.domain.repository.VariacionRepository;
import cl.ubb.muebleria.muebleshermanos.domain.strategy.PriceCalculator;
import cl.ubb.muebleria.muebleshermanos.domain.strategy.PriceCalculatorFactory;
import cl.ubb.muebleria.muebleshermanos.exception.BusinessException;
import cl.ubb.muebleria.muebleshermanos.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CotizacionService {

    private final CotizacionRepository cotizacionRepository;
    private final MuebleRepository muebleRepository;
    private final VariacionRepository variacionRepository;
    private final PriceCalculatorFactory priceCalculatorFactory;

    public CotizacionService(CotizacionRepository cotizacionRepository,
                             MuebleRepository muebleRepository,
                             VariacionRepository variacionRepository,
                             PriceCalculatorFactory priceCalculatorFactory) {
        this.cotizacionRepository = cotizacionRepository;
        this.muebleRepository = muebleRepository;
        this.variacionRepository = variacionRepository;
        this.priceCalculatorFactory = priceCalculatorFactory;
    }

    public List<Cotizacion> listar(EstadoCotizacion estado) {
        if (estado != null) {
            return cotizacionRepository.findByEstado(estado);
        }
        return cotizacionRepository.findAll();
    }

    public Cotizacion obtener(Long id) {
        return cotizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la cotización con id " + id));
    }

    public Cotizacion crearCotizacion(CotizacionRequest request) {
        if (request.items().isEmpty()) {
            throw new BusinessException("La cotización debe contener al menos un mueble");
        }

        Cotizacion cotizacion = new Cotizacion();

        for (CotizacionItemRequest itemRequest : request.items()) {
            CotizacionItem item = construirItem(itemRequest);
            cotizacion.addItem(item);
        }

        if (cotizacion.getItems().isEmpty()) {
            throw new BusinessException("No fue posible crear la cotización sin items válidos");
        }

        return cotizacionRepository.save(cotizacion);
    }

    public Cotizacion confirmarCotizacion(Long id) {
        Cotizacion cotizacion = obtener(id);
        if (cotizacion.getEstado() != EstadoCotizacion.CREADA) {
            throw new BusinessException("Solo se pueden confirmar cotizaciones en estado CREADA");
        }

        cotizacion.getItems().forEach(item -> {
            Mueble mueble = muebleRepository.findById(item.getMueble().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("No existe mueble con id " + item.getMueble().getId()));

            if (mueble.getEstado() != EstadoMueble.ACTIVO) {
                throw new BusinessException("El mueble " + mueble.getNombre() + " no está disponible para confirmar la venta");
            }

            if (mueble.getStock() < item.getCantidad()) {
                throw new BusinessException("Stock insuficiente para el mueble " + mueble.getNombre());
            }

            mueble.setStock(mueble.getStock() - item.getCantidad());
            muebleRepository.save(mueble);
            item.setMueble(mueble);
        });

        cotizacion.setEstado(EstadoCotizacion.CONFIRMADA);
        return cotizacion;
    }

    public Cotizacion cancelarCotizacion(Long id) {
        Cotizacion cotizacion = obtener(id);
        if (cotizacion.getEstado() == EstadoCotizacion.CANCELADA) {
            throw new BusinessException("La cotización ya está cancelada");
        }
        if (cotizacion.getEstado() == EstadoCotizacion.CONFIRMADA) {
            throw new BusinessException("No es posible cancelar cotizaciones confirmadas");
        }
        cotizacion.setEstado(EstadoCotizacion.CANCELADA);
        return cotizacion;
    }

    private CotizacionItem construirItem(CotizacionItemRequest request) {
        Mueble mueble = muebleRepository.findById(request.muebleId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe mueble con id " + request.muebleId()));

        if (mueble.getEstado() != EstadoMueble.ACTIVO) {
            throw new BusinessException("El mueble " + mueble.getNombre() + " no está disponible para cotizar");
        }

        Variacion variacion = null;
        PriceStrategyType strategyType = PriceStrategyType.NONE;
        BigDecimal ajuste = BigDecimal.ZERO;

        if (request.variacionId() != null) {
            variacion = variacionRepository.findById(request.variacionId())
                    .orElseThrow(() -> new ResourceNotFoundException("No existe variación con id " + request.variacionId()));
            if (!variacion.getMueble().getId().equals(mueble.getId())) {
                throw new BusinessException("La variación " + request.variacionId() + " no pertenece al mueble " + mueble.getNombre());
            }
            if (!variacion.isActiva()) {
                throw new BusinessException("La variación seleccionada no está activa");
            }
            strategyType = variacion.getPriceStrategyType();
            ajuste = variacion.getValorAjuste();
        }

        if (request.cantidad() <= 0) {
            throw new BusinessException("La cantidad debe ser mayor a cero");
        }

        PriceCalculator calculator = priceCalculatorFactory.getCalculator(strategyType);
        BigDecimal precioUnitario = calculator
                .calculate(mueble.getPrecioBase(), ajuste)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal subtotal = precioUnitario
                .multiply(BigDecimal.valueOf(request.cantidad()))
                .setScale(2, RoundingMode.HALF_UP);

        CotizacionItem item = new CotizacionItem();
        item.setMueble(mueble);
        item.setVariacion(variacion);
        item.setCantidad(request.cantidad());
        item.setPrecioUnitario(precioUnitario);
        item.setSubtotal(subtotal);

        return item;
    }
}
