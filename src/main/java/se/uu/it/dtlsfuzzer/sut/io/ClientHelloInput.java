package se.uu.it.dtlsfuzzer.sut.io;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAttribute;

import de.rub.nds.tlsattacker.core.constants.CipherSuite;
import de.rub.nds.tlsattacker.core.dtls.MessageFragmenter;
import de.rub.nds.tlsattacker.core.protocol.message.ClientHelloMessage;
import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.protocol.message.HandshakeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.state.State;

public class ClientHelloInput extends NamedTlsInput {

	@XmlAttribute(name = "suite", required = true)
	private CipherSuite suite;

	/**
	 * option needed to learn DTLS implementations which use cookie-less
	 * handshake messages
	 */
	@XmlAttribute(name = "forceDigest", required = false)
	private boolean forceDigest = false;

	private ProtocolMessage message;

	public ClientHelloInput() {
		super("CLIENT_HELLO");
	}

	public ClientHelloInput(CipherSuite cipherSuite) {
		super(cipherSuite.name() + "_CLIENT_HELLO");
		this.suite = cipherSuite;
	}

	@Override
	public ProtocolMessage generateMessage(State state) {
		state.getConfig().setDefaultClientSupportedCiphersuites(
				Arrays.asList(suite));
		if (suite.name().contains("EC")) {
			state.getConfig().setAddECPointFormatExtension(true);
			state.getConfig().setAddEllipticCurveExtension(true);
		} else {
			state.getConfig().setAddECPointFormatExtension(false);
			state.getConfig().setAddEllipticCurveExtension(false);
		}
		state.getTlsContext().getDigest().reset();
		ClientHelloMessage message = new ClientHelloMessage(state.getConfig());
		this.message = message;
		return message;
	}

	@Override
	public TlsInputType getInputType() {
		return TlsInputType.HANDSHAKE;
	}

	public void postSendUpdate(State state) {
		if (forceDigest) {
			DtlsHandshakeMessageFragment fragment = new MessageFragmenter(state
					.getTlsContext().getConfig()).wrapInSingleFragment(
					(HandshakeMessage) message, state.getTlsContext());
			state.getTlsContext().getDigest()
					.append(fragment.getCompleteResultingMessage().getValue());
		}
	}

}
