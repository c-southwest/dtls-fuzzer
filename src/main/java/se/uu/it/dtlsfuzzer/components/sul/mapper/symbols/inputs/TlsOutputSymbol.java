package se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs;

import jakarta.xml.bind.annotation.XmlAttribute;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContext;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsProtocolMessage;

public class TlsOutputSymbol extends DtlsInput {

    @XmlAttribute(name = "name", required = true)
    private String name;

    public TlsOutputSymbol() {
    }

    @Override
    public TlsProtocolMessage generateProtocolMessage(TlsExecutionContext context) {
        throw new RuntimeException(
                "OutputSymbol is meant to only be present during alphabet construction during RA-learning." +
                        "One possible source of this issue is using an RA-alphabet for Mealy learning.");
    }

    @Override
    public TlsInputType getInputType() {
        throw new RuntimeException(
                "OutputSymbol is meant to only be present during alphabet construction during RA-learning." +
                        "One possible source of this issue is using an RA-alphabet for Mealy learning.");
    }

}
