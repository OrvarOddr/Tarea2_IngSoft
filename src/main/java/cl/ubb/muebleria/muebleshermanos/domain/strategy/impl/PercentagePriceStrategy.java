package cl.ubb.muebleria.muebleshermanos.domain.strategy.impl;

import cl.ubb.muebleria.muebleshermanos.domain.enums.PriceStrategyType;
import cl.ubb.muebleria.muebleshermanos.domain.strategy.PriceCalculator;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class PercentagePriceStrategy implements PriceCalculator {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final MathContext MATH_CONTEXT = new MathContext(6, RoundingMode.HALF_UP);

    @Override
    public PriceStrategyType getType() {
        return PriceStrategyType.PERCENTAGE;
    }

    @Override
    public BigDecimal calculate(BigDecimal basePrice, BigDecimal adjustmentValue) {
        BigDecimal percentage = adjustmentValue.divide(ONE_HUNDRED, MATH_CONTEXT);
        BigDecimal increment = basePrice.multiply(percentage, MATH_CONTEXT);
        return basePrice.add(increment).setScale(2, RoundingMode.HALF_UP);
    }
}
