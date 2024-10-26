package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilderTransformer;
import de.learnlib.ralib.data.DataType;
import de.learnlib.ralib.words.InputSymbol;
import de.learnlib.ralib.words.OutputSymbol;
import de.learnlib.ralib.words.ParameterizedSymbol;
import java.util.HashMap;
import java.util.Map;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsInput;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.inputs.TlsOutputSymbol;

public class TlsInputTransformer extends AlphabetBuilderTransformer<TlsInput, ParameterizedSymbol> {
    // Map<DtlsInput, DataType[]> dataTypes; An idea, shared for all symbols for
    // now.
    public final DataType[] dataTypes;
    private Map<InputSymbol, TlsInput> translationMap = new HashMap<>();

    public TlsInputTransformer(AlphabetBuilderStandard<TlsInput> tlsAlphabetBuilder,
            DataType[] dataTypes) {
        super(tlsAlphabetBuilder);
        this.dataTypes = dataTypes;
    }

    @Override
    public TlsInput fromTransformedInput(ParameterizedSymbol ti) {
        return translationMap.get(ti);
    }

    @Override
    public ParameterizedSymbol toTransformedInput(TlsInput ri) {
        String symbolName = ri.getName();
        // DataType[] empty = {}; // The default value if no DataTypes are defined
        // DataType[] typesForSymbol = this.dataTypes.getOrDefault(ri, empty);
        if (ri instanceof TlsOutputSymbol) {
            return new OutputSymbol(symbolName);
        }
        InputSymbol inputSymbol = new InputSymbol(symbolName, this.dataTypes);

        translationMap.put(inputSymbol, ri);
        return inputSymbol;
    }

}
