package cl.ubb.muebleria.muebleshermanos.domain.strategy;

import cl.ubb.muebleria.muebleshermanos.domain.enums.PriceStrategyType;
import cl.ubb.muebleria.muebleshermanos.domain.strategy.impl.AdditivePriceStrategy;
import cl.ubb.muebleria.muebleshermanos.domain.strategy.impl.NoPriceAdjustmentStrategy;
import cl.ubb.muebleria.muebleshermanos.domain.strategy.impl.PercentagePriceStrategy;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PriceCalculatorFactoryTest {

    private PriceCalculatorFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PriceCalculatorFactory(List.of(
                new NoPriceAdjustmentStrategy(),
                new AdditivePriceStrategy(),
                new PercentagePriceStrategy()
        ));
    }

    @Test
    void devuelveEstrategiaAditiva() {
        PriceCalculator calculator = factory.getCalculator(PriceStrategyType.ADDITIVE);
        BigDecimal resultado = calculator.calculate(new BigDecimal("1000"), new BigDecimal("250"));
        assertThat(resultado).isEqualByComparingTo("1250");
    }

    @Test
    void devuelveEstrategiaPorcentaje() {
        PriceCalculator calculator = factory.getCalculator(PriceStrategyType.PERCENTAGE);
        BigDecimal resultado = calculator.calculate(new BigDecimal("1000"), new BigDecimal("10"));
        assertThat(resultado).isEqualByComparingTo("1100.00");
    }

    @Test
    void usaEstrategiaPorDefectoCuandoNoExiste() {
        PriceCalculator calculator = factory.getCalculator(PriceStrategyType.NONE);
        BigDecimal resultado = calculator.calculate(new BigDecimal("1000"), new BigDecimal("999"));
        assertThat(resultado).isEqualByComparingTo("1000");
    }
}
