package sobracero.backend.controllers.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import sobracero.backend.models.EstadoCuenta;
import sobracero.backend.models.Rol;
import sobracero.backend.models.Usuario;
import sobracero.backend.security.JwtUtil;
import sobracero.backend.services.FileService;
import sobracero.backend.services.auth.AuthService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private FileService fileService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    public void testLoginExitosoAdmin() throws Exception {
        // Inicializar datos
        Usuario adminMock = new Usuario();
        adminMock.setId(1L);
        adminMock.setEmail("admin@sobracero.cl");
        adminMock.setRol(Rol.ADMIN);
        adminMock.setEstado(EstadoCuenta.ACTIVO);

        // Mocks
        when(authService.autenticar("admin@sobracero.cl", "admin123")).thenReturn(adminMock);
        when(jwtUtil.generateToken(adminMock)).thenReturn("fake-jwt-token-12345");

        String requestBody = "{\"email\": \"admin@sobracero.cl\", \"password\": \"admin123\"}";

        // Ejecutar request y validar
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value("fake-jwt-token-12345"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.usuario.email").value("admin@sobracero.cl"));
    }

    @Test
    public void testLoginConOTP() throws Exception {
        // Mocks
        when(authService.autenticar("donante@test.com", "pass123")).thenReturn("OTP enviado al correo");

        String requestBody = "{\"email\": \"donante@test.com\", \"password\": \"pass123\"}";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.requiereOtp").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mensaje").value("OTP enviado al correo"));
    }

    @Test
    public void testRegistroExitoso() throws Exception {
        // Inicializar datos
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setId(2L);
        nuevoUsuario.setEmail("nuevo@ong.cl");
        nuevoUsuario.setRol(Rol.ONG);
        nuevoUsuario.setRazonSocial("ONG Solidaria");

        // Mocks
        when(authService.registrarUsuario(any(Usuario.class))).thenReturn(nuevoUsuario);

        // Ejecutar request y validar
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/auth/registro")
                .param("email", "nuevo@ong.cl")
                .param("rol", "ONG")
                .param("razonSocial", "ONG Solidaria")
                .param("telefono", "987654321"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("nuevo@ong.cl"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.rol").value("ONG"));
    }

    @Test
    public void testValidarOtpExitoso() throws Exception {
        // Inicializar datos
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setEmail("donante@test.com");

        // Mocks
        when(authService.verificarOtp("donante@test.com", "123456")).thenReturn(usuarioMock);
        when(jwtUtil.generateToken(usuarioMock)).thenReturn("fake-token-otp");

        String requestBody = "{\"email\": \"donante@test.com\", \"otp\": \"123456\"}";

        // Ejecutar request y validar
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value("fake-token-otp"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.usuario.email").value("donante@test.com"));
    }
}
