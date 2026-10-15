@GetMapping("/work/{id}")
public Obra obtenerObra(@PathVariable Long id) {
    return obraService.getObras()
            .stream()
            .filter(o -> o.getId().equals(id))
            .findFirst()
            .orElse(null);
}
