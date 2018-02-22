import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

import rocks.xmpp.addr.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.TcpConnectionConfiguration;
import rocks.xmpp.core.session.XmppClient;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.session.debug.ConsoleDebugger;
import rocks.xmpp.core.stanza.model.Message;
import rocks.xmpp.extensions.httpbind.BoshConnectionConfiguration;
import rocks.xmpp.im.roster.RosterManager;
import rocks.xmpp.im.roster.model.Contact;

public class SimpleBabblerClient {

	public static void main(String[] args) {
		
		if (args.length < 2) {
			System.out.println("Invalid Arguments. Please provide username and password.");
			System.exit(0);
		}
		
		/*
		XmppSessionConfiguration xmppsessionconfiguration = XmppSessionConfiguration.builder()
	    	    .debugger(ConsoleDebugger.class)
	    	    .build();

		TcpConnectionConfiguration configuration = TcpConnectionConfiguration.builder()
			    .hostname("localhost")
			    .port(5222)
			    .secure(false)
			    .build();
		*/
		BoshConnectionConfiguration configuration = BoshConnectionConfiguration.builder()
			    .hostname("localhost")
			    .port(5280)
			    .path("/http-bind/")
			    .build();
		
		XmppClient xmppClient = XmppClient.create("localhost", /*xmppsessionconfiguration,*/ configuration);

		try {
			xmppClient.connect();

			xmppClient.login(args[0], args[1]);

			xmppClient.addInboundMessageListener(e -> {
				Message message = e.getMessage();
				System.out.println(message.getFrom()+":"+message.getBody());
			});

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String msg;

			RosterManager rosterManager = xmppClient.getManager(RosterManager.class);
			Collection<Contact> contacts = rosterManager.getContacts();
			for (Contact contact : contacts) {
				System.out.println(contact.getJid());
			}

			System.out.println("Who do you want to talk to? - Type contacts full email address:");
			String talkTo = br.readLine();

			System.out.println("All messages will be sent to " + talkTo);
			System.out.println("Enter your message in the console:");
			System.out.println("-----\n");

			while (!(msg = br.readLine()).equals("bye")) {
				xmppClient.send(new Message(Jid.of(talkTo), Message.Type.CHAT,msg));
			}

			xmppClient.close();
			System.exit(0);
		} catch (XmppException | IOException e) {
			e.printStackTrace();
		}
	}

}
