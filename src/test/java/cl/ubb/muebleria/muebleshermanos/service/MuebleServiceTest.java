package cl.ubb.muebleria.muebleshermanos.service;

import cl.ubb.muebleria.muebleshermanos.api.dto.MuebleRequest;
import cl.ubb.muebleria.muebleshermanos.domain.enums.EstadoMueble;
import cl.ubb.muebleria.muebleshermanos.domain.enums.Tamano;
import cl.ubb.muebleria.muebleshermanos.domain.enums.TipoMueble;
import cl.ubb.muebleria.muebleshermanos.domain.model.Mueble;
import cl.ubb.muebleria.muebleshermanos.exception.BusinessException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@Transactional
class MuebleServiceTest {

    @Autowired
    private MuebleService muebleService;

    @Test
    void creaYActualizaMueble() {
        MuebleRequest request = new MuebleRequest(
                "Silla Gamer",
                TipoMueble.SILLA,
                new BigDecimal("95000"),
                7,
                EstadoMueble.ACTIVO,
                Tamano.GRANDE,
                "Metal y cuero"
        );

        Mueble mueble = muebleService.crear(request);
        assertThat(mueble.getId()).isNotNull();
        assertThat(mueble.getNombre()).isEqualTo("Silla Gamer");
        assertThat(mueble.getStock()).isEqualTo(7);

        MuebleRequest updateRequest = new MuebleRequest(
                "Silla Gamer Deluxe",
                TipoMueble.SILLA,
                new BigDecimal("110000"),
                10,
                EstadoMueble.ACTIVO,
                Tamano.GRANDE,
                "Metal y cuero premium"
        );

        Mueble actualizado = muebleService.actualizar(mueble.getId(), updateRequest);
        assertThat(actualizado.getNombre()).isEqualTo("Silla Gamer Deluxe");
        assertThat(actualizado.getPrecioBase()).isEqualByComparingTo("110000");
        assertThat(actualizado.getStock()).isEqualTo(10);
    }

    @Test
    void cambiarEstadoValidaTransicion() {
        Mueble mueble = muebleService.crear(new MuebleRequest(
                "Velador",
                TipoMueble.ESTANTE,
                new BigDecimal("35000"),
                3,
                EstadoMueble.ACTIVO,
                Tamano.PEQUENO,
                "Madera"
        ));

        Mueble inactivo = muebleService.cambiarEstado(mueble.getId(), EstadoMueble.INACTIVO);
        assertThat(inactivo.getEstado()).isEqualTo(EstadoMueble.INACTIVO);

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> muebleService.cambiarEstado(mueble.getId(), EstadoMueble.INACTIVO))
                .withMessageContaining("ya se encuentra en estado");
    }
}
