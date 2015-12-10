package objetos;

import personajes.Personaje;
import entorno.Localizacion;


public class Food extends ExpirationItem {
	private int health;
	
	public Food(String name, String description, int health) {
		super(name, description, 1);
		this.health=health;
	}
	
	public Food(String name, String description, int health, int times) {
		super(name, description, times);
		this.health=health;
	}
	
	public boolean usar(Personaje who, Localizacion where) {
		boolean ok=false;
		
		if(this.puedeUsarse()) {
			super.usar(who, where);
			who.anadirVida(this.health);
			ok=true;
		}
		return ok;
	}
}