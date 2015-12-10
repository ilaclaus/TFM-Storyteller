package objetos;

import personajes.Personaje;
import entorno.Localizacion;

public class Decorado extends Objeto {

	protected Decorado(String id, String descripcion) {
		super(id, descripcion);
	}

	public boolean usar(Personaje quien, Localizacion donde) {
		// Observar el objeto
		return true;
	}

	public boolean puedeUsarse() {
		// Puede observarse. En un futuro, podrian esconderse objetos en éstos.
		return true;
	}

	public boolean puedeCogerse() {
		return false;
	}

}