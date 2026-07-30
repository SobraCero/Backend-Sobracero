package sobracero.backend.services.email;

public interface EmailService {
    void enviarOtp(String destinatario, String otp);
    void enviarEnlaceRecuperacion(String destinatario, String token);
    void enviarContrasenaProvisoria(String destinatario, String contrasenaProvisoria);
    void enviarConfirmacionEliminacionCuenta(String destinatario);
    void enviarEliminacionCuentaPorAdmin(String destinatario);
    void enviarNotificacionEliminacionDonacion(String destinatario, String nombreDonacion);
    void enviarCodigoReserva(String destinatarioOng, String destinatarioDonante, String codigo, String nombreDonacion);
}
