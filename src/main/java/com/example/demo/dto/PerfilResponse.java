
package com.example.demo.dto;

public class PerfilResponse {

    private Long id;
    private String nombre;
    private String biografia;
    private String fotoPerfilUrl;
    private String bannerUrl;

    public PerfilResponse(Long id, String nombre, String biografia,
            String fotoPerfilUrl, String bannerUrl) {
        this.id = id;
        this.nombre = nombre;
        this.biografia = biografia;
        this.fotoPerfilUrl = fotoPerfilUrl;
        this.bannerUrl = bannerUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getBiografia() {
        return biografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }

    public String getFotoPerfilUrl() {
        return fotoPerfilUrl;
    }

    public void setFotoPerfilUrl(String fotoPerfilUrl) {
        this.fotoPerfilUrl = fotoPerfilUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }
}
