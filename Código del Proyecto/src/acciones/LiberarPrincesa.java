package acciones;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import personajes.Personaje;

public class LiberarPrincesa {

	private Personaje personaje;
	private String princesa, dragon;
	private AID agenteMundo;

	public LiberarPrincesa(Personaje personaje, String princesa, String dragon,
			AID agenteMundo) {

		this.personaje = personaje;
		this.princesa = princesa;
		this.dragon = dragon;
		this.agenteMundo = agenteMundo;

	}

	public void execute() {

		ACLMessage liberarPrincesa = new ACLMessage(ACLMessage.REQUEST);
		liberarPrincesa.addReceiver(agenteMundo);
		liberarPrincesa.setConversationId("Liberar");
		liberarPrincesa.setReplyWith("liberar" + System.currentTimeMillis());
		liberarPrincesa.setContent(personaje.getLocalName() + " " + princesa
				+ " " + dragon);
		personaje.send(liberarPrincesa);

		MessageTemplate mt = MessageTemplate.MatchInReplyTo(liberarPrincesa
				.getReplyWith());
		ACLMessage reply = personaje.blockingReceive(mt);

		personaje.getLogger().info(
				"El caballero " + personaje.getLocalName()
						+ " ha liberado a la princesa " + reply.getContent());
		
		System.out.println(personaje.marcaDeClase() + " El caballero "
				+ personaje.getLocalName() + " ha liberado a la princesa "
				+ reply.getContent() + ". \n");

	}
}
