package com.api.service;

import com.api.domain.Articulo;
import com.api.repository.ArticuloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArticuloService {

	@Autowired
	ArticuloRepository articuloRepository;

	public Articulo updateArticulo(Articulo articulo) {
		articuloRepository.save(articulo);
		return articulo;
	}

	public Articulo getArticulo(String id) {
		return articuloRepository.findOne(id);
	}
}
