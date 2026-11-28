package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.BiografiaRequest;
import com.example.demo.dto.NombreRequest;
import com.example.demo.dto.PerfilResponse;
import com.example.demo.dto.PrivacidadRequest;
import com.example.demo.service.PerfilService;

import jakarta.validation.Valid;

import java.util.List;
import com.example.demo.dto.UsuarioBusquedaDTO;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

	private final PerfilService perfilService;

	public PerfilController(PerfilService perfilService) {
		this.perfilService = perfilService;
	}

	// Mi perfil. Spring rellena "auth" con el usuario de la sesión;
	// auth.getName() es el email guardado en AuthController al hacer login.
	@GetMapping
	public PerfilResponse miPerfil(Authentication auth) {
		return perfilService.obtenerMiPerfil(auth.getName());
	}

	// Editar mi biografía.
	@PutMapping("/biografia")
	public PerfilResponse editarBiografia(@Valid @RequestBody BiografiaRequest request, Authentication auth) {

		return perfilService.actualizarBiografia(auth.getName(), request.getBiografia());
	}

	// Editar mi nombre.
	@PutMapping("/nombre")
	public PerfilResponse editarNombre(@Valid @RequestBody NombreRequest request, Authentication auth) {
		return perfilService.actualizarNombre(auth.getName(), request.getNombre());
	}

	// Perfil de otro usuario (datos públicos).
	@GetMapping("/{id}")
	public PerfilResponse perfilDeUsuario(@PathVariable Long id, Authentication auth) {
		return perfilService.obtenerPerfilPorId(id, auth.getName());
	}

	// Cuenta pública o privada.
	// H5.3 - Cambiar la privacidad del perfil.
	@PutMapping("/privacidad")
	public PerfilResponse cambiarPrivacidad(@Valid @RequestBody PrivacidadRequest request, Authentication auth) {

		return perfilService.actualizarPrivacidad(auth.getName(), request.getPrivacidad());
	}

	// Errores de negocio (usuario no encontrado, biografía demasiado larga).
	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String manejarError(IllegalArgumentException error) {
		return error.getMessage();
	}

	// Errores de validación de @Valid (por ejemplo, más de 500 caracteres).
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String manejarValidacion(MethodArgumentNotValidException error) {
		return error.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
	}

	// H4.2 - Subir o cambiar la foto de perfil
	@PostMapping("/foto")
	public Map<String, String> subirFoto(@RequestParam("archivo") MultipartFile archivo, Authentication auth)
			throws IOException {

		String url = perfilService.actualizarFotoPerfil(auth.getName(), archivo);

		return Map.of("url", url);
	}

	// H4.4 - Subir o cambiar el banner
	@PostMapping("/banner")
	public Map<String, String> subirBanner(@RequestParam("archivo") MultipartFile archivo, Authentication auth)
			throws IOException {

		String url = perfilService.actualizarBanner(auth.getName(), archivo);

		return Map.of("url", url);
	}
	
	// Buscador de usuarios.
	@GetMapping("/buscar")
	public List<UsuarioBusquedaDTO> buscarUsuarios(@RequestParam("texto") String texto, Authentication auth) {
		return perfilService.buscarUsuarios(texto, auth.getName());
	}

}