package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.demo.entity.Seguimiento;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.SeguimientoRepository;
import com.example.demo.repository.UsuarioRepository;

class SeguimientoServiceTest {

    @Test
    void permiteSeguirUsuario() {

        SeguimientoRepository seguimientoRepository =
                mock(SeguimientoRepository.class);

        UsuarioRepository usuarioRepository =
                mock(UsuarioRepository.class);

        SeguimientoService service =
                new SeguimientoService(
                        seguimientoRepository,
                        usuarioRepository);

        Usuario seguidor = new Usuario();
        seguidor.setNombre("alba");

        Usuario seguido = new Usuario();
        seguido.setNombre("juan");

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(seguidor));

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(seguido));

        when(seguimientoRepository
                .existsBySeguidorAndSeguido(
                        seguidor,
                        seguido))
                .thenReturn(false);

        service.seguir(1L, 2L);

        verify(seguimientoRepository)
                .save(any(Seguimiento.class));
    }

    @Test
    void noPermiteSeguimientosDuplicados() {

        SeguimientoRepository seguimientoRepository =
                mock(SeguimientoRepository.class);

        UsuarioRepository usuarioRepository =
                mock(UsuarioRepository.class);

        SeguimientoService service =
                new SeguimientoService(
                        seguimientoRepository,
                        usuarioRepository);

        Usuario seguidor = new Usuario();
        Usuario seguido = new Usuario();

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(seguidor));

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(seguido));

        when(seguimientoRepository
                .existsBySeguidorAndSeguido(
                        seguidor,
                        seguido))
                .thenReturn(true);

        service.seguir(1L, 2L);

        verify(seguimientoRepository, never())
                .save(any());
    }

    @Test
    void noPermiteSeguirseASiMismo() {

        SeguimientoRepository seguimientoRepository =
                mock(SeguimientoRepository.class);

        UsuarioRepository usuarioRepository =
                mock(UsuarioRepository.class);

        SeguimientoService service =
                new SeguimientoService(
                        seguimientoRepository,
                        usuarioRepository);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.seguir(1L, 1L));
    }

    @Test
    void permiteDejarDeSeguir() {

        SeguimientoRepository seguimientoRepository =
                mock(SeguimientoRepository.class);

        UsuarioRepository usuarioRepository =
                mock(UsuarioRepository.class);

        SeguimientoService service =
                new SeguimientoService(
                        seguimientoRepository,
                        usuarioRepository);

        Usuario seguidor = new Usuario();
        Usuario seguido = new Usuario();

        Seguimiento seguimiento =
                new Seguimiento(
                        seguidor,
                        seguido);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(seguidor));

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(seguido));

        when(seguimientoRepository
                .findBySeguidorAndSeguido(
                        seguidor,
                        seguido))
                .thenReturn(Optional.of(seguimiento));

        service.dejarDeSeguir(1L, 2L);

        verify(seguimientoRepository)
                .delete(seguimiento);
    }
    
    @Test
    void obtieneSeguidoresCorrectamente() {

        SeguimientoRepository seguimientoRepository =
                mock(SeguimientoRepository.class);

        UsuarioRepository usuarioRepository =
                mock(UsuarioRepository.class);

        SeguimientoService service =
                new SeguimientoService(
                        seguimientoRepository,
                        usuarioRepository);

        Usuario usuario = new Usuario();
        usuario.setNombre("juan");

        Usuario seguidor = new Usuario();
        seguidor.setNombre("alba");

        Seguimiento seguimiento =
                new Seguimiento();

        seguimiento.setSeguidor(seguidor);
        seguimiento.setSeguido(usuario);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(seguimientoRepository.findBySeguido(usuario))
                .thenReturn(List.of(seguimiento));

        var resultado =
                service.obtenerSeguidores(1L);

        assertEquals(1, resultado.size());

        assertEquals(
                "alba",
                resultado.get(0).getUsername());
    }
    
    @Test
    void obtieneSeguidosCorrectamente() {

        SeguimientoRepository seguimientoRepository =
                mock(SeguimientoRepository.class);

        UsuarioRepository usuarioRepository =
                mock(UsuarioRepository.class);

        SeguimientoService service =
                new SeguimientoService(
                        seguimientoRepository,
                        usuarioRepository);

        Usuario usuario = new Usuario();
        usuario.setNombre("alba");

        Usuario seguido = new Usuario();
        seguido.setNombre("juan");

        Seguimiento seguimiento =
                new Seguimiento();

        seguimiento.setSeguidor(usuario);
        seguimiento.setSeguido(seguido);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(seguimientoRepository.findBySeguidor(usuario))
                .thenReturn(List.of(seguimiento));

        var resultado =
                service.obtenerSeguidos(1L);

        assertEquals(1, resultado.size());

        assertEquals(
                "juan",
                resultado.get(0).getUsername());
    }

}

