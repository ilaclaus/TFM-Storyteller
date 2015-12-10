package personajes;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import org.apache.log4j.Logger;

public class Caballero extends Personaje {

	private int dinero;

	public Caballero() {
		
		super((Math.random() <= 0.5) ? 1 : 2, "Pueblo");
	}

	protected void setup() {

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Rescate");
		sd.setName("JADE-Rescate");
		dfd.addServices(sd);
		
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		System.out.println("Vida de " + getLocalName() + ": " + this.getVida() + "\n");
		avisarInicio();
		localizarPersonaje();
		
		dinero = 50;

		getLogger().info("El caballero " + getAID().getLocalName()
				+ " entra en escena.");
		System.out.println(marcaDeClase() + " El caballero " + getLocalName()
				+ " entra en escena. \n");

		/*
		addBehaviour(new OfrecerRescate());
		addBehaviour(new AceptarOfertaRescate());
		*/
		comportamientoPaseo();
	}

	protected void takeDown() {
		if ( estaMuerto() ) {
			
			getLogger().info("El caballero " + getLocalName()
					+ " ha muerto en combate.");
			System.out.println(marcaDeClase() + " El caballero " + getLocalName()
					+ " ha muerto en combate. \n");
			
		} else {
		
			getLogger().info("El caballero " + getAID().getLocalName() + " se retira.");
			System.out.println(marcaDeClase() + " El caballero " + getAID().getLocalName() + " se retira. \n");
			
		}	
	}

	private class OfrecerRescate extends CyclicBehaviour {

		public void action() {

			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchConversationId("PedirAyuda"),
					MessageTemplate.MatchPerformative(ACLMessage.CFP));
			ACLMessage msg = myAgent.receive(mt);

			if (msg != null) {
				String princesa = msg.getContent();
				ACLMessage reply = msg.createReply();

				reply.setPerformative(ACLMessage.PROPOSE);
				reply.setContent(String.valueOf(dinero));

				myAgent.send(reply);

			} else
				block();

		}
	}

	private class AceptarOfertaRescate extends CyclicBehaviour {

		public void action() {

			MessageTemplate mt = MessageTemplate.and(MessageTemplate
					.MatchConversationId("Aceptar-oferta"), MessageTemplate
					.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL));
			ACLMessage msg = myAgent.receive(mt);

			if (msg != null) {
				String princesa = msg.getContent().split(" ")[0];
				String dragon = msg.getContent().split(" ")[1];

				try {
					planificar(princesa);
					addBehaviour(new FinPlanificacion(msg.getSender()));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			} else
				block();

		}
	}
	
	private class FinPlanificacion extends Behaviour {
		
		AID rey;
		ACLMessage receive;
		
		public FinPlanificacion(AID rey) {
			this.rey = rey;
		}
		
		
		public void action() {

			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Fin-Plan"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));
			
			receive = receive(mt);
			
			if ( receive != null ) {
				
				ACLMessage rescate = new ACLMessage(ACLMessage.INFORM);
				rescate.setConversationId("Rescate");
				rescate.addReceiver(rey);
				
				if ( estaMuerto() )
					rescate.setPerformative(ACLMessage.FAILURE);
				
				try {
					DFService.deregister(myAgent);
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
				doDelete();
				send(rescate);
				
			} else
				block();
		}

		@Override
		public boolean done() {
			return receive != null;
		}
	}
}