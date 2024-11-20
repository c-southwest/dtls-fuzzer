package se.uu.it.dtlsfuzzer.components.sul.mapper;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.Mapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputChecker;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfig;
import de.learnlib.ralib.words.PSymbolInstance;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputBuilderRA;

public class OutputBuilderWrapper implements Mapper<PSymbolInstance, PSymbolInstance, TlsExecutionContextRA> {

    TlsOutputBuilderRA outputBuilder;

    public OutputBuilderWrapper(TlsOutputBuilderRA outputBuilder) {
        this.outputBuilder = outputBuilder;
    }

    @Override
    public PSymbolInstance execute(PSymbolInstance input, TlsExecutionContextRA context) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

    @Override
    public MapperConfig getMapperConfig() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMapperConfig'");
    }

    @Override
    public OutputBuilder<PSymbolInstance> getOutputBuilder() {
        // TODO Auto-generated method stub
        return outputBuilder;
    }

    @Override
    public OutputChecker<PSymbolInstance> getOutputChecker() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getOutputChecker'");
    }

}
