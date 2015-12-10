package acciones;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import personajes.Personaje;

public class Mover {

	private Personaje personaje;
	private String locOrigen;
	private String locDest;
	private AID agenteMundo;

	public Mover(Personaje personaje, String locOrigen, String locDest,
			AID agenteMundo) {

		this.personaje = personaje;
		this.locOrigen = locOrigen;
		this.locDest = locDest;
		this.agenteMundo = agenteMundo;

	}

	public void execute() {

		if (locOrigen.equalsIgnoreCase(personaje.getLocalizacion())) {
			ACLMessage mover = new ACLMessage(ACLMessage.REQUEST);
			mover.addReceiver(agenteMundo);
			mover.setConversationId("Mover");
			mover.setReplyWith("mover" + System.currentTimeMillis());
			mover.setContent(personaje.getClass().getName().substring(11) + " "
					+ locDest + " " + locOrigen);
			personaje.send(mover);

			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchConversationId("Mover"),
					MessageTemplate.MatchInReplyTo(mover.getReplyWith()));

			ACLMessage msg = personaje.blockingReceive(mt);

			if (msg.getPerformative() == ACLMessage.CONFIRM) {

				personaje.setLocalizacion(msg.getContent());
				
				personaje.getLogger().info(
						personaje.getLocalName() + " ha llegado a "
								+ personaje.getLocalizacion() + ".");
				
				System.out.println(personaje.marcaDeClase() + " "
						+ personaje.getLocalName() + " ha llegado a "
						+ personaje.getLocalizacion() + ".\n");

			} else {
				personaje.getLogger().error(
						"No se ha podido cambiar de localizacion.");
				System.err.println(personaje.marcaDeClase()
						+ " No se ha podido cambiar de localizacion. \n");
			}

		} else {
			
			personaje.getLogger().error(
					"El personaje " + personaje.getLocalName()
							+ " se quería ir de " + locOrigen
							+ ", pero éste estaba en "
							+ personaje.getLocalizacion() + ".");
			
			System.err.println(personaje.marcaDeClase() + " El personaje "
					+ personaje.getLocalName() + " se quería ir de "
					+ locOrigen + ", pero éste estaba en "
					+ personaje.getLocalizacion() + ". \n");
		}
	}
}
