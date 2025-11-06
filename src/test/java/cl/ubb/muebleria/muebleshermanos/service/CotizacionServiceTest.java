package cl.ubb.muebleria.muebleshermanos.service;

import cl.ubb.muebleria.muebleshermanos.api.dto.CotizacionItemRequest;
import cl.ubb.muebleria.muebleshermanos.api.dto.CotizacionRequest;
import cl.ubb.muebleria.muebleshermanos.api.dto.MuebleRequest;
import cl.ubb.muebleria.muebleshermanos.api.dto.VariacionRequest;
import cl.ubb.muebleria.muebleshermanos.domain.enums.EstadoCotizacion;
import cl.ubb.muebleria.muebleshermanos.domain.enums.EstadoMueble;
import cl.ubb.muebleria.muebleshermanos.domain.enums.PriceStrategyType;
import cl.ubb.muebleria.muebleshermanos.domain.enums.Tamano;
import cl.ubb.muebleria.muebleshermanos.domain.enums.TipoMueble;
import cl.ubb.muebleria.muebleshermanos.domain.model.Cotizacion;
import cl.ubb.muebleria.muebleshermanos.domain.model.Mueble;
import cl.ubb.muebleria.muebleshermanos.domain.model.Variacion;
import cl.ubb.muebleria.muebleshermanos.exception.BusinessException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@Transactional
class CotizacionServiceTest {

    @Autowired
    private MuebleService muebleService;

    @Autowired
    private VariacionService variacionService;

    @Autowired
    private CotizacionService cotizacionService;

    @Test
    void creaCotizacionCalculandoPrecioConVariacion() {
        Mueble mueble = muebleService.crear(new MuebleRequest(
                "Silla Test",
                TipoMueble.SILLA,
                new BigDecimal("50000"),
                10,
                EstadoMueble.ACTIVO,
                Tamano.MEDIANO,
                "Madera"
        ));

        Variacion variacion = variacionService.crear(mueble.getId(), new VariacionRequest(
                "Barniz premium",
                "Aumento 15%",
                new BigDecimal("15"),
                PriceStrategyType.PERCENTAGE,
                true
        ));

        Cotizacion cotizacion = cotizacionService.crearCotizacion(new CotizacionRequest(List.of(
                new CotizacionItemRequest(mueble.getId(), variacion.getId(), 2)
        )));

        assertThat(cotizacion.getItems()).hasSize(1);
        assertThat(cotizacion.getItems().get(0).getPrecioUnitario()).isEqualByComparingTo("57500.00");
        assertThat(cotizacion.getItems().get(0).getSubtotal()).isEqualByComparingTo("115000.00");
        assertThat(cotizacion.getEstado()).isEqualTo(EstadoCotizacion.CREADA);
    }

    @Test
    void confirmarCotizacionDescuentaStock() {
        Mueble mueble = muebleService.crear(new MuebleRequest(
                "Mesa Test",
                TipoMueble.MESA,
                new BigDecimal("120000"),
                5,
                EstadoMueble.ACTIVO,
                Tamano.GRANDE,
                "Madera"
        ));

        Cotizacion cotizacion = cotizacionService.crearCotizacion(new CotizacionRequest(List.of(
                new CotizacionItemRequest(mueble.getId(), null, 2)
        )));

        Cotizacion confirmada = cotizacionService.confirmarCotizacion(cotizacion.getId());

        assertThat(confirmada.getEstado()).isEqualTo(EstadoCotizacion.CONFIRMADA);
        assertThat(muebleService.obtener(mueble.getId()).getStock()).isEqualTo(3);
    }

    @Test
    void confirmarCotizacionLanzaErrorSiNoHayStockSuficiente() {
        Mueble mueble = muebleService.crear(new MuebleRequest(
                "Estante Test",
                TipoMueble.ESTANTE,
                new BigDecimal("80000"),
                1,
                EstadoMueble.ACTIVO,
                Tamano.MEDIANO,
                "Metal"
        ));

        Cotizacion cotizacion = cotizacionService.crearCotizacion(new CotizacionRequest(List.of(
                new CotizacionItemRequest(mueble.getId(), null, 2)
        )));

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> cotizacionService.confirmarCotizacion(cotizacion.getId()))
                .withMessageContaining("Stock insuficiente");
    }
}
