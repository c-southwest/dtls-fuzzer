package se.uu.it.dtlsfuzzer.sut.input;

import de.rub.nds.tlsattacker.core.protocol.message.DHEServerKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ECDHEServerKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.PskServerKeyExchangeMessage;
import de.rub.nds.tlsattacker.core.protocol.message.TlsMessage;
import de.rub.nds.tlsattacker.core.state.State;
import javax.xml.bind.annotation.XmlAttribute;
import se.uu.it.dtlsfuzzer.mapper.ExecutionContext;

/*
 * This input assumes that DH is DHE and ECDH is ECDHE
 */
public class ServerKeyExchangeInput extends DtlsInput {

    @XmlAttribute(name = "algorithm", required = true)
    private KeyExchangeAlgorithm algorithm;

    public ServerKeyExchangeInput() {
        super("SERVER_KEY_EXCHANGE");
    }

    public ServerKeyExchangeInput(KeyExchangeAlgorithm algorithm) {
        super(algorithm + "_SERVER_KEY_EXCHANGE");
        this.algorithm = algorithm;
    }

    @Override
    public TlsMessage generateMessage(State state, ExecutionContext context) {
        if (algorithm == null) {
            throw new RuntimeException("Algorithm not set");
        }
        switch (algorithm) {
            case DH :
                return new DHEServerKeyExchangeMessage(state.getConfig());
            case ECDH :
                return new ECDHEServerKeyExchangeMessage(state.getConfig());
            case PSK:
                return new PskServerKeyExchangeMessage(state.getConfig());
            default :
                throw new RuntimeException("Algorithm " + algorithm
                        + " not supported");
        }
    }

    public KeyExchangeAlgorithm getAlgorithm() {
        return algorithm;
    }

    @Override
    public TlsInputType getInputType() {
        return TlsInputType.HANDSHAKE;
    }

}
