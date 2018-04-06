package com.api.controller;

import com.api.domain.Articulo;
import com.api.exception.ArticuloNoEncontradoException;
import com.api.service.ArticuloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/api")
class ArticuloController {

	@Autowired
	ArticuloService articuloService;

	@GetMapping("/articulos/{id}")
	public Articulo getArticuloById(@PathVariable(value = "id") String articuloId) {

		Articulo articulo = articuloService.getArticulo(articuloId);

		if (articulo != null) {
			return articulo;
		} else {
			throw new ArticuloNoEncontradoException("Articulo", "id", articuloId);
		}
	}

	@PutMapping("/articulos/{id}")
	public Articulo updateArticulo(@PathVariable(value = "id") String articuloId, @Valid @RequestBody Articulo articuloDescripcion) {

		Articulo articulo = articuloService.getArticulo(articuloId);

		if (articulo != null) {
			articulo.setDescripcion(articuloDescripcion.getDescripcion());
			articulo.setModelo(articuloDescripcion.getModelo());

			Articulo updatedArticulo = articuloService.updateArticulo(articulo);
			return updatedArticulo;
		} else {
			throw new ArticuloNoEncontradoException("Articulo", "id", articuloId);
		}
	}
}