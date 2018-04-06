package com.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ArticuloNoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private String recurso;
	private String campo;
	private Object valor;

	public ArticuloNoEncontradoException(String recurso, String campo, Object valor) {
		super(String.format("%s no encontrado con %s : '%s'", recurso, campo, valor));
		this.recurso = recurso;
		this.campo = campo;
		this.valor = valor;
	}

	public String getRecurso() {
		return recurso;
	}

	public String getCampo() {
		return campo;
	}

	public Object getValor() {
		return valor;
	}

}
