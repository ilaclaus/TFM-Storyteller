package objetos;

import personajes.Personaje;
import entorno.Localizacion;


public abstract class PersistentItem extends Item{
	public PersistentItem(String id, String description) {
		super(id, description);
	}
	public boolean puedeUsarse() {
		return true;
	}
	public abstract boolean usar(Personaje who, Localizacion where);
}