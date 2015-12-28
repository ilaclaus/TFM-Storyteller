package personajes;

import java.util.ArrayList;
import java.util.Random;

import org.apache.log4j.Logger;

import acciones.Batalla;
import acciones.Mover;
import acciones.Pasear;
import acciones.Saludo;
import agentesPrincipales.AgenteDirector;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import objetos.Item;


/*
 * La idea es que los personajes (caballero, dragón y princesa) anden aleatoriamente por el mapa. 
 * En el momento en que se cruzan pueden ocurrir varias cosas:
 * - Dragón + dragón: hablan/pasan de largo
 * - Dragón + caballero: pasan de largo/luchan
 * - Caballero + caballero: hablan/pasan de largo
 * - Caballero + princesa: hablan/pasan de largo/se enamoran
 * - Princesa + princesa: hablan/pasan de largo
 * - Dragón + princesa: pasan de largo/secuestro
 * - Caballero + dragón secuestrador: luchan
 * 
 */

public class Personaje extends Agent {

	private int vida;
	private ArrayList<Par> objetos;
	
	private String localizacion;

	private AID agenteMundo;

	private Logger logger;
	private String persInteraccion;
	private boolean maestro;

	class Par {

		private Item objeto;
		private int numero;

		public Par(Item objeto, int numero) {
			this.objeto = objeto;
			this.numero = numero;
		}

	}

	public Personaje(int vida, String localizacion) {

		this.vida = vida;
		this.objetos = new ArrayList<Par>();
		this.localizacion = localizacion;
		logger = Logger.getLogger(getClass().getName().substring(11));
		persInteraccion = null;
	}

	public String marcaDeClase() {

		String clase = getClass().getName().substring(11);

		if (clase.equalsIgnoreCase("Dragon"))
			clase = "#";

		else if (clase.equalsIgnoreCase("Rey"))
			clase = "-";

		else if (clase.equalsIgnoreCase("Princesa"))
			clase = "*";

		else if (clase.equalsIgnoreCase("Caballero"))
			clase = "+";

		return clase;
	}

	public void avisarInicio() {

		ACLMessage confirmacion = new ACLMessage(ACLMessage.CONFIRM);
		confirmacion.setContent(AgenteDirector.INICIO);
		confirmacion.addReceiver(new AID("director", AID.ISLOCALNAME));
		send(confirmacion);

	}

	public void localizarPersonaje() {

		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Mundo");
		template.addServices(sd);

		try {

			DFAgentDescription[] result = DFService.search(this, template);

			agenteMundo = new AID();
			agenteMundo = result[0].getName();

			ACLMessage localizar = new ACLMessage(ACLMessage.REQUEST);
			localizar.addReceiver(agenteMundo);
			localizar.setConversationId("Mover");
			localizar.setContent(getClass().getName().substring(11) + " "
					+ localizacion);
			localizar.setReplyWith("localizar" + System.currentTimeMillis());
			send(localizar);

			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchConversationId("Mover"),
					MessageTemplate.MatchInReplyTo(localizar.getReplyWith()));
			blockingReceive(mt);

		} catch (FIPAException e) {
			e.printStackTrace();
		}

	}

	public int getVida() {
		return this.vida;
	}

	public void anadirVida(int vida) {
		this.vida += vida;

		if (this.vida <= 0) {
			this.vida = 0;

			ACLMessage muerto = new ACLMessage(ACLMessage.INFORM);
			muerto.addReceiver(agenteMundo);
			muerto.setConversationId("Muerto");
			muerto.setContent(getLocalName());
			send(muerto);
			MessageTemplate mt = MessageTemplate.MatchConversationId("Muerto");
			blockingReceive(mt);

		}
	}

	public boolean estaMuerto() {
		return this.vida == 0;
	}

	public void anadirObjeto(Item objeto) {

		for (int i = 0; i < this.objetos.size(); i++) {

			if (objeto.getId().equalsIgnoreCase(
					this.objetos.get(i).objeto.getId()))
				this.objetos.get(i).numero++;

			else {
				Par par = new Par(objeto, 1);
				this.objetos.add(par);
			}
		}
	}

	public boolean elimObjeto(String id) {

		boolean ok = false;

		for (int i = 0; i < this.objetos.size(); i++) {
			if (this.objetos.get(i).objeto.getId().equalsIgnoreCase(id)) {
				if (this.objetos.get(i).numero == 0)
					this.objetos.remove(i);
				else
					this.objetos.get(i).numero--;

				ok = true;
				break;
			}
		}

		return ok;
	}

	public String getLocalizacion() {
		return this.localizacion;
	}

	public void setLocalizacion(String localizacion) {
		this.localizacion = localizacion;
	}

	public Logger getLogger() {
		return logger;
	}

	public void planificar(String princesa) throws Exception {

		boolean ok;
		boolean falloSecuestro = false;

		/*
		for (int i = 0; 1 < 3; i++)
			(new Pasear(this, localizacion, agenteMundo)).execute();
		/*
		do {
			
			ok = true;

			for (int i = 0; i < 2; i++) {
				
				if (estaMuerto())
					break;
				
				(new Mover(this, "Castillo", "Pueblo", agenteMundo)).execute();
				
			}
			
			mandarCrearArchivo(princesa);
			String[] args = { "domain.pddl", getLocalName() + ".pddl" };

			String ff = JavaFF.crearPlan(args);
			String[] cadena = ff.split("\n");

			for (String sigAccion : cadena) {
				String[] accionActual = sigAccion.split(" ");
				String accion = accionActual[0];

				if (estaMuerto())
					break;

				if (!accionActual[1].equalsIgnoreCase(getLocalName())) {
					ok = false;
					break;
				}

				else if (accion.equalsIgnoreCase("moverprincipal")
						|| accion.equalsIgnoreCase("moversecundario")) {
					new Mover(this, accionActual[2], accionActual[3],
							agenteMundo).execute();
				}

				else if (accion.equalsIgnoreCase("secuestrar")) {
					if ( ! new Secuestrar(this, accionActual[2], agenteMundo).execute() ) {
						falloSecuestro = true;
						break;
					}
				}
				
				else if (accion.equalsIgnoreCase("moverpersonajeconprincesa")) {
					ACLMessage moverPrincesa = new ACLMessage(
							ACLMessage.REQUEST);
					moverPrincesa.setConversationId("Mover-Princesa");
					moverPrincesa.setReplyWith("mover-princesa"
							+ System.currentTimeMillis());
					moverPrincesa.addReceiver(new AID((String) princesa,
							AID.ISLOCALNAME));
					moverPrincesa.setContent(accionActual[4]);
					send(moverPrincesa);

					MessageTemplate mt = MessageTemplate
							.MatchInReplyTo(moverPrincesa.getReplyWith());
					blockingReceive(mt);
					new Mover(this, accionActual[3], accionActual[4],
							agenteMundo).execute();

				} else if (accion.equalsIgnoreCase("batalla"))
					new Batalla(this, accionActual[2]).execute();

				else if (accion.equalsIgnoreCase("liberarprincesa"))
					new LiberarPrincesa(this, accionActual[2], accionActual[3],
							agenteMundo).execute();

				else if (accion.equalsIgnoreCase("dejarencasa"))
					new DejarEnCasa(this, accionActual[2], agenteMundo)
							.execute();

				else if (accion.equalsIgnoreCase("convertirseenheroe"))
					new ConvertirseEnHeroe(this, agenteMundo).execute();

				else {
					System.out.println(sigAccion);
					throw new Exception("Accion no reconocible");
				}
				
			}
			

		} while (!ok);

		if ( falloSecuestro ) {
			
			ACLMessage fallo = new ACLMessage(ACLMessage.FAILURE);
			fallo.setConversationId("Fallo Secuestro");
			fallo.addReceiver(getAID());
			send(fallo);
			
		} else {
			ACLMessage finPlan = new ACLMessage(ACLMessage.INFORM);
			finPlan.setConversationId("Fin-Plan");
			finPlan.addReceiver(getAID());
			send(finPlan);
		}*/
	}

	public void mandarCrearArchivo(String princesa) {

		ACLMessage toPDDL = new ACLMessage(ACLMessage.REQUEST);
		toPDDL.addReceiver(agenteMundo);
		toPDDL.setConversationId("toPDDL");
		toPDDL.setReplyWith("toPDDL" + System.currentTimeMillis());
		toPDDL.setContent(getClass().getName().substring(11) + " "
				+ getLocalName() + " " + princesa);
		send(toPDDL);

		MessageTemplate mt = MessageTemplate.and(
				MessageTemplate.MatchConversationId("toPDDL"),
				MessageTemplate.MatchInReplyTo(toPDDL.getReplyWith()));
		blockingReceive(mt);
	}

	public void moverSecuestrado(String locDest) {
		new Mover(this, localizacion, locDest, agenteMundo).execute();
	}
	
	public void comportamientoPaseo() {
		
		FSMBehaviour m = new FSMBehaviour(this);
		
		m.registerFirstState(new DarUnPaseo(), "Paseo");
		m.registerState(new PidePersonajes(), "Pide personajes");
		m.registerState(new InteractuaMSTR(), "Interactua-MSTR");
		m.registerState(new InteractuaSLV(), "Interactua-SLV");
		
		m.registerDefaultTransition("Paseo", "Pide personajes");
		m.registerTransition("Pide personajes", "Paseo", 0);
		m.registerTransition("Pide personajes", "Interactua-MSTR", 1);
		m.registerTransition("Pide personajes", "Interactua-SLV", 2);
		m.registerTransition("Interactua-SLV", "Paseo", 0);
		m.registerTransition("Interactua-SLV", "Interactua-SLV", 1);
		m.registerDefaultTransition("Interactua-MSTR", "Paseo");
		
		addBehaviour(m);
		addBehaviour(new RespondeSaludo());

	}
	
	private class Pasea extends CyclicBehaviour {

		@Override
		public void action() {
			(new Pasear(Personaje.this, Personaje.this.localizacion, agenteMundo)).execute();
		}
		
	}
	
	private class DarUnPaseo extends OneShotBehaviour {

		@Override
		public void action() {
			// TODO: Hacer que los personajes en la misma localización interactúen ¿? 
			(new Pasear(Personaje.this, Personaje.this.localizacion, agenteMundo)).execute();
			
			/*
			 * Máquina de estados: Paseo -> Pide personajes -> Saludo o Pasar de largo
			 */
		}
	}
	
	private class PidePersonajes extends OneShotBehaviour {
		
		// Espera emparejamiento con un personaje
		@Override
		public void action() {
			// El mundo devuelve: 
			// - Null: No hay personajes con los que interactuar
			// - P MSTR: Interactuar con el personaje P, siendo el maestro (crea y ejecuta la acción)
			// - P SLV: Interactuar con el personaje P, siendo el esclave (espera a que se acabe la acción)
			
			ACLMessage personajesEnMiLoc = new ACLMessage(ACLMessage.REQUEST);
			personajesEnMiLoc.addReceiver(agenteMundo);
			personajesEnMiLoc.setConversationId("peerRequest");
			personajesEnMiLoc.setReplyWith("peerRequest" + System.currentTimeMillis());
			personajesEnMiLoc.setContent(localizacion + " " + getLocalName());
			send(personajesEnMiLoc);
			
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchConversationId("peerRequest"));
			ACLMessage receive = myAgent.blockingReceive(mt);
			
			if (receive.getContent() != null) { 
				String [] msj = receive.getContent().split(" ");
				persInteraccion = msj[0];
				
				if (msj[1].equalsIgnoreCase("MSTR"))
					maestro = true;
				
				else 
					maestro = false;
			} else 
				persInteraccion = null;
		}
		
		// 0 -> Nadie, seguir paseando
		// 1 -> Alguien, pasar a interacción. TODO: Interacción MSTR-SLV
		public int onEnd() {
			// Si no tiene pareja, a pasear
			if (persInteraccion == null)
				return 0;
			
			// Si tiene pareja y es maestro, a interactuar como un maestro
			else if (maestro)
				return 1;
			
			// Si tiene pareja y es esclavo, a interactuar como un esclavo
			else 
				return 2;
		}
	}
	
	private class InteractuaMSTR extends OneShotBehaviour {

		@Override
		public void action() {
			// El maestro es el que ejecuta las acciones
			if ((Personaje.this.getLocalName().equalsIgnoreCase("Arturo") && persInteraccion.equalsIgnoreCase("Draco")))
				(new Batalla(Personaje.this, persInteraccion)).execute();

			else {
				System.out.println(Personaje.this.getLocalName() + " saluda a " + persInteraccion + " en " + localizacion);
				(new Saludo(Personaje.this, persInteraccion)).execute();
			}

			ACLMessage interaccionCompleta = new ACLMessage(ACLMessage.INFORM);
			interaccionCompleta.addReceiver(new AID(persInteraccion, AID.ISLOCALNAME));
			interaccionCompleta.addReceiver(agenteMundo);
			interaccionCompleta.setConversationId("interaccionCompleta");
			send(interaccionCompleta);

		}
	}
	
	private class InteractuaSLV extends OneShotBehaviour {

		private ACLMessage receive;
		
		@Override
		public void action() {
			// Espera recepción del mensaje de info de fin de interacción (para que no cambie de localización mientras se realiza)
			MessageTemplate mt = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchConversationId("interaccionCompleta"));

				receive = myAgent.receive(mt);
		}
		
		public int onEnd() {
			if (receive != null)
				return 0;
			
			else 
				return 1;
		}
		
	}
	
	private class RespondeSaludo extends CyclicBehaviour {

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchConversationId("Saludo"));
			ACLMessage receive = myAgent.receive(mt);
			
			if (receive != null) {
					// Si no estan en la misma localización, no hacer nada
					System.out.println(Personaje.this.getLocalName() + " le devuelve el saludo a " + receive.getSender().getLocalName() + " en " + localizacion);
					
					ACLMessage response = receive.createReply();
					send(response);
			}
		}
	}
	
}
