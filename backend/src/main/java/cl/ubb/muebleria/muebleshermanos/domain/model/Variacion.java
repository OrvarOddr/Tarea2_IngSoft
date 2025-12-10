package cl.ubb.muebleria.muebleshermanos.domain.model;

import cl.ubb.muebleria.muebleshermanos.domain.enums.PriceStrategyType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "variacion")
public class Variacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mueble_id", nullable = false)
    private Mueble mueble;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "valor_ajuste", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorAjuste;

    @Enumerated(EnumType.STRING)
    @Column(name = "estrategia_precio", nullable = false)
    private PriceStrategyType priceStrategyType;

    @Column(nullable = false)
    private boolean activa = true;

    public Long getId() {
        return id;
    }

    public Mueble getMueble() {
        return mueble;
    }

    public void setMueble(Mueble mueble) {
        this.mueble = mueble;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getValorAjuste() {
        return valorAjuste;
    }

    public void setValorAjuste(BigDecimal valorAjuste) {
        this.valorAjuste = valorAjuste;
    }

    public PriceStrategyType getPriceStrategyType() {
        return priceStrategyType;
    }

    public void setPriceStrategyType(PriceStrategyType priceStrategyType) {
        this.priceStrategyType = priceStrategyType;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}
