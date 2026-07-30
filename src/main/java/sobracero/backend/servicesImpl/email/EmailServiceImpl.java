package sobracero.backend.servicesImpl.email;

import sobracero.backend.services.email.EmailService;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @org.springframework.beans.factory.annotation.Value("${FRONTEND_URL:http://localhost:5173}")
    private String frontendUrl;

    public EmailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void enviarOtp(String destinatario, String otp) {
        try {
            jakarta.mail.internet.MimeMessage mensaje = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mensaje, true, "UTF-8");
            
            helper.setFrom("no-reply@sobracero.com");
            helper.setTo(destinatario);
            helper.setSubject("Tu código de verificación OTP - Sobra Cero");
            
            Context context = new Context();
            context.setVariable("otp", otp);
            
            String contenidoHtml = templateEngine.process("otp", context);
            helper.setText(contenidoHtml, true);
            
            mailSender.send(mensaje);
        } catch (Exception e) {
            System.err.println("No se pudo enviar el correo a " + destinatario + ": " + e.getMessage());
            System.out.println("======> CÓDIGO OTP PARA " + destinatario + " ES: " + otp + " <======");
        }
    }

    @Override
    public void enviarEnlaceRecuperacion(String destinatario, String token) {
        try {
            jakarta.mail.internet.MimeMessage mensaje = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mensaje, true, "UTF-8");
            
            helper.setFrom("no-reply@sobracero.com");
            helper.setTo(destinatario);
            helper.setSubject("Recuperación de Contraseña - Sobra Cero");
            
            String urlRecuperacion = frontendUrl + "/restablecer-password?token=" + token;
            
            Context context = new Context();
            context.setVariable("enlaceRecuperacion", urlRecuperacion);
            
            String contenidoHtml = templateEngine.process("recuperar-password", context);
            
            helper.setText(contenidoHtml, true); // true indica que el contenido es HTML
            
            mailSender.send(mensaje);
        } catch (Exception e) {
            System.err.println("No se pudo enviar el correo de recuperación a " + destinatario + ": " + e.getMessage());
            System.out.println("======> ENLACE DE RECUPERACIÓN PARA " + destinatario + " ES: " + frontendUrl + "/restablecer-password?token=" + token + " <======");
        }
    }

    @Override
    public void enviarContrasenaProvisoria(String destinatario, String contrasenaProvisoria) {
        try {
            jakarta.mail.internet.MimeMessage mensaje = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mensaje, true, "UTF-8");
            
            helper.setFrom("no-reply@sobracero.com");
            helper.setTo(destinatario);
            helper.setSubject("Tu cuenta ha sido aprobada - Sobra Cero");
            
            Context context = new Context();
            context.setVariable("contrasenaProvisoria", contrasenaProvisoria);
            context.setVariable("loginUrl", frontendUrl + "/login");
            
            String contenidoHtml = templateEngine.process("contrasena-provisoria", context);
            helper.setText(contenidoHtml, true);
            
            mailSender.send(mensaje);
        } catch (Exception e) {
            System.err.println("No se pudo enviar el correo de contraseña provisoria a " + destinatario + ": " + e.getMessage());
        }
    }

    @Override
    public void enviarConfirmacionEliminacionCuenta(String destinatario) {
        try {
            jakarta.mail.internet.MimeMessage mensaje = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mensaje, true, "UTF-8");
            
            helper.setFrom("no-reply@sobracero.com");
            helper.setTo(destinatario);
            helper.setSubject("Confirmación de eliminación de cuenta - Sobra Cero");
            
            Context context = new Context();
            
            String contenidoHtml = templateEngine.process("eliminacion-cuenta", context);
            helper.setText(contenidoHtml, true);
            
            mailSender.send(mensaje);
        } catch (Exception e) {
            System.err.println("No se pudo enviar el correo de eliminación de cuenta a " + destinatario + ": " + e.getMessage());
        }
    }

    @Override
    public void enviarEliminacionCuentaPorAdmin(String destinatario) {
        try {
            jakarta.mail.internet.MimeMessage mensaje = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mensaje, true, "UTF-8");
            
            helper.setFrom("no-reply@sobracero.com");
            helper.setTo(destinatario);
            helper.setSubject("Aviso: Tu cuenta ha sido eliminada - Sobra Cero");
            
            Context context = new Context();
            
            String contenidoHtml = templateEngine.process("eliminacion-cuenta-admin", context);
            helper.setText(contenidoHtml, true);
            
            mailSender.send(mensaje);
            System.out.println("\n📧 Correo de eliminación (por Admin) enviado a " + destinatario);
        } catch (Exception e) {
            System.err.println("No se pudo enviar el correo de eliminación por Admin a " + destinatario + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void enviarNotificacionEliminacionDonacion(String destinatario, String nombreDonacion) {
        try {
            jakarta.mail.internet.MimeMessage mensaje = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mensaje, true, "UTF-8");
            
            helper.setFrom("no-reply@sobracero.com");
            helper.setTo(destinatario);
            helper.setSubject("Tu donación ha sido eliminada - Sobra Cero");
            
            Context context = new Context();
            context.setVariable("nombreDonacion", nombreDonacion);
            
            String contenidoHtml = templateEngine.process("eliminacion-donacion", context);
            helper.setText(contenidoHtml, true);
            
            mailSender.send(mensaje);
            System.out.println("\n📧 Correo de eliminación enviado a " + destinatario + " por la donación: " + nombreDonacion);
        } catch (Exception e) {
            System.err.println("No se pudo enviar el correo de eliminación a " + destinatario + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void enviarCodigoReserva(String destinatarioOng, String destinatarioDonante, String codigo, String nombreDonacion) {
        String mensajeOng = "¡Has reservado una donación con éxito! Al momento de ir a retirarla, deberás mostrarle al Donante el siguiente código para confirmar la entrega:";
        enviarEmailReservaInterno(destinatarioOng, codigo, nombreDonacion, mensajeOng);

        String mensajeDonante = "¡Una ONG ha reservado tu donación! Cuando vayan a retirarla, pídeles el siguiente código e ingrésalo en la plataforma para validar la entrega:";
        enviarEmailReservaInterno(destinatarioDonante, codigo, nombreDonacion, mensajeDonante);
    }

    private void enviarEmailReservaInterno(String destinatario, String codigo, String nombreDonacion, String mensajeRol) {
        try {
            jakarta.mail.internet.MimeMessage mensaje = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mensaje, true, "UTF-8");
            
            helper.setFrom("no-reply@sobracero.com");
            helper.setTo(destinatario);
            helper.setSubject("Código de Retiro: " + nombreDonacion + " - Sobra Cero");
            
            Context context = new Context();
            context.setVariable("codigoRetiro", codigo);
            context.setVariable("nombreDonacion", nombreDonacion);
            context.setVariable("mensajeRol", mensajeRol);
            
            String contenidoHtml = templateEngine.process("reserva-confirmada", context);
            helper.setText(contenidoHtml, true);
            
            mailSender.send(mensaje);
            System.out.println("\n📧 Correo de reserva confirmada enviado a " + destinatario);
        } catch (Exception e) {
            System.err.println("No se pudo enviar el correo de reserva confirmada a " + destinatario + ": " + e.getMessage());
            System.out.println("======> CÓDIGO DE RETIRO DE RESPALDO: " + codigo + " <======");
        }
    }
}
