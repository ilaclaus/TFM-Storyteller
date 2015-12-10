package acciones;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import personajes.Personaje;

public class Batalla {

	private Personaje personaje;
	private String secundario;
	
	
	public Batalla(Personaje personaje, String secundario) {
		
		this.personaje = personaje;
		this.secundario = secundario;
		
	}
	
	
	public void execute() {
		
		ACLMessage batalla = new ACLMessage(ACLMessage.INFORM);
		batalla.addReceiver(new AID ((String) secundario, AID.ISLOCALNAME));
		batalla.setConversationId("Batalla");
		batalla.setReplyWith("batalla" + System.currentTimeMillis());
		batalla.setContent(Integer.toString(personaje.getVida()));
		personaje.send(batalla);
		
		MessageTemplate mt = MessageTemplate.MatchInReplyTo(batalla.getReplyWith());
		ACLMessage reply = personaje.blockingReceive(mt);
		personaje.anadirVida( -Integer.parseInt(reply.getContent()) );
		
	}
}
