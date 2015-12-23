package acciones;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import personajes.Personaje;

public class Saludo {

	private Personaje personaje;
	private String secundario;
	
	
	public Saludo(Personaje personaje, String secundario) {
		
		this.personaje = personaje;
		this.secundario = secundario;
		
	}
	
	
	public void execute() {
		
		ACLMessage saludo = new ACLMessage(ACLMessage.INFORM);
		saludo.addReceiver(new AID ((String) secundario, AID.ISLOCALNAME));
		saludo.setConversationId("Saludo");
		saludo.setReplyWith("Saludo" + System.currentTimeMillis());
		personaje.send(saludo);
		
		MessageTemplate mt = MessageTemplate.MatchInReplyTo(saludo.getReplyWith());
		ACLMessage reply = personaje.blockingReceive(mt);
		
	}
	
}
