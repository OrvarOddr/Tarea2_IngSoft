package cl.ubb.muebleria.muebleshermanos.domain.strategy.impl;

import cl.ubb.muebleria.muebleshermanos.domain.enums.PriceStrategyType;
import cl.ubb.muebleria.muebleshermanos.domain.strategy.PriceCalculator;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class NoPriceAdjustmentStrategy implements PriceCalculator {

    @Override
    public PriceStrategyType getType() {
        return PriceStrategyType.NONE;
    }

    @Override
    public BigDecimal calculate(BigDecimal basePrice, BigDecimal adjustmentValue) {
        return basePrice;
    }
}
