package entorno;

import java.util.ArrayList;
import java.util.Random;

public class Mapa {

	private ArrayList<Localizacion> localizaciones;

	public Mapa() {
		this.localizaciones = new ArrayList<Localizacion>();
	}

	public String dameLocAdyacente(String loc) {
		int r = 0;
		String loc2 = "";
		
		do {
			r = (new Random()).nextInt(3);
			loc2 = localizaciones.get(r).getNombre();
		} while (loc.equalsIgnoreCase(loc2));
		
		return loc2;
	}
	
	public Localizacion anadirLocalizacion(String loc) throws Exception {

		if ( !localizaciones.contains(getLocalizacion(loc)) ) {
			Localizacion localizacion = new Localizacion(loc);
			this.localizaciones.add(localizacion);
			return localizacion;
			
		} else
			throw new Exception("Hay localizaciones repetidas");
	}

	public Localizacion getLocalizacion(String loc) {

		Localizacion localizacion = null;

		for (Localizacion localiz : localizaciones) {

			if (loc.equalsIgnoreCase(localiz.getNombre())) {
				localizacion = localiz;
				break;
			}
		}

		return localizacion;
	}
	
}
