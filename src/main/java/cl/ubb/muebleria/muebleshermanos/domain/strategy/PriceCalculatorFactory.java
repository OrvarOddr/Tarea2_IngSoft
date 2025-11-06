package cl.ubb.muebleria.muebleshermanos.domain.strategy;

import cl.ubb.muebleria.muebleshermanos.domain.enums.PriceStrategyType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PriceCalculatorFactory {

    private final Map<PriceStrategyType, PriceCalculator> calculatorMap;

    public PriceCalculatorFactory(List<PriceCalculator> calculators) {
        this.calculatorMap = calculators.stream()
                .collect(Collectors.toMap(
                        PriceCalculator::getType,
                        Function.identity(),
                        (first, second) -> first,
                        () -> new EnumMap<>(PriceStrategyType.class)));
    }

    public PriceCalculator getCalculator(PriceStrategyType type) {
        return calculatorMap.getOrDefault(type, calculatorMap.get(PriceStrategyType.NONE));
    }
}
