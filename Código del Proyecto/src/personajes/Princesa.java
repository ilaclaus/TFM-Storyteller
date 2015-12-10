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

public class Princesa extends Personaje {

	
	private AID padre;
	private AID dragon;
	
	
	public Princesa() {
		super(100, "Castillo");
	}
	
	
	protected void setup(){
		
		avisarInicio();
		localizarPersonaje();

		Object[] args = getArguments(); 
		 if (args != null && args.length > 0) {
			padre = new AID((String) args[0], AID.ISLOCALNAME);

			getLogger().info("La Princesa " + getAID().getLocalName() + " despierta.");
			System.out.println(marcaDeClase() + " La Princesa " + getAID().getLocalName() +
						" despierta. \n");	
			
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setType("Secuestro");
			sd.setName("JADE-Secuestro");
			dfd.addServices(sd);
			
			try{
				DFService.register(this, dfd);
			} catch (FIPAException fe){
				fe.printStackTrace();
			}
			
			/*
			addBehaviour(new MoverSecuestrada());
			addBehaviour(new AvisaAPadre());
			addBehaviour(new Rescatada());
			*/
			comportamientoPaseo();
		 }
		 else {
			 getLogger().info("La Princesa " + getAID().getLocalName() + " no tiene padre. No hay a quien pedir rescate.");
			 System.out.println(marcaDeClase() + " La Princesa no tiene padre. No hay a quien pedir rescate. \n");
			 doDelete();
		 }
	}
	
	protected void takeDown() {
		getLogger().info("La Princesa " + getAID().getLocalName() + " pone fin a su aventura.");
		System.out.println(marcaDeClase() + " La Princesa " + getLocalName() + " pone fin a su aventura. \n");
	}
	
	
	private class MoverSecuestrada extends CyclicBehaviour {
		
		public void action() {
			
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchConversationId("Mover-Princesa"));
			ACLMessage receive = myAgent.receive(mt);
			
			if ( receive != null ) {

				moverSecuestrado(receive.getContent());
				ACLMessage reply = receive.createReply();
				myAgent.send(reply);	
				
			} else
				block();
			
		}
	}
	
	private class AvisaAPadre extends Behaviour {

		private ACLMessage receive;	
		
		public void action() {
			
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Te secuestro"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));
			receive = receive(mt);
			
			if ( receive != null ) {
				
				try{
					DFService.deregister(myAgent);
				}catch (FIPAException fe){
					fe.printStackTrace();
				}
				
				dragon = receive.getSender();
				send(receive.createReply());	
			
				getLogger().info("La Princesa " + getAID().getLocalName() + " ha sido secuestrada.");
				System.out.println(marcaDeClase() + " La Princesa " + myAgent.getLocalName() + " ha sido secuestrada. \n");
				
				ACLMessage inform = new ACLMessage(ACLMessage.REQUEST);
				inform.setConversationId("Ayuda");
				inform.setReplyWith("request" + System.currentTimeMillis());
				inform.addReceiver(padre);
				inform.setContent(dragon.getLocalName());
				myAgent.send(inform);

			} else
				block();
		}

		@Override
		public boolean done() {
			return receive != null;
		}
	}
	
	private class Rescatada extends Behaviour {
		
		ACLMessage receive;
		
		public void action() {
			
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Rescatada"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));
			receive = receive(mt);
			
			if ( receive != null ) 
				myAgent.doDelete();
				
			else
				block();
			
		}
		
		public boolean done() {
			return receive != null;
		}
	}
}
