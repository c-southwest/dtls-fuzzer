package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderTransformer;
import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.words.InputSymbol;
import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.ParameterizedSymbol;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsOutputSymbol;

public class TlsInputTransformer extends AlphabetBuilderTransformer<TlsInput, ParameterizedSymbol> {
    // Map<DtlsInput, DataType[]> dataTypes; An idea, shared for all symbols for
    // now.
    DataType[] dataTypes;

    public TlsInputTransformer(AlphabetBuilderStandard<TlsInput> tlsAlphabetBuilder,
            DataType[] dataTypes) {
        super(tlsAlphabetBuilder);
        this.dataTypes = dataTypes;
    }

    @Override
    public TlsInput fromTransformedInput(ParameterizedSymbol ti) {
        throw new RuntimeException("Transforming an alphabet to TlsInput is as of yet not supported.");
    }

    @Override
    public ParameterizedSymbol toTransformedInput(TlsInput ri) {
        String symbolName = ri.getName();
        // DataType[] empty = {}; // The default value if no DataTypes are defined
        // DataType[] typesForSymbol = this.dataTypes.getOrDefault(ri, empty);
        if (ri instanceof TlsOutputSymbol) {
            return new OutputSymbol(symbolName, this.dataTypes);
        }

        return new InputSymbol(symbolName, this.dataTypes);
    }

}
