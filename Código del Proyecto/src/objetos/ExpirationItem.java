package objetos;

import personajes.Personaje;
import entorno.Localizacion;

public class ExpirationItem extends Item{
	private int times;
	private final int TIMES = 1;
	
	public ExpirationItem(String id, String description) {
		super(id, description);
		this.times = TIMES;
	}
	public ExpirationItem(String id, String description, int times) {
		super(id, description);
		this.times=times;
	}
	public boolean puedeUsarse() {
		return this.times>=1;
	}
	public String toString() {
		return super.toString() + "//" + this.times;
	}
	public boolean usar(Personaje who, Localizacion where) {
		this.times--;
		
		if(this.times<=0) {
			who.elimObjeto(this.getId());
			//System.out.println(Constants.MESSAGE_EMPTY.replace("id", this.getId()));
		}
		return true;
	}
	
}