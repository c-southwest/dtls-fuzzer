package se.uu.it.dtlsfuzzer.components.sul.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulConfig;
import de.learnlib.ralib.words.PSymbolInstance;
import se.uu.it.dtlsfuzzer.components.sul.core.config.TlsSulConfig;
import se.uu.it.dtlsfuzzer.components.sul.mapper.DtlsInputMapper;
import se.uu.it.dtlsfuzzer.components.sul.mapper.DtlsMapperComposer;
import se.uu.it.dtlsfuzzer.components.sul.mapper.DtlsOutputMapper;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsExecutionContextRA;
import se.uu.it.dtlsfuzzer.components.sul.mapper.TlsInputTransformer;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputBuilder;
import se.uu.it.dtlsfuzzer.components.sul.mapper.symbols.outputs.TlsOutputChecker;

public class TlsSulBuilderRA implements SulBuilder<PSymbolInstance, PSymbolInstance, TlsExecutionContextRA> {

    private TlsSulAdapter sulAdapter = null;
    private TlsInputTransformer inputTransformer = null;

    public TlsSulBuilderRA(TlsInputTransformer transformer) {
        inputTransformer = transformer;
    }

    @Override
    public TlsSulRA build(SulConfig sulConfig,
            com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks cleanupTasks) {

        TlsOutputBuilder outputBuilder = new TlsOutputBuilder();
        TlsOutputChecker outputChecker = new TlsOutputChecker();

        DtlsOutputMapper outputMapper = new DtlsOutputMapper(sulConfig.getMapperConfig(), outputBuilder, outputChecker);
        DtlsInputMapper inputMapper = new DtlsInputMapper(sulConfig.getMapperConfig(), outputChecker);

        DtlsMapperComposer mapperComposer = new DtlsMapperComposer(inputMapper, outputMapper);

        TlsSulRA tlsSul = new TlsSulRA((TlsSulConfig) sulConfig, sulConfig.getMapperConfig(), mapperComposer,
                cleanupTasks, inputTransformer);

        if (sulConfig.getSulAdapterConfig().getAdapterPort() != null) {
            if (sulAdapter == null) {
                sulAdapter = new TlsSulAdapter(sulConfig.getSulAdapterConfig(), cleanupTasks,
                        sulConfig.isFuzzingClient());
            }
            tlsSul.setSulAdapter(sulAdapter);
        }
        return tlsSul;
    }
}
