package objetos;

import personajes.Personaje;
import entorno.Localizacion;

public abstract class Objeto {
	private String id;
	private String descripcion;
	
	protected Objeto (String id, String descripcion) {
		this.id = id;
		this.descripcion = descripcion;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String toString() {
		String cadena = "";
		
		cadena += "-- objeto[ " + this.id + " ]= " + this.descripcion;
		return cadena;
	}
	
	public abstract boolean usar(Personaje quien, Localizacion donde);
	public abstract boolean puedeUsarse();
	public abstract boolean puedeCogerse();
}
