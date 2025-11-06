package cl.ubb.muebleria.muebleshermanos.domain.model;

import cl.ubb.muebleria.muebleshermanos.domain.enums.EstadoCotizacion;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cotizacion")
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCotizacion estado;

    @OneToMany(mappedBy = "cotizacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CotizacionItem> items = new ArrayList<>();

    public Cotizacion() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoCotizacion.CREADA;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public EstadoCotizacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoCotizacion estado) {
        this.estado = estado;
    }

    public List<CotizacionItem> getItems() {
        return items;
    }

    public void addItem(CotizacionItem item) {
        items.add(item);
        item.setCotizacion(this);
    }

    public void removeItem(CotizacionItem item) {
        items.remove(item);
        item.setCotizacion(null);
    }
}
