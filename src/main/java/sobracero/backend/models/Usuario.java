package sobracero.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCuenta estado;

    @Column(name = "debe_cambiar_password", nullable = false)
    private Boolean debeCambiarPassword = false;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(nullable = false)
    private String telefono;

    private String direccion;

    @Column(name = "documento_url")
    private String documentoUrl;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "rut")
    private String rut;

    @Column(name = "comuna")
    private String comuna;

    @Column(name = "region")
    private String region;

    @Column(name = "nombre_responsable")
    private String nombreResponsable;

    @Column(name = "cargo_responsable")
    private String cargoResponsable;

    @CreationTimestamp
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "codigo_otp", length = 6)
    private String codigoOtp;

    @Column(name = "fecha_expiracion_otp")
    private LocalDateTime fechaExpiracionOtp;

    @Column(name = "token_recuperacion")
    private String tokenRecuperacion;

    @Column(name = "fecha_expiracion_token")
    private LocalDateTime fechaExpiracionToken;

    // Método que se ejecuta justo antes de insertar el registro en la BD por primera vez
    @PrePersist
    protected void onCreate() {
        if (this.estado == null) {
            this.estado = EstadoCuenta.PENDIENTE;
        }
        // Aseguramos que nunca vaya nulo a la base de datos
        if (this.debeCambiarPassword == null) {
            this.debeCambiarPassword = false;
        }
    }
}