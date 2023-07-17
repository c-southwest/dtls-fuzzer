package se.uu.it.dtlsfuzzer.components.sul.core.config;

import com.beust.jcommander.ParametersDelegate;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulAdapterConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.config.SulClientConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConfigStandard;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.config.MapperConnectionConfig;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.config.delegate.ServerDelegate;
import de.rub.nds.tlsattacker.core.exceptions.ConfigurationException;

public class DtlsSulClientConfig extends SulClientConfigStandard implements ConfigDelegateProvider {

    @ParametersDelegate
    private ClientConfigDelegate configDelegate;

    public DtlsSulClientConfig() {
        super(new MapperConfigStandard(), new SulAdapterConfigStandard());
        configDelegate = new ClientConfigDelegate();
    }

    @Override
    public void applyDelegate(MapperConnectionConfig config) {
    }


    @Override
    public ConfigDelegate getConfigDelegate() {
        return configDelegate;
    }

    private class ClientConfigDelegate extends ConfigDelegate {
        ClientConfigDelegate() {
        }

        @Override
        public void applyDelegate(Config config) throws ConfigurationException {
            super.applyDelegate(config);
            ServerDelegate serverDelegate = new ServerDelegate();
            serverDelegate.setPort(getPort());
            serverDelegate.applyDelegate(config);
            config.getDefaultServerConnection().setTimeout(getResponseWait().intValue());
            config.getDefaultServerConnection().setFirstTimeout(getResponseWait().intValue());
        }
    }
}
