@GetMapping("/work/{id}")
public Obra obtenerObra(@PathVariable Long id) {
    return obraService.getObras()
            .stream()
            .filter(o -> o.getId().equals(id))
            .findFirst()
            .orElse(null);
}

package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Obra;
import com.example.demo.service.ObraService;

@RestController
public class ObraController {

    private final ObraService obraService;

    public ObraController(ObraService obraService) {
        this.obraService = obraService;
    }

    @GetMapping("/search")
    public List<Obra> buscar() {
        return obraService.getObras();
    }

    import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;


@PutMapping("/work/{id}/estado")
public String cambiarEstado(
        @PathVariable Long id,
        @RequestParam String estado) {

    for (Obra obra : obraService.getObras()) {

        if (obra.getId().equals(id)) {

            obra.setEstado(
                com.example.demo.entity.EstadoObra.valueOf(estado)
            );

            return "Estado actualizado";
        }
    }

    return "Obra no encontrada";
}

}
