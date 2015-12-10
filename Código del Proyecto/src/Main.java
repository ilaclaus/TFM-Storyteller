import jade.Boot;

public class Main {


	public static void main(String[] args) {
		String[] argumentos = new String [2];
		argumentos[0] = "-gui";
		argumentos[1] = "D:agentesPrincipales.AgenteDirector()";
		
		jade.Boot.main(argumentos);

	}

}
