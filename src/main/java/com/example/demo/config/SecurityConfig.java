
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
public class SecurityConfig {

    // Cifra las contraseñas utilizando BCrypt.
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Guarda la autenticación en la sesión HTTP.
    // Es el mismo repositorio que utiliza AuthController.
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    // Configuración de seguridad de Shelfie.
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository securityContextRepository)
            throws Exception {

        http

            // Configuración provisional para pruebas.
            // Activaremos CSRF antes de terminar H6.2.
            .csrf(csrf -> csrf.disable())

            // Permite crear sesiones cuando sean necesarias.
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )

            // Indica dónde recuperar y guardar la autenticación.
            .securityContext(context -> context
                .securityContextRepository(securityContextRepository)
            )

            // Reglas de acceso.
            .authorizeHttpRequests(auth -> auth

                // Páginas y recursos públicos.
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/login.html",
                    "/registro.html",
                    "/search.html",
                    "/work.html",
                    "/resenas.html",
                    "/valoraciones.html",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()

                // H6.1 - Registro de usuarios.
                // H6.2 - Inicio de sesión.
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/auth/registro",
                    "/api/auth/login"
                ).permitAll()

                // H6.1 - Verificación del correo electrónico.
                // No requiere iniciar sesión porque el usuario
                // todavía no ha activado su cuenta.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/auth/verificar"
                ).permitAll()

                // H6.2 - Cierre de sesión.
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/auth/logout"
                ).authenticated()

                // Consultas públicas de obras.
                .requestMatchers(
                    HttpMethod.GET,
                    "/search",
                    "/work/*"
                ).permitAll()

                // Consultas públicas de reseñas y valoraciones.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/resenas/**",
                    "/api/valoraciones/**"
                ).permitAll()

                // Estados personales de lectura.
                .requestMatchers(
                    "/users/*/works/*/estado"
                ).authenticated()

                // Publicar, editar o eliminar contenido.
                .requestMatchers(
                    "/api/resenas/**",
                    "/api/valoraciones/**"
                ).authenticated()

                // Modificar obras requiere autenticación.
                .requestMatchers(
                    "/work/*/estado"
                ).authenticated()

                // El resto queda protegido por defecto.
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
