package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import java.util.Collections;
import java.util.List;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

/**
 * The outputs used in learning can be messages/records, application data, and
 * information about the state of the SUT (is it still alive).
 *
 * We restrict ourselves only to the received message types.
 */
public class TlsOutput extends AbstractOutput<TlsOutput, TlsProtocolMessage> {

    public TlsOutput(String name) {
        super(name);
        this.messages = Collections.emptyList();
    }

    public TlsOutput(String name, List<TlsProtocolMessage> messages) { // TODO: guess, since generic protocolMessage
                                                                       // seems to be removed.
        super(name, Collections.emptyList());
    }

    @Override
    protected TlsOutput buildOutput(String name) {
        return new TlsOutput(name);
    }

    @Override
    protected TlsOutput convertOutput() {
        // TODO I don't know the intended purpose for this method.
        // Supposedly it converts abstract output to O, but O is a subclass of abstract
        // output here, so is this needed?
        throw new UnsupportedOperationException("Unimplemented method 'convertOutput'");
    }
}
