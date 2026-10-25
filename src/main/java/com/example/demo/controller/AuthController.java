
package com.example.demo.controller;

// Imports para crear los endpoints y devolver respuestas HTTP
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Clases de Spring Security para gestionar la autenticación
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;

// Permite gestionar la petición, la respuesta y la sesión HTTP
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

// Permite validar los datos recibidos
import jakarta.validation.Valid;

// Clases de nuestro proyecto
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegistroRequest;
import com.example.demo.service.UsuarioService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final SecurityContextRepository securityContextRepository;

    // Inyectamos el servicio de usuarios y el repositorio
    // encargado de guardar la autenticación en la sesión.
    public AuthController(
            UsuarioService usuarioService,
            SecurityContextRepository securityContextRepository) {

        this.usuarioService = usuarioService;
        this.securityContextRepository = securityContextRepository;
    }

    // H6.1 - REGISTRO DE USUARIOS
    @PostMapping("/registro")
    public ResponseEntity<String> registrar(
            @Valid @RequestBody RegistroRequest request) {

        try {
            // El servicio cifra la contraseña antes de guardarla.
            usuarioService.registrar(
                    request.getNombre(),
                    request.getEmail(),
                    request.getPassword()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Usuario registrado correctamente");

        } catch (IllegalArgumentException e) {

            // El correo electrónico ya está registrado.
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(e.getMessage());

        } catch (org.springframework.mail.MailException e) {

            // Mostramos el error real de Gmail en la consola de Eclipse.
            e.printStackTrace();

            // Gmail no ha podido enviar el correo de verificación.  
        	return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR
            ).body(
                    "La cuenta se ha guardado, pero no se ha podido "
                    + "enviar el correo de verificación."
            );
        }
    }

    // H6.1 - VERIFICACIÓN DEL CORREO ELECTRÓNICO
    @GetMapping("/verificar")
    public ResponseEntity<String> verificarEmail(
            @RequestParam("token") String token) {

        // Comprobamos si el token existe y no ha caducado.
        boolean verificado = usuarioService.verificarEmail(token);

        if (verificado) {

            // El usuario queda activado en la base de datos.
            return ResponseEntity.ok(
                    "Correo verificado correctamente. "
                    + "Ya puedes iniciar sesión."
            );
        }

        // El token no existe, ya se utilizó o ha caducado.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("El enlace de verificación no es válido "
                      + "o ha caducado.");
    }

    // H6.2 - INICIO DE SESIÓN CON PERSISTENCIA
    @PostMapping("/login")
    public ResponseEntity<String> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        // Comprobamos el correo y la contraseña con nuestro servicio.
        boolean correcto = usuarioService.login(
                request.getEmail(),
                request.getPassword()
        );

        if (!correcto) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Correo o contraseña incorrectos");
        }

        // Creamos un objeto que representa al usuario autenticado.
        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        request.getEmail(),
                        null,
                        java.util.Collections.emptyList()
                );

        // Creamos un contexto de seguridad y añadimos el usuario.
        SecurityContext securityContext =
                SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(authentication);

        // Guardamos la autenticación para la petición actual.
        SecurityContextHolder.setContext(securityContext);

        // Renovamos la sesión para evitar reutilizar
        // un identificador de sesión anterior al login.
        if (httpRequest.getSession(false) != null) {
            httpRequest.changeSessionId();
        }

        // Guardamos el contexto en la sesión HTTP.
        // Así Spring Security reconocerá al usuario
        // en las siguientes peticiones del navegador.
        securityContextRepository.saveContext(
                securityContext,
                httpRequest,
                httpResponse
        );

        return ResponseEntity.ok("Inicio de sesión correcto");
    }

    // H6.2 - CIERRE DE SESIÓN
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpServletRequest httpRequest) {

        // Eliminamos la autenticación del contexto de seguridad.
        SecurityContextHolder.clearContext();

        // Recuperamos la sesión actual sin crear una nueva.
        HttpSession sesion = httpRequest.getSession(false);

        // Si existe una sesión, la invalidamos.
        // Esto elimina la autenticación almacenada en ella.
        if (sesion != null) {
            sesion.invalidate();
        }

        return ResponseEntity.ok("Sesión cerrada correctamente");
    }
}
