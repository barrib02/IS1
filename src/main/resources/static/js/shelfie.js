/* ==========================================================
   Shelfie - utilidades compartidas
   Se incluye en el <head> de cada página con:
   <script src="/js/shelfie.js"></script>
   y en el <body> basta con poner <div id="navbar"></div>
   ========================================================== */
(function () {

	const Shelfie = {};

	// Escapa texto antes de meterlo en innerHTML (evita inyección de HTML/JS).
	Shelfie.esc = function (texto) {
		return String(texto == null ? "" : texto).replace(/[&<>"']/g, function (c) {
			return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c];
		});
	};

	// Espera "ms" milisegundos sin nuevas llamadas antes de ejecutar la función.
	// Sirve para no lanzar una petición por cada tecla pulsada.
	Shelfie.debounce = function (funcion, ms) {
		let temporizador;
		return function () {
			const args = arguments;
			clearTimeout(temporizador);
			temporizador = setTimeout(function () { funcion.apply(null, args); }, ms || 250);
		};
	};

	// Llama a la API y devuelve siempre { ok, status, datos } sin lanzar error por un 4xx/5xx.
	Shelfie.api = async function (url, opciones) {
		const respuesta = await fetch(url, Object.assign({ credentials: "same-origin" }, opciones));
		const texto = await respuesta.text();
		let datos = null;
		if (texto) {
			try { datos = JSON.parse(texto); } catch (e) { datos = texto; }
		}
		return { ok: respuesta.ok, status: respuesta.status, datos: datos };
	};

	// Perfil del usuario con sesión (o null). Se pide UNA sola vez por página:
	// la navbar y la ficha de obra comparten esta misma promesa.
	let promesaPerfil = null;
	Shelfie.obtenerPerfil = function () {
		if (!promesaPerfil) {
			promesaPerfil = Shelfie.api("/api/perfil")
				.then(function (r) { return r.ok ? r.datos : null; })
				.catch(function () { return null; });
		}
		return promesaPerfil;
	};

	// "CIENCIA_FICCION" -> "Ciencia ficcion"
	Shelfie.formatearEnum = function (valor) {
		const texto = String(valor || "").replace(/_/g, " ").toLowerCase();
		return texto.charAt(0).toUpperCase() + texto.slice(1);
	};

	// Pide a Cloudinary una versión pequeña de la imagen (carga más rápida).
	Shelfie.miniatura = function (url, lado) {
		if (!url || url.indexOf("/upload/") === -1) { return url; }
		return url.replace("/upload/", "/upload/w_" + lado + ",h_" + lado + ",c_fill,f_auto,q_auto/");
	};

	// Dibuja 5 estrellas clicables dentro de "contenedor".
	// opciones: { valor, onChange(valor), soloLectura }
	// Pulsar la estrella que ya está marcada pone la valoración a 0.
	Shelfie.estrellas = function (contenedor, opciones) {
		opciones = opciones || {};
		let valor = opciones.valor || 0;
		const botones = [];

		contenedor.innerHTML = "";
		contenedor.classList.add("estrellas");
		contenedor.setAttribute("role", "group");
		contenedor.setAttribute("aria-label", "Valoración de 0 a 5 estrellas");

		function pintar() {
			botones.forEach(function (boton, i) {
				boton.classList.toggle("on", i < valor);
				boton.setAttribute("aria-pressed", String(i < valor));
			});
		}

		for (let i = 1; i <= 5; i++) {
			const boton = document.createElement("button");
			boton.type = "button";
			boton.textContent = "★";
			boton.setAttribute("aria-label", i + (i === 1 ? " estrella" : " estrellas"));
			boton.disabled = !!opciones.soloLectura;
			boton.addEventListener("click", function () {
				valor = (valor === i) ? 0 : i;
				pintar();
				if (opciones.onChange) { opciones.onChange(valor); }
			});
			botones.push(boton);
			contenedor.appendChild(boton);
		}

		pintar();

		return {
			get: function () { return valor; },
			set: function (nuevo) { valor = nuevo || 0; pintar(); }
		};
	};

	// Construye la barra superior: logo, secciones, lupa de búsqueda y avatar.
	Shelfie.montarNavbar = async function () {
		const barra = document.getElementById("navbar");
		if (!barra) { return; }

		barra.className = "navbar";
		barra.innerHTML =
			'<a class="marca" href="/search.html">' +
			'<img src="/images/logo.svg" alt="" width="34" height="34"><span>Shelfie</span></a>' +

			'<nav class="enlaces" aria-label="Secciones">' +
			'<a href="/search.html">General</a>' +
			'<a href="/search.html?tipo=LIBRO">Libros</a>' +
			'<a href="/search.html?tipo=PELICULA">Películas</a>' +
			'<a href="/search.html?tipo=SERIE">Series</a>' +
			'<a href="/search.html?tipo=VIDEOJUEGO">Videojuegos</a>' +
			'<a href="/publicas.html">Listas</a></nav>' +

			// Buscador: solo se ve la lupa; al pulsarla se despliega el campo.
			'<form class="buscador-nav" role="search" action="/search.html" method="get">' +
			'<input type="search" name="q" placeholder="Buscar…" aria-label="Buscar" autocomplete="off">' +
			'<button type="submit" class="lupa" aria-label="Buscar">' +
			'<svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" ' +
			'stroke-width="2" stroke-linecap="round" aria-hidden="true">' +
			'<circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/></svg></button></form>' +

			'<div id="acceso"></div>';

		// Subrayamos la sección en la que estamos.
		const tipoActual = new URLSearchParams(window.location.search).get("tipo") || "";
		barra.querySelectorAll(".enlaces a").forEach(function (enlace) {
			const url = new URL(enlace.href);
			const mismaPagina = url.pathname === window.location.pathname;
			const mismoTipo = (url.searchParams.get("tipo") || "") === tipoActual;
			if (mismaPagina && mismoTipo) { enlace.setAttribute("aria-current", "page"); }
		});

		// Lupa: 1er clic abre el campo, 2º clic (con texto) busca. Escape lo cierra.
		const formulario = barra.querySelector(".buscador-nav");
		const campo = formulario.querySelector("input");

		formulario.querySelector(".lupa").addEventListener("click", function (evento) {
			if (!formulario.classList.contains("abierto")) {
				evento.preventDefault();
				formulario.classList.add("abierto");
				campo.focus();
			} else if (campo.value.trim() === "") {
				evento.preventDefault();
				formulario.classList.remove("abierto");
			}
		});
		campo.addEventListener("keydown", function (evento) {
			if (evento.key === "Escape") { formulario.classList.remove("abierto"); }
		});

		// Avatar o botón de acceso (igual que antes).
		const perfil = await Shelfie.obtenerPerfil();
		const enlace = document.createElement("a");

		if (perfil) {
			enlace.href = "/perfil.html";
			enlace.className = "avatar";
			enlace.title = "Mi perfil (" + perfil.nombre + ")";
			enlace.setAttribute("aria-label", "Ir a mi perfil");

			if (perfil.fotoPerfilUrl) {
				const imagen = document.createElement("img");
				imagen.src = Shelfie.miniatura(perfil.fotoPerfilUrl, 80);
				imagen.alt = "";
				enlace.appendChild(imagen);
			} else {
				enlace.textContent = (perfil.nombre || "?").charAt(0).toUpperCase();
			}
		} else {
			enlace.href = "/login.html";
			enlace.className = "btn btn-sec";
			enlace.textContent = "Iniciar sesión";
		}

		// --- Sprint 6: campana de notificaciones y enlace de moderación ---
		const acceso = barra.querySelector("#acceso");

		if (perfil) {
			Shelfie.montarCampana(acceso);   // la campana va antes que el avatar

			// Solo los moderadores ven el enlace al panel.
			if (perfil.rol === "MODERADOR") {
				const enlaceMod = document.createElement("a");
				enlaceMod.href = "/validar-obras.html";
				enlaceMod.textContent = "Moderación";
				barra.querySelector(".enlaces").appendChild(enlaceMod);
			}
			const enlaceStats = document.createElement("a"); // Rev si es dentro del if anterior o no
			enlaceStats.href = "/estadisticas.html";
			enlaceStats.textContent = "Estadísticas";
			barra.querySelector(".enlaces").appendChild(enlaceStats);
		}

		acceso.appendChild(enlace);   // avatar (o botón "Iniciar sesión")

	};
	
	document.addEventListener("DOMContentLoaded", Shelfie.montarNavbar);
	
	// Nombre legible de cada tipo de obra.
	Shelfie.NOMBRE_TIPO = {
		LIBRO: "Libro",
		PELICULA: "Película",
		SERIE: "Serie",
		VIDEOJUEGO: "Videojuego"
	};

	// "MUSICA" -> "tipo-musica" (clase CSS que le da su color).
	Shelfie.claseTipo = function (tipo) {
		return "tipo-" + String(tipo || "").toLowerCase();
	};

	// Etiqueta (HTML) coloreada del tipo. Úsala dentro de plantillas con innerHTML.
	Shelfie.chipTipo = function (tipo) {
		const nombre = Shelfie.NOMBRE_TIPO[tipo] || Shelfie.formatearEnum(tipo);
		return '<span class="chip ' + Shelfie.claseTipo(tipo) + '">' + Shelfie.esc(nombre) + '</span>';
	};

	// Etiqueta (HTML) del estado de validación: Pendiente / Validada / Rechazada.
	Shelfie.NOMBRE_ESTADO = { PENDIENTE: "Pendiente", VALIDADA: "Validada", RECHAZADA: "Rechazada" };

	Shelfie.chipEstado = function (estado) {
		const nombre = Shelfie.NOMBRE_ESTADO[estado] || estado;
		return '<span class="chip estado-' + String(estado || "").toLowerCase() + '">' + Shelfie.esc(nombre) + '</span>';
	};

	// Igual que Shelfie.api, pero si no hay sesión (401) te lleva al login.
	Shelfie.apiSesion = async function (url, opciones) {
		const r = await Shelfie.api(url, opciones);
		if (r.status === 401) {
			window.location.href = "/login.html";
		}
		return r;
	};

	// Dibuja la campana de notificaciones dentro de "contenedor" (con el globo rojo).
	Shelfie.montarCampana = async function (contenedor) {
		const enlace = document.createElement("a");
		enlace.href = "/notificaciones.html";
		enlace.className = "campana";
		enlace.title = "Notificaciones";
		enlace.setAttribute("aria-label", "Notificaciones");
		enlace.innerHTML =
			'<svg viewBox="0 0 24 24" aria-hidden="true">' +
			'<path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9"/>' +
			'<path d="M13.7 21a2 2 0 0 1-3.4 0"/></svg>' +
			'<span class="contador-campana" hidden></span>';
		contenedor.appendChild(enlace);

		// Pedimos cuántas hay sin leer y, si hay, mostramos el globo.
		const r = await Shelfie.api("/api/notificaciones/no-leidas");
		if (r.ok && r.datos && r.datos.total > 0) {
			const globo = enlace.querySelector(".contador-campana");
			globo.textContent = r.datos.total > 9 ? "9+" : String(r.datos.total);
			globo.hidden = false;
			enlace.setAttribute("aria-label", "Notificaciones (" + r.datos.total + " sin leer)");
		}
	};

	// Gráfica de barras apiladas: obras completadas por mes y tipo.
	// "meses" es la respuesta de /api/estadisticas/mensual (12 elementos, del más antiguo al más reciente).
	Shelfie.graficaMensual = function (contenedor, meses) {

		const TIPOS = [
			{ campo: "libros",      clase: "tipo-libro",      nombre: "Libros" },
			{ campo: "peliculas",   clase: "tipo-pelicula",   nombre: "Películas" },
			{ campo: "series",      clase: "tipo-serie",      nombre: "Series" },
			{ campo: "videojuegos", clase: "tipo-videojuego", nombre: "Videojuegos" }
		];

		function fecha(iso) {
			const p = iso.split("-");
			return new Date(Number(p[0]), Number(p[1]) - 1, 1);
		}
		function corto(iso) { return fecha(iso).toLocaleDateString("es-ES", { month: "short", year: "2-digit" }); }
		function largo(iso) { return fecha(iso).toLocaleDateString("es-ES", { month: "long", year: "numeric" }); }
		function total(m) { return TIPOS.reduce(function (s, t) { return s + m[t.campo]; }, 0); }

		const max = Math.max(1, Math.max.apply(null, meses.map(total)));

		contenedor.innerHTML = "";

		const barras = document.createElement("div");
		barras.className = "grafica-barras";

		const tip = document.createElement("div");
		tip.className = "grafica-tip";
		tip.hidden = true;
		barras.appendChild(tip);

		function mostrarTip(col, m) {
			tip.innerHTML = "<b>" + largo(m.mes) + "</b>" +
				TIPOS.map(function (t) {
					return '<div><i class="grafica-seg ' + t.clase + '"></i>' + t.nombre + ": " + Number(m[t.campo]) + "</div>";
				}).join("") +
				'<div class="total">Total: ' + total(m) + "</div>";
			tip.hidden = false;

			// Centrado sobre la barra, sin salirse del gráfico por los lados.
			let x = col.offsetLeft + col.offsetWidth / 2 - tip.offsetWidth / 2;
			x = Math.max(0, Math.min(x, barras.clientWidth - tip.offsetWidth));
			tip.style.left = x + "px";
		}

		meses.forEach(function (m) {
			const t = total(m);

			const col = document.createElement("div");
			col.className = "grafica-col";
			col.tabIndex = 0;   // accesible con teclado
			col.setAttribute("aria-label", largo(m.mes) + ": " + t + (t === 1 ? " obra" : " obras"));

			const barra = document.createElement("div");
			barra.className = "grafica-barra";

			if (t === 0) {
				barra.classList.add("vacia");
			} else {
				barra.style.height = (t * 100 / max) + "%";
				TIPOS.forEach(function (tipo) {
					if (m[tipo.campo] > 0) {
						const seg = document.createElement("div");
						seg.className = "grafica-seg " + tipo.clase;
						seg.style.flexGrow = m[tipo.campo];   // altura proporcional al nº de obras
						barra.appendChild(seg);
					}
				});
			}
			col.appendChild(barra);

			col.addEventListener("mouseenter", function () { mostrarTip(col, m); });
			col.addEventListener("focus", function () { mostrarTip(col, m); });
			col.addEventListener("click", function () { mostrarTip(col, m); });   // móvil: sin hover
			col.addEventListener("mouseleave", function () { tip.hidden = true; });
			col.addEventListener("blur", function () { tip.hidden = true; });

			barras.appendChild(col);
		});

		// Etiquetas: primera barra, la del medio (la 7.ª, porque con 12 no hay una exacta) y la última.
		const ejes = document.createElement("div");
		ejes.className = "grafica-ejes";
		meses.forEach(function (m, i) {
			const celda = document.createElement("div");
			if (i === 0 || i === 6 || i === meses.length - 1) {
				celda.style.justifyContent = i === 0 ? "flex-start" : (i === 6 ? "center" : "flex-end");
				const etiqueta = document.createElement("span");
				etiqueta.textContent = corto(m.mes);
				celda.appendChild(etiqueta);
			}
			ejes.appendChild(celda);
		});

		const leyenda = document.createElement("div");
		leyenda.className = "grafica-leyenda";
		leyenda.innerHTML = TIPOS.map(function (t) {
			return '<span><i class="grafica-seg ' + t.clase + '"></i>' + t.nombre + "</span>";
		}).join("");

		contenedor.appendChild(barras);
		contenedor.appendChild(ejes);
		contenedor.appendChild(leyenda);
	};

	window.Shelfie = Shelfie;
})();
