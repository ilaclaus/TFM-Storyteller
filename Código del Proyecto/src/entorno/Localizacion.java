package entorno;

import java.util.ArrayList;

import objetos.Item;
import personajes.Personaje;

public class Localizacion {

	
	private String nombre;
	private ArrayList<String> conectadoCon;
	
	private ArrayList<String> personajes;
	private ArrayList<Par> objetos;
	
	
	class Par {
		private Item objeto;
		private int numero;
		
		public Par (Item objeto, int numero) {
			this.objeto = objeto;
			this.numero = numero;
		}
	}
	
	
	public Localizacion (String nombre) {
		this.nombre = nombre;
		this.conectadoCon = new ArrayList<String>();
		this.personajes = new ArrayList<String>();
		this.objetos = new ArrayList<Par>();
	}
	
	
	public Localizacion (String nombre, Item[] items) {
		this.nombre = nombre;
		this.objetos = new ArrayList<Par>();
		
		for (int i = 0; i < items.length; i++) {
			for (int j = 0; j < this.objetos.size(); j++) {
				
				if (items[i].getId().equalsIgnoreCase(this.objetos.get(j).objeto.getId()))
					this.objetos.get(j).numero++;
				
				else {
					Par par = new Par(items[i], 1);
					this.objetos.add(par);
				}
			}
		}
	}

	
	public void anadirConectado (String localizacion) {
		
		if ( !conectadoCon.contains(localizacion) )
			conectadoCon.add(localizacion); 	
	}
	
	
	public boolean existeConexion (String localizacion) {

		for (String conectado : conectadoCon){
			if ( localizacion.equalsIgnoreCase(conectado) ) 
				return true;
		}
		
		return false;
	}
	
	
	public void anadirPersonaje (String personaje) {
		
		personajes.add(personaje);
		
	}
	
	public boolean eliminarPersonaje (String personaje) {
		
		return personajes.remove(personaje);
		
	}
	
	// A�ade un objeto a la posici�n
	public void anadirObjeto (Item objeto) {

		for (int i = 0; i < this.objetos.size(); i++) {
			
			if (objeto.getId().equalsIgnoreCase(this.objetos.get(i).objeto.getId()))
				this.objetos.get(i).numero++;
		
			else {
				Par par = new Par(objeto, 1);
				this.objetos.add(par);
			}
		}
	}
	
//	Devuelve la descripci�n de la posici�n junto con los objetos que hay en ella
//	public String darDescripcion() {
//		String cadena = "";
//		
//		cadena += this.nombre + "\n";
//		if (this.objetos.size() != 0) {
//			cadena += "Hay los siguientes objetos:" + "\n";
//			for (int i = 0; i < this.objetos.size(); i++) {
//				cadena += this.objetos.get(i).objeto.toString();
//				if (i < this.objetos.size() - 1)
//					cadena += "\n";
//			}
//		}
//		
//		return cadena;
//	}
	
	// Coge un objeto de la posici�n y lo a�ade al inventario del personaje dado
	public boolean cogerObjeto (Personaje quien, String id) {
		boolean encontrado = existeObjeto(id);
		
		if (encontrado) {
			for (int i = 0; i < this.objetos.size(); i++)
				if (id.equalsIgnoreCase(this.objetos.get(i).objeto.getId())){
					quien.anadirObjeto(this.objetos.get(i).objeto);
					
					if ( this.objetos.get(i).numero == 1)
						this.objetos.remove(i);
					else
						this.objetos.get(i).numero--;
				}
		}
	
		return encontrado;
	}
	
	// Comprueba si existe el objeto dado en la posici�n
	public boolean existeObjeto (String id) {
		boolean encontrado = false;
		
		for (int i = 0; i < this.objetos.size(); i++)
			if (id.equalsIgnoreCase(this.objetos.get(i).objeto.getId()))
				encontrado = true;
		
		return encontrado;
	}

	public String getNombre() {
		return nombre;
	}
	
}

