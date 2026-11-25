// Textos de los estados según el tipo de obra.

const EstadosShelfie = {

    textos: {
        LIBRO: {
            PENDIENTE: "Por leer",
            EN_CURSO: "Leyendo",
            COMPLETADA: "Leído",
            ABANDONADA: "Abandonado"
        },

        PELICULA: {
            PENDIENTE: "Por ver",
            EN_CURSO: "Viendo",
            COMPLETADA: "Vista",
            ABANDONADA: "Abandonada"
        },

        SERIE: {
            PENDIENTE: "Por ver",
            EN_CURSO: "Viendo",
            COMPLETADA: "Vista",
            ABANDONADA: "Abandonada"
        },

        VIDEOJUEGO: {
            PENDIENTE: "Por jugar",
            EN_CURSO: "Jugando",
            COMPLETADA: "Completado",
            ABANDONADA: "Abandonado"
        }
    },

    texto(tipo, estado) {

        if (!estado) {
            return "Sin estado";
        }

        return this.textos[tipo]?.[estado] || estado;
    },

    crearEtiqueta(tipo, estado) {

        const etiqueta = document.createElement("span");

        etiqueta.className = "chip";
        etiqueta.textContent = this.texto(tipo, estado);

        const colores = {
            PENDIENTE: "#e8e5df",
            EN_CURSO: "#fce8b2",
            COMPLETADA: "#cfe9d9",
            ABANDONADA: "#f4d2ce"
        };

        etiqueta.style.backgroundColor =
            colores[estado] || "#eeeeee";

        etiqueta.style.color = "#333333";

        return etiqueta;
    },

    async obtener(usuarioId, obraId) {

        const respuesta = await Shelfie.api(
            "/users/" + encodeURIComponent(usuarioId)
            + "/works/" + encodeURIComponent(obraId)
            + "/estado"
        );

        if (!respuesta.ok) {
            throw new Error("No se pudo consultar el estado");
        }

        return respuesta.datos || null;
    },

    async obtenerVarios(usuarioId, idsObras) {

        const estados = new Map();

        const ids = [...new Set(idsObras)];

        // Consultamos en grupos para evitar demasiadas
        // peticiones simultáneas.
        for (let i = 0; i < ids.length; i += 10) {

            const grupo = ids.slice(i, i + 10);

            await Promise.all(grupo.map(async id => {

                try {

                    const estado = await this.obtener(
                        usuarioId, id);

                    estados.set(String(id), estado);

                } catch (error) {

                    estados.set(String(id), "NO_DISPONIBLE");
                }
            }));
        }

        return estados;
    }
};