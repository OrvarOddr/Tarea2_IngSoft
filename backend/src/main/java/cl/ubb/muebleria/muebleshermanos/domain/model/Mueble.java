package cl.ubb.muebleria.muebleshermanos.domain.model;

import cl.ubb.muebleria.muebleshermanos.domain.enums.EstadoMueble;
import cl.ubb.muebleria.muebleshermanos.domain.enums.Tamano;
import cl.ubb.muebleria.muebleshermanos.domain.enums.TipoMueble;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mueble")
public class Mueble {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_mueble", nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMueble tipo;

    @Column(name = "precio_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBase;

    @Column(nullable = false)
    private Integer stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMueble estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tamano tamano;

    @Column(nullable = false)
    private String material;

    @OneToMany(mappedBy = "mueble", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Variacion> variaciones = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoMueble getTipo() {
        return tipo;
    }

    public void setTipo(TipoMueble tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public EstadoMueble getEstado() {
        return estado;
    }

    public void setEstado(EstadoMueble estado) {
        this.estado = estado;
    }

    public Tamano getTamano() {
        return tamano;
    }

    public void setTamano(Tamano tamano) {
        this.tamano = tamano;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public List<Variacion> getVariaciones() {
        return variaciones;
    }

    public void addVariacion(Variacion variacion) {
        variaciones.add(variacion);
        variacion.setMueble(this);
    }

    public void removeVariacion(Variacion variacion) {
        variaciones.remove(variacion);
        variacion.setMueble(null);
    }
}
