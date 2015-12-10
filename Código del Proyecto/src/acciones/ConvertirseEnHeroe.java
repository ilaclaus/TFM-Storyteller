package acciones;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import personajes.Personaje;

public class ConvertirseEnHeroe {

	private Personaje personaje;
	private AID agenteMundo;
	
	public ConvertirseEnHeroe (Personaje personaje, AID agenteMundo) {
		
		this.personaje = personaje;
		this.agenteMundo = agenteMundo;
		
	}
	
	public void execute() {
		
		ACLMessage serHeroe = new ACLMessage(ACLMessage.REQUEST);
		serHeroe.addReceiver(agenteMundo);
		serHeroe.setConversationId("Ser Heroe");
		serHeroe.setReplyWith("serheroe" + System.currentTimeMillis());
		personaje.send(serHeroe);
		
		MessageTemplate mt = MessageTemplate.MatchInReplyTo(serHeroe.getReplyWith());
		personaje.blockingReceive(mt);
		
		
		personaje.getLogger().info("El caballero " + personaje.getLocalName()
				+ " se ha convertido en heroe.");
		System.out.println("+ El caballero " + personaje.getLocalName()
				+ " se ha convertido en héroe. \n");
		
	}
	
}
