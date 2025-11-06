package cl.ubb.muebleria.muebleshermanos.domain.strategy;

import cl.ubb.muebleria.muebleshermanos.domain.enums.PriceStrategyType;
import java.math.BigDecimal;

public interface PriceCalculator {

    PriceStrategyType getType();

    BigDecimal calculate(BigDecimal basePrice, BigDecimal adjustmentValue);
}
