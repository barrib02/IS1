
package com.example.demo.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Actividad;
import com.example.demo.entity.Usuario;
import com.example.demo.service.ActividadService;
import com.example.demo.service.PerfilService;

@RestController
@RequestMapping("/api/actividad")
public class ActividadController {

    private final ActividadService actividadService;
    private final PerfilService perfilService;

    public ActividadController(
            ActividadService actividadService,
            PerfilService perfilService) {

        this.actividadService = actividadService;
        this.perfilService = perfilService;
    }

    // Obtener la actividad reciente del usuario autenticado.
    @GetMapping
    public List<Actividad> obtenerMiActividad(Principal principal) {

        Usuario usuario = perfilService
                .obtenerUsuarioPorEmail(principal.getName());

        return actividadService
                .obtenerActividadReciente(usuario.getId());
    }
}
