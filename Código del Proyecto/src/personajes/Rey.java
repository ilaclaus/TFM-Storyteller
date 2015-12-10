package personajes;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Rey extends Personaje {

	private AID[] CaballerosDisponibles;
	private String dragon;
	private int numeroHijas;
	private AID princesaSecuestrada;
	private int tesoro;

	private MessageTemplate mt;
	private AID mejorCaballero;
	private int menosDineroPedido;
	private int repliesCnt;

	public Rey() {
		super(100, "Castillo");
	}

	protected void setup() {

		avisarInicio();
		localizarPersonaje();

		getLogger().info(
				"El Rey " + getAID().getLocalName() + " está preparado.");
		System.out.println(marcaDeClase() + " El Rey " + getAID().getLocalName()
				+ " está preparado. \n");

		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			numeroHijas = Integer.parseInt(((String) args[0]));
			tesoro = 100;

			FSMBehaviour m = new FSMBehaviour(this);
			m.registerFirstState(new Atento(), "Atento");
			m.registerState(new Rescate(), "Rescate");
			m.registerState(new Ayuda(), "Pedir Ayuda");
			m.registerState(new RecibirOfertas(), "Ofertas");
			m.registerState(new AceptarOferta(), "Aceptar");
			m.registerState(new Salvada(), "Princesa Salvada");

			m.registerDefaultTransition("Atento", "Rescate");
			m.registerDefaultTransition("Rescate", "Pedir Ayuda");
			m.registerDefaultTransition("Pedir Ayuda", "Ofertas");
			m.registerTransition("Ofertas", "Rescate", 1);
			m.registerTransition("Ofertas", "Aceptar", 2);
			m.registerTransition("Aceptar", "Princesa Salvada", 1);
			m.registerTransition("Aceptar", "Rescate", 2);
			m.registerDefaultTransition("Princesa Salvada", "Atento");

			addBehaviour(m);
		} else {
			// Make the agent terminate immediately
			getLogger().info(
					"Si el Rey " + getAID().getLocalName()
							+ " no tiene hijas, no tiene de que preocuparse.");
			System.out.println(marcaDeClase() + " Si el Rey " + getAID().getLocalName()
					+ " no tiene hijas, no tiene de que preocuparse \n");
			doDelete();
		}

	}

	protected void takeDown() {
		getLogger().info(
				"El Rey " + getAID().getLocalName()
						+ " terminó su trabajo por hoy.");
		System.out.println(marcaDeClase() + " El Rey " + getAID().getLocalName()
				+ " terminó su trabajo por hoy. \n");
	}

	private class Atento extends OneShotBehaviour {

		private MessageTemplate mt;

		public void action() {

			mt = MessageTemplate.and(
					MessageTemplate.MatchConversationId("Ayuda"),
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
			ACLMessage receive = myAgent.blockingReceive(mt);
			princesaSecuestrada = receive.getSender();
			dragon = receive.getContent();
		}

	}

	private class Rescate extends Behaviour {

		public void action() {

			System.out.println(marcaDeClase() + " Intentando pedir rescate para la princesa "
					+ princesaSecuestrada.getLocalName() + ".");
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("Rescate");
			template.addServices(sd);

			try {

				DFAgentDescription[] result = DFService.search(myAgent,
						template);
				System.out.println(marcaDeClase() + " Encontrados los siguientes caballeros:");
				CaballerosDisponibles = new AID[result.length];

				if (done()) {
					for (int i = 0; i < result.length; i++) {
						CaballerosDisponibles[i] = result[i].getName();
						System.out.println("\t" + CaballerosDisponibles[i]
								.getLocalName());
					}
					System.out.println();
					
				} else {
					System.out.println("\n" + marcaDeClase() + " Esperando 10 segundos... \n");
					Thread.sleep(10000);
				}
				
			} catch (Exception fe) {
				fe.printStackTrace();
			}

		}

		@Override
		public boolean done() {

			return CaballerosDisponibles.length != 0;

		}
	}

	private class Ayuda extends OneShotBehaviour {

		public void action() {

			ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
			cfp.setContent(princesaSecuestrada.getLocalName());
			cfp.setConversationId("PedirAyuda");

			for (int i = 0; i < CaballerosDisponibles.length; i++) {
				cfp.addReceiver(CaballerosDisponibles[i]);
			}

			repliesCnt = CaballerosDisponibles.length;
			mejorCaballero = null;
			menosDineroPedido = Integer.MAX_VALUE;

			cfp.setReplyWith("cfp" + System.currentTimeMillis());
			myAgent.send(cfp);
			mt = MessageTemplate.and(
					MessageTemplate.MatchConversationId("PedirAyuda"),
					MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
		}
	}

	private class RecibirOfertas extends Behaviour {

		public void action() {

			ACLMessage msg = myAgent.blockingReceive(mt);

			int dineroPedido = Integer.parseInt(msg.getContent());

			if ((mejorCaballero == null || dineroPedido < menosDineroPedido)
					&& (dineroPedido <= tesoro)) {
				
				menosDineroPedido = dineroPedido;
				mejorCaballero = msg.getSender();
			}

			repliesCnt--;

			if (!done())
				reset();
		}

		@Override
		public boolean done() {
			return repliesCnt == 0;
		}

		public int onEnd() {

			if (mejorCaballero == null)
				return 1;
			else
				return 2;
		}
	}

	private class AceptarOferta extends OneShotBehaviour {

		public void action() {

			ACLMessage aceptar = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
			aceptar.addReceiver(mejorCaballero);
			aceptar.setContent(princesaSecuestrada.getLocalName() + " "
					+ dragon);
			aceptar.setConversationId("Aceptar-oferta");
			myAgent.send(aceptar);
			
		}

		public int onEnd() {

			mt = MessageTemplate.MatchConversationId("Rescate");
			ACLMessage reply = myAgent.blockingReceive(mt);

			if (reply.getPerformative() == ACLMessage.INFORM)
				return 1;

			else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				getLogger()
						.info("El Rey "
								+ getAID().getLocalName()
								+ " recibe la noticia de la muerte del caballero "
								+ reply.getSender().getLocalName()
								+ ", asi que busca a otro.");
				System.out.println(marcaDeClase() + " El Rey recibe la noticia de la muerte del caballero, así que busca a otro. \n");

				return 2;

			}

		}
	}

	private class Salvada extends OneShotBehaviour {

		public void action() {

			getLogger().info(
					"El Rey " + getAID().getLocalName() + " entrega "
							+ menosDineroPedido + " monedas al caballero "
							+ mejorCaballero.getLocalName() + ".");
			
			System.out.println(marcaDeClase() + " El Rey entrega " + menosDineroPedido
					+ " monedas al caballero " + mejorCaballero.getLocalName() + ". \n");
			
			tesoro -= menosDineroPedido;

			ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
			inform.setConversationId("Rescatada");
			inform.setReplyWith("inform" + System.currentTimeMillis());
			inform.setContent(mejorCaballero.getLocalName());
			inform.addReceiver(princesaSecuestrada);

			myAgent.send(inform);
			numeroHijas--;

			if (numeroHijas == 0) {
				getLogger().info("Todas las hijas del Rey " + getAID().getLocalName()
								+ " estan a salvo, puede morir tranquilo.");
				System.out.println(marcaDeClase() + " Todas las hijas del Rey están a salvo, puede morir tranquilo. \n");
				doDelete();
			}

			else if (tesoro == 0) {
				getLogger().info("El Rey "+ getAID().getLocalName() 
						+ " ha perdido todo su dinero con el rescate, asi que deja de ser Rey.");
				System.out.println(marcaDeClase() + " El Rey ha perdido todo su dinero con el rescate, asi que deja de ser Rey. \n");
				doDelete();
			}
		}
	}
}