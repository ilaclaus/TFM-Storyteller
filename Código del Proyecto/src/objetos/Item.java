package objetos;

public abstract class Item extends Objeto{

	protected Item(String id, String descripcion) {
		super(id, descripcion);
	}
	
	public boolean puedeCogerse() {
		return true;
	}
}
