package agentesPrincipales;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import entorno.Estado;
import entorno.Localizacion;
import entorno.Mapa;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AgenteMundo extends Agent {

	private Mapa mapa;
	private Estado estado;
	private List<String> buscandoPareja;
	private Map<String, String> interacciones;

	public AgenteMundo() {

		this.estado = new Estado();
		this.mapa = cargarMapa();
		buscandoPareja = Collections.synchronizedList(new ArrayList<String>());
		interacciones = Collections.synchronizedMap(new HashMap<String, String>());

	}

	public Mapa cargarMapa() {

		mapa = new Mapa();

		try {
			File fXmlFile = new File("Mapa.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("localizacion");

			Localizacion loc = null;

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					loc = mapa.anadirLocalizacion(eElement.getAttribute("id"));
					estado.anadirNombre(eElement.getAttribute("id"));

					String[] cade = eElement
							.getElementsByTagName("conectadoCon").item(0)
							.getTextContent().split(" ");

					for (String conectadoCon : cade) {
						loc.anadirConectado(conectadoCon);
						estado.anadirAdyacente(loc.getNombre(), conectadoCon);
						estado.anadirNombre(conectadoCon);
					}

					if (eElement.getElementsByTagName("esSegura").item(0) != null)
						estado.esSegura(loc.getNombre());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		return mapa;
	}

	protected void setup() {

		ACLMessage confirmacion = new ACLMessage(ACLMessage.CONFIRM);
		confirmacion.setContent(AgenteDirector.INICIO);
		confirmacion.addReceiver(new AID("director", AID.ISLOCALNAME));
		send(confirmacion);

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Mundo");
		sd.setName("JADE-Mundo");
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);

		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		addBehaviour(new LocalizarPersonajes());
		addBehaviour(new ToPDDLfile());
		addBehaviour(new MoverPrincesaSecuestrada());
		addBehaviour(new Secuestro());
		addBehaviour(new Liberar());
		addBehaviour(new PersonajeEnCasa());
		addBehaviour(new ConvertirEnHeroe());
		addBehaviour(new MuertePersonaje());
		addBehaviour(new PeticionesDePersonajes());
		addBehaviour(new FinInteraccion());
		addBehaviour(new Empareja());
	}

	protected void takeDown() {

		try {
			DFService.deregister(this);

		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

	}
	
	private class FinInteraccion extends CyclicBehaviour {

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchConversationId("interaccionCompleta"));
			ACLMessage receive = myAgent.receive(mt);
			
			if (receive != null) {
				String mstr = receive.getSender().getLocalName(), slv = interacciones.get(mstr);
				interacciones.remove(mstr);
				interacciones.remove(slv);
			}
		}
		
	}
	
	// Empareja personajes en la misma localización
	private class Empareja extends CyclicBehaviour {

		@Override
		public void action() {
			for (String loc : estado.getLocations().split(" ")) {
				for (String pers : estado.getPersonajes(loc).split(" ")) {
					if (interacciones.get(pers) == null) {
						String pareja = buscaPareja(pers);
						
						if (!pareja.equals("")) {
							enviaNotificacion(pers, pareja, loc);
							
							AgenteMundo.this.addBehaviour(new ConfirmaEmparejamiento(pers, pareja));
						}
					}
				}
			}
		}
		
		public String buscaPareja(String pers) {
			// TODO: Comprobar que la pareja no esté interactuando ya 
			String [] persEnLoc = estado.personajesEnLoc(pers).split(" ");
			
			return persEnLoc[(new Random()).nextInt(persEnLoc.length)];
		}
		
		public void enviaNotificacion(String mstr, String slv, String loc) {
			ACLMessage notify = new ACLMessage(ACLMessage.INFORM);
			notify.addReceiver(new AID(mstr, AID.ISLOCALNAME));
			notify.addReceiver(new AID(slv, AID.ISLOCALNAME));
			notify.setConversationId("Emparejamiento");
			notify.setContent(loc);
			send(notify);
		}
		
	}
	
	// Recibe la respuesta de las parejas. Si ambas OK, la interacción comienza.
//	private class ConfirmaEmparejamiento extends OneShotBehaviour {
//		private String mstr, slv;
//		
//		public ConfirmaEmparejamiento(String master, String slave) {
//			mstr = master;
//			slv = slave;
//		}
//		@Override
//		public void action() {
//			MessageTemplate mt = MessageTemplate.MatchConversationId("Emparejamiento");
//			
//			// Recibe mensaje mstr
//			ACLMessage receive_mstr = myAgent.receive(mt);
//			
//			// Recibe mensaje slv
//			ACLMessage receive_slv = myAgent.receive(mt);
//			
//			while(receive_mstr == null || receive_slv == null) {
//				if (receive_mstr == null) receive_mstr = myAgent.receive(mt);
//				if (receive_slv == null) receive_slv = myAgent.receive(mt);
//				block();
//			}
//			
//			// Si ambos responden OK (están en la misma loc) se continúa con la interacción
//			
//			ACLMessage ok_mstr = new ACLMessage(ACLMessage.INFORM), ok_slv = new ACLMessage(ACLMessage.INFORM);
//			
//			ok_mstr.addReceiver(receive_mstr.getSender());
//			ok_slv.addReceiver(receive_slv.getSender());
//			
//			ok_mstr.setConversationId("Confirma_emparejamiento");
//			ok_slv.setConversationId("Confirma_emparejamiento");
//			
//			if (receive_mstr.getPerformative() == ACLMessage.CONFIRM && 
//					receive_slv.getPerformative() == ACLMessage.CONFIRM) {
//				
//				String master = receive_mstr.getSender().getLocalName(), 
//						slave = receive_slv.getSender().getLocalName();
//				
//				interacciones.put(master, slave);
//				interacciones.put(slave, master);
//				
//				ok_mstr.setPerformative(ACLMessage.CONFIRM);
//				ok_slv.setPerformative(ACLMessage.CONFIRM);
//				
//				ok_mstr.setContent(slave + " MSTR");
//				ok_slv.setContent(mstr + " SLV");
//			} else {
//				ok_mstr.setPerformative(ACLMessage.FAILURE);
//				ok_slv.setPerformative(ACLMessage.FAILURE);
//			}
//			
//			send(ok_mstr);
//		}
//	}
	
	// TODO: Comprobar receptores
	private abstract class RecibeMensajeEmparejamiento extends SimpleBehaviour {

		private boolean finished;
		private ACLMessage msg;
		private String sender; 
		
		public RecibeMensajeEmparejamiento(String snd) {
			sender = snd;
		}
		
		public boolean done() {
			return finished;
		}

		public void action() {
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchConversationId("Emparejamiento"),
					MessageTemplate.MatchSender(new AID(sender, AID.ISLOCALNAME)));
			
			msg = myAgent.receive(mt);

			if( msg != null) {
				finished = true;
				handle(msg);
				return;
			}
		}
		
		public int onEnd() {
			System.out.println("Fin " + sender);
			return 0;
		}
		
		public abstract void handle( ACLMessage m);
	}
	
	private class ConfirmaEmparejamiento extends SequentialBehaviour {
		
		private int msg1performative, msg2performative;
		private String mstr, slv;
		
		public ConfirmaEmparejamiento(String master, String slave) {
			super();
			mstr = master;
			slv = slave;
			
			ParallelBehaviour par = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);
			
			par.addSubBehaviour(new RecibeMensajeEmparejamiento(mstr) {
				public void handle(ACLMessage m) {
					msg1performative = m.getPerformative();
				}
			});
			
			par.addSubBehaviour(new RecibeMensajeEmparejamiento(slv) {
				public void handle(ACLMessage m) {
					msg2performative = m.getPerformative();
				}
			});
			
			addSubBehaviour(par);
			
			OneShotBehaviour confirm = new OneShotBehaviour() {
				@Override
				public void action() {
					ACLMessage ok_mstr = new ACLMessage(ACLMessage.INFORM), ok_slv = new ACLMessage(ACLMessage.INFORM);
					
					ok_mstr.addReceiver(new AID(mstr, AID.ISLOCALNAME));
					ok_slv.addReceiver(new AID(slv, AID.ISLOCALNAME));
					
					ok_mstr.setConversationId("Confirma_emparejamiento");
					ok_slv.setConversationId("Confirma_emparejamiento");
					
					if (msg1performative == ACLMessage.CONFIRM && 
							msg2performative == ACLMessage.CONFIRM) {
						
						interacciones.put(mstr, slv);
						interacciones.put(slv, mstr);
						
						ok_mstr.setPerformative(ACLMessage.CONFIRM);
						ok_slv.setPerformative(ACLMessage.CONFIRM);
						
						ok_mstr.setContent(slv + " MSTR");
						ok_slv.setContent(mstr + " SLV");
					} else {
						ok_mstr.setPerformative(ACLMessage.FAILURE);
						ok_slv.setPerformative(ACLMessage.FAILURE);
					}
					
					send(ok_mstr);
				}
			};
			
			addSubBehaviour(confirm);
		}
	}

	private class PeticionesDePersonajes extends CyclicBehaviour {

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchConversationId("peerRequest"));
			ACLMessage receive = myAgent.receive(mt);

			// Si no está interactuando con nadie, se busca una pareja 
			// que tampoco esté interactuando con nadie, y se añade al Map de interacciones. 
			if (receive != null) {
				String sender = receive.getSender().getLocalName();
				//buscandoPareja.add(sender);
				

				ACLMessage reply = receive.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				
				if (interacciones.get(sender) == null) {
					// El personaje no está interactuando, por lo que habrá que buscarle pareja
					String pareja = buscaPareja(sender);
					
					if (!estaInteractuando(sender) && !pareja.equalsIgnoreCase("")) {
						interacciones.put(sender, pareja);
						interacciones.put(pareja, sender);

						reply.setContent(pareja + " MSTR");
						send(reply);
						
					} else if (pareja.equalsIgnoreCase("")) 
						send(reply);	
					
				} else {
					// El personaje ya tiene una pareja asignada
					reply.setContent(interacciones.get(sender) + " SLV");
					send(reply);
				}
				
				//buscandoPareja.remove(sender);
			} else block();
		}
		
		private String buscaPareja(String personaje) {
			String [] persEnLoc = estado.personajesEnLoc(personaje).split(" ");
			
//			for (String p : buscandoPareja)
//				if (estado.estanMismaLocalizacion(personaje, p) && 
//						!p.equalsIgnoreCase(personaje) && !estaInteractuando(p))
//					pareja = p;
			
			return persEnLoc[0];
		}
		
		private boolean estaInteractuando(String personaje) {
			return interacciones.containsKey(personaje) || interacciones.containsValue(personaje);
		}

	}

	private class ToPDDLfile extends CyclicBehaviour {

		public void action() {

			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchConversationId("toPDDL"));
			ACLMessage receive = myAgent.receive(mt);

			if (receive != null) {
				ACLMessage reply = receive.createReply();

				String[] mensaje = receive.getContent().split(" ");
				String clase = mensaje[0];
				String nombrePersonaje = mensaje[1];
				String nombrePrincesa = mensaje[2];
				String problema = "";

				problema += "(define (problem " + clase + ")" + "\n";
				problema += "(:domain Historia)" + "\n";
				problema += "\n";
				problema += "(:objects" + "\n";

				problema += estado.nombresToString();

				problema += ")" + "\n";

				problema += "(:init" + "\n";
				problema += estado.toString();
				problema += ")\n";

				try {
					File fXmlFile = new File("Objetivos.xml");
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory
							.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(fXmlFile);

					doc.getDocumentElement().normalize();

					NodeList nList = doc.getElementsByTagName("personaje");

					for (int temp = 0; temp < nList.getLength(); temp++) {

						Node nNode = nList.item(temp);

						if (nNode.getNodeType() == Node.ELEMENT_NODE) {

							Element eElement = (Element) nNode;

							if (eElement.getAttribute("tipo").equalsIgnoreCase(
									clase)) {

								String cadena = eElement
										.getElementsByTagName("objetivo")
										.item(0).getTextContent();

								String objetivo = cadena
										.replace("Dragon", nombrePersonaje)
										.replace("Caballero", nombrePersonaje)
										.replace("Princesa", nombrePrincesa);

								problema += "(:goal \n" + objetivo + "\n)"
										+ "\n)";
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}

				PrintWriter writer;
				try {
					writer = new PrintWriter(nombrePersonaje + ".pddl", "UTF-8");
					writer.println(problema);
					writer.close();

				} catch (Exception e) {
				}

				send(reply);
			} else
				block();
		}
	}

	private class LocalizarPersonajes extends CyclicBehaviour {

		public void action() {

			boolean ok = true;

			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchConversationId("Mover"));
			ACLMessage receive = myAgent.receive(mt);

			if (receive != null) {
				ACLMessage reply = receive.createReply();
				String[] mensaje = receive.getContent().split(" ");
				AID personaje = receive.getSender();

				String locOrigen, locDest;

				if (mensaje[1].equalsIgnoreCase("pasear")) {
					locDest = mapa.dameLocAdyacente(mensaje[2]);
				}

				else 
					locDest = mensaje[1];


				Localizacion loc2 = mapa.getLocalizacion(locDest);

				if (mensaje.length == 3) {
					locOrigen = mensaje[2];
					Localizacion loc1 = mapa.getLocalizacion(locOrigen);

					if (loc1 != null && loc1.existeConexion(locDest))
						ok = loc1.eliminarPersonaje(personaje.getLocalName());

					else
						ok = false;
				}

				if (ok && loc2 != null) {
					loc2.anadirPersonaje(personaje.getLocalName());
					estado.anadirLocalizacion(personaje.getLocalName(), locDest);
					reply.setPerformative(ACLMessage.CONFIRM);
					reply.setContent(loc2.getNombre());

					if (mensaje.length == 2) {
						estado.anadirPersonaje(mensaje[0],
								personaje.getLocalName());
						estado.anadirCasa(personaje.getLocalName(), locDest);
						estado.anadirNombre(personaje.getLocalName());
					}
				} else
					reply.setPerformative(ACLMessage.FAILURE);

				send(reply);
				//eliminaInteraccion(personaje.getLocalName());

			} else
				block();
		}
		private void eliminaInteraccion(String personaje) {
			String pareja = interacciones.get(personaje);
			interacciones.remove(personaje);
			interacciones.remove(pareja);
		}
		
	}

	private class MoverPrincesaSecuestrada extends CyclicBehaviour {

		MessageTemplate mt;

		public void action() {

			mt = MessageTemplate
					.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
							MessageTemplate
							.MatchConversationId("Mundo-Mover-Princesa"));
			ACLMessage receive = myAgent.receive(mt);

			if (receive != null) {

				String[] contenido = receive.getContent().split(" ");
				ACLMessage moverPrincesa = new ACLMessage(ACLMessage.REQUEST);
				moverPrincesa.setReplyWith("mover-princesa"
						+ System.currentTimeMillis());
				moverPrincesa.setConversationId("Mover-Princesa");
				moverPrincesa.setContent(contenido[0] + " "
						+ receive.getSender().getLocalName());
				AID princesa = new AID(
						(String) estado.nombreCorrecto(contenido[1]),
						AID.ISLOCALNAME);
				moverPrincesa.addReceiver(princesa);
				send(moverPrincesa);

				mt = MessageTemplate.MatchInReplyTo(moverPrincesa
						.getReplyWith());
				ACLMessage rec1 = receive(mt);
				while (rec1 == null)
					block();

				send(receive.createReply());

			} else
				block();
		}
	}

	private class Secuestro extends CyclicBehaviour {

		public void action() {

			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchConversationId("Secuestro"));
			ACLMessage receive = myAgent.receive(mt);

			if (receive != null) {

				ACLMessage reply = receive.createReply();
				String princesa = receive.getContent();
				String secuestrador = receive.getSender().getLocalName();

				if (estado.estanMismaLocalizacion(secuestrador, princesa)) {
					estado.anadirPersonajeConPrincesa(secuestrador, princesa);
					estado.secuestrar(princesa);
					estado.estaLlenoPersonaje(secuestrador);

					reply.setContent(estado.nombreCorrecto(princesa));

				} else
					reply.setContent("fallo");

				myAgent.send(reply);

			} else
				block();
		}

	}

	private class Liberar extends CyclicBehaviour {

		public void action() {

			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchConversationId("Liberar"));

			ACLMessage receive = myAgent.receive(mt);

			if (receive != null) {

				String[] contenido = receive.getContent().split(" ");
				estado.estaLlenoPersonaje(contenido[0]);
				estado.anadirPersonajeConPrincesa(contenido[0], contenido[1]);
				estado.borrarPersonajeConPrincesa(contenido[2]);

				ACLMessage reply = receive.createReply();
				reply.setContent(estado.nombreCorrecto(contenido[1]));
				send(reply);

			} else
				block();
		}
	}

	private class PersonajeEnCasa extends CyclicBehaviour {

		public void action() {

			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchConversationId("Dejar en Casa"));

			ACLMessage receive = myAgent.receive(mt);

			if (receive != null) {

				String princesa = receive.getContent();
				estado.liberar(princesa);
				estado.borrarPersonajeConPrincesa(receive.getSender()
						.getLocalName());
				estado.anadirPrincesaSalvada(princesa);
				estado.estaLibrePersonaje(receive.getSender().getLocalName());

				ACLMessage reply = receive.createReply();
				reply.setContent(estado.nombreCorrecto(princesa));
				send(reply);

			} else
				block();
		}
	}

	private class ConvertirEnHeroe extends CyclicBehaviour {

		public void action() {

			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchConversationId("Ser Heroe"));

			ACLMessage receive = myAgent.receive(mt);

			if (receive != null) {

				estado.anadirHeroe(receive.getSender().getLocalName());
				send(receive.createReply());

			} else
				block();

		}
	}

	private class MuertePersonaje extends CyclicBehaviour {

		public void action() {

			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchConversationId("Muerto"));
			ACLMessage receive = myAgent.receive(mt);

			if (receive != null) {
				estado.mata(receive.getContent());
				send(receive.createReply());

			} else
				block();
		}
	}

}
