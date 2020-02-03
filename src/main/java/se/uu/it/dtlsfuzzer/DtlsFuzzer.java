package se.uu.it.dtlsfuzzer;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alexmerz.graphviz.ParseException;
import com.pfg666.dotparser.fsm.mealy.MealyDotParser;

import de.learnlib.api.SUL;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.oracle.membership.SULOracle;
import net.automatalib.automata.transducers.impl.FastMealy;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import se.uu.it.dtlsfuzzer.config.DtlsFuzzerConfig;
import se.uu.it.dtlsfuzzer.execute.TestingInputExecutor;
import se.uu.it.dtlsfuzzer.learn.Extractor;
import se.uu.it.dtlsfuzzer.learn.Extractor.ExtractorResult;
import se.uu.it.dtlsfuzzer.sut.IsAliveWrapper;
import se.uu.it.dtlsfuzzer.sut.ResettingWrapper;
import se.uu.it.dtlsfuzzer.sut.TlsProcessWrapper;
import se.uu.it.dtlsfuzzer.sut.TlsSUL;
import se.uu.it.dtlsfuzzer.sut.io.AlphabetFactory;
import se.uu.it.dtlsfuzzer.sut.io.TlsInput;
import se.uu.it.dtlsfuzzer.sut.io.TlsOutput;
import se.uu.it.dtlsfuzzer.sut.io.definitions.Definitions;
import se.uu.it.dtlsfuzzer.sut.io.definitions.DefinitionsFactory;

public class DtlsFuzzer {
	private static final Logger LOGGER = LogManager.getLogger(DtlsFuzzer.class);

	private DtlsFuzzerConfig config;
	private CleanupTasks cleanupTasks;

	public DtlsFuzzer(DtlsFuzzerConfig config) {
		this.config = config;
		this.cleanupTasks = new CleanupTasks();
	}

	public void startTesting() throws ParseException, IOException {
		try {
			if (config.getTestRunnerConfig().getTest() != null) {
				runTest(config);
			} else {
				// setting up our output directory
				File folder = new File(config.getOutput());
				folder.mkdirs();
				extractModel(config);
			}
		} catch (Exception e) {
			cleanupTasks.execute();
			throw e;
		}
		cleanupTasks.execute();
	}

	private void runTest(DtlsFuzzerConfig config) throws IOException,
			ParseException {
		MealyMembershipOracle<TlsInput, TlsOutput> sutOracle = createTestOracle(config);
		Alphabet<TlsInput> alphabet = AlphabetFactory.buildAlphabet(config);
		TestRunner runner = new TestRunner(config.getTestRunnerConfig(),
				alphabet, sutOracle);
		List<TestRunnerResult<TlsInput, TlsOutput>> results = runner.runTests();
		for (TestRunnerResult<TlsInput, TlsOutput> result : results) {
			LOGGER.error(result.toString());
			if (config.getSpecification() != null) {
				Definitions definitions = DefinitionsFactory
						.generateDefinitions(alphabet);
				MealyDotParser<TlsInput, TlsOutput> dotParser = new MealyDotParser<>(
						new TlsProcessor(definitions));
				FastMealy<TlsInput, TlsOutput> machine = dotParser
						.parseAutomaton(config.getSpecification()).get(0);
				Word<TlsOutput> outputWord = machine.computeOutput(result
						.getInputWord());
				LOGGER.error("Expected output: " + outputWord);
			}
		}
	}

	public MealyMembershipOracle<TlsInput, TlsOutput> createTestOracle(
			DtlsFuzzerConfig config) {
		TlsSUL actualSut = new TlsSUL(config.getSulDelegate(),
				new TestingInputExecutor());
		SUL<TlsInput, TlsOutput> tlsSut = actualSut;
		if (config.getSulDelegate().getCommand() != null) {
			tlsSut = new TlsProcessWrapper(tlsSut, config.getSulDelegate());
		}
		if (config.getSulDelegate().getResetPort() != null) {
			ResettingWrapper<TlsInput, TlsOutput> wrapper = new ResettingWrapper<>(
					tlsSut, config.getSulDelegate(), cleanupTasks);
			actualSut.setDynamicPortProvider(wrapper);
			tlsSut = wrapper;
		}
		tlsSut = new IsAliveWrapper(tlsSut);

		MealyMembershipOracle<TlsInput, TlsOutput> tlsOracle = new SULOracle<TlsInput, TlsOutput>(
				tlsSut);

		return tlsOracle;
	}

	private ExtractorResult extractModel(DtlsFuzzerConfig config) {
		Alphabet<TlsInput> alphabet = AlphabetFactory.buildAlphabet(config);
		Extractor extractor = new Extractor(config, alphabet, cleanupTasks);
		ExtractorResult result = extractor.extractStateMachine();
		return result;
	}
}
