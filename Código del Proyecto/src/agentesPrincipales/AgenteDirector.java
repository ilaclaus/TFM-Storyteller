package agentesPrincipales;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.PlatformController;

import java.util.ArrayList;

public class AgenteDirector extends Agent {


	public final static String INICIO = "INICIO";
	
	
	private ArrayList<AID> agentes;
	

	public AgenteDirector() {
		
		agentes = new ArrayList<AID>();
		
	}

	protected void setup () {

		try {
			
			PlatformController container = getContainerController();
			
			AgentController guest = container.createNewAgent("Mundo", "agentesPrincipales.AgenteMundo", null);
			guest.start();
			agentes.add(new AID("Mundo", AID.ISLOCALNAME) );
			
			addBehaviour(new ConfirmarInicio());
			guest = container.createNewAgent("Arturo", "personajes.Caballero", null);
			guest.start();
			agentes.add(new AID("Arturo", AID.ISLOCALNAME) );
			
			/*
			addBehaviour(new ConfirmarInicio());
			guest = container.createNewAgent("Hector", "personajes.Caballero", null);
			guest.start();
			agentes.add(new AID("Hector", AID.ISLOCALNAME) );
			
			addBehaviour(new ConfirmarInicio());
			String[] arg = {"10"};
			guest = container.createNewAgent("Reinaldo", "personajes.Rey", arg);
			guest.start();
			agentes.add(new AID("Reinaldo", AID.ISLOCALNAME) );
			*/
			addBehaviour(new ConfirmarInicio());
			String[] args = {"Reinaldo"};
			guest = container.createNewAgent("Laura", "personajes.Princesa", args);
			guest.start();
			agentes.add(new AID("Laura", AID.ISLOCALNAME) );
			

//			addBehaviour(new ConfirmarInicio());
//			guest = container.createNewAgent("Draco", "personajes.Dragon", null);
//			guest.start();
//			agentes.add(new AID("Draco", AID.ISLOCALNAME) );
//			addBehaviour(new ConfirmarInicio());

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	private class ConfirmarInicio extends OneShotBehaviour {
		
		public void action () {
			try {
				ACLMessage msg = blockingReceive();

					if ( !INICIO.equals( msg.getContent() ))
						throw new Exception ("Error al iniciar un Agente");
				
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
