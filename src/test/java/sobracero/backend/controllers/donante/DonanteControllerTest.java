package sobracero.backend.controllers.donante;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import sobracero.backend.dtos.ong.DonacionResponseDto;
import sobracero.backend.services.donante.DonanteService;
import sobracero.backend.security.JwtUtil;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

@WebMvcTest(DonanteController.class)
@AutoConfigureMockMvc(addFilters = false) // Deshabilitar JWT para pruebas de controlador
public class DonanteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DonanteService donanteService;

    @MockBean
    private JwtUtil jwtUtil;

    @BeforeEach
    public void setUp() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        // Inyectar Principal (Long) esperado por el controlador
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(1L, null, List.of());
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    public void testObtenerMisDonaciones() throws Exception {
        // Mocks
        DonacionResponseDto donacion1 = new DonacionResponseDto();
        donacion1.setId(100L);
        donacion1.setNombre("Pan Fresco");

        DonacionResponseDto donacion2 = new DonacionResponseDto();
        donacion2.setId(101L);
        donacion2.setNombre("Leche");

        when(donanteService.obtenerMisDonaciones(anyLong())).thenReturn(List.of(donacion1, donacion2));

        // Ejecutar request
        mockMvc.perform(MockMvcRequestBuilders.get("/api/donante/donaciones")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].nombre").value("Pan Fresco"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].nombre").value("Leche"));
    }

    @Test
    public void testEliminarDonacionExitoso() throws Exception {
        // Mock method void
        doNothing().when(donanteService).eliminarDonacion(anyLong(), anyLong());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/donante/donaciones/100"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.mensaje").value("Donación eliminada"));
    }

    @Test
    public void testPublicarDonacion() throws Exception {
        // Inicializar datos
        DonacionResponseDto respuestaMock = new DonacionResponseDto();
        respuestaMock.setId(200L);
        respuestaMock.setNombre("Manzanas");

        // Mocks
        when(donanteService.crearDonacion(any(), anyLong())).thenReturn(respuestaMock);

        // Ejecutar request y validar
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/donante/donaciones")
                .param("tipoAlimento", "Fruta")
                .param("porciones", "50")
                .param("ubicacion", "Av. Principal 123"))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.nombre").value("Manzanas"));
    }
}
