package de.rub.nds.modelfuzzer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alexmerz.graphviz.ParseException;
import com.pfg666.dotparser.fsm.mealy.MealyDotParser;

import de.learnlib.api.SUL;
import de.learnlib.oracles.SULOracle;
import de.rub.nds.modelfuzzer.config.ModelBasedFuzzerConfig;
import de.rub.nds.modelfuzzer.fuzz.FragmentationBug;
import de.rub.nds.modelfuzzer.fuzz.FragmentationStrategy;
import de.rub.nds.modelfuzzer.fuzz.FuzzingReport;
import de.rub.nds.modelfuzzer.fuzz.SpecificationBug;
import de.rub.nds.modelfuzzer.learn.Extractor;
import de.rub.nds.modelfuzzer.learn.StateMachine;
import de.rub.nds.modelfuzzer.sut.ProcessHandler;
import de.rub.nds.modelfuzzer.sut.SulProcessWrapper;
import de.rub.nds.modelfuzzer.sut.TlsSUL;
import de.rub.nds.modelfuzzer.sut.io.AlphabetFactory;
import de.rub.nds.modelfuzzer.sut.io.AlphabetSerializer;
import de.rub.nds.modelfuzzer.sut.io.FragmentedTlsInput;
import de.rub.nds.modelfuzzer.sut.io.TlsInput;
import de.rub.nds.modelfuzzer.sut.io.TlsOutput;
import de.rub.nds.modelfuzzer.sut.io.TlsProcessor;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.automata.transout.impl.FastMealyState;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

public class ModelBasedFuzzer {
	private static final Logger LOGGER = LogManager.getLogger(ModelBasedFuzzer.class);
	private ModelBasedFuzzerConfig config;

	public ModelBasedFuzzer(ModelBasedFuzzerConfig config) {
		this.config = config;
	}
	

	public FuzzingReport startFuzzing() throws ParseException, IOException {
		SULOracle<TlsInput, TlsOutput> sutOracle = createOracle(config);
		FastMealy<TlsInput, String> model = generateModel(config);
		FuzzingReport report = fuzzModel(sutOracle, model);
		logResult(report, config);
		return report;
	}
	
	private void logResult(FuzzingReport report, ModelBasedFuzzerConfig config) throws IOException {
		if (config.getOutput() == null) {
			report.printReport(System.out);
		} else {
			PrintStream ps = new PrintStream(new FileOutputStream(config.getOutput()));
			report.printReport(ps);
		}
	}


	public SULOracle<TlsInput, TlsOutput> createOracle(ModelBasedFuzzerConfig config) {
		SUL<TlsInput, TlsOutput> tlsSut = new TlsSUL(config.getSulDelegate());
		if (config.getSulDelegate().getCommand() != null) {
			tlsSut = new SulProcessWrapper<>(tlsSut, 
					new ProcessHandler(config.getSulDelegate().getCommand(), 
							config.getSulDelegate().getRunWait()));
		}
		SULOracle<TlsInput, TlsOutput> tlsOracle = new SULOracle<TlsInput, TlsOutput>(tlsSut);
		return tlsOracle;
	}
	
	public ModelBasedTestingTask generateModelBasedTestingTask(ModelBasedFuzzerConfig config) throws FileNotFoundException, ParseException {
		Alphabet<TlsInput> alphabet = AlphabetFactory.buildConfiguredAlphabet(config);
		if (config.getSpecification() == null) {
			if (alphabet == null) {
				alphabet = AlphabetFactory.buildDefaultAlphabet();
			}
			Extractor extractor = new Extractor(config, alphabet);
			StateMachine model = extractor.extractStateMachine().getLearnedModel();
			MealyMachine<?, TlsInput, ?, TlsOutput> mealyMachine = model.getMealyMachine();
			return new ModelBasedTestingTask(model.getMealyMachine(), alphabet);
		}
		else {
			MealyDotParser<TlsInput, String> dotParser = new MealyDotParser<>(new TlsProcessor());
			FastMealy<TlsInput, String>  model = dotParser.parseAutomaton(config.getSpecification()).get(0);
			if (alphabet == null) {
				alphabet = model.getInputAlphabet();
				alphabet = model.
			}
			return model;
		}
	}
	
	private FuzzingReport fuzzModel(SULOracle<TlsInput, TlsOutput> tlsOracle, FastMealy<TlsInput, String> specification) {
		LOGGER.info("Starting fuzzing");
		FuzzingReport report = new FuzzingReport();
		Alphabet<TlsInput> inputs = specification.getInputAlphabet();

		List<Word<TlsInput>> stateCover = Automata.stateCover(specification, inputs);

		int testNumber = 0;
		int initNumFrag = 2;
		int maxNumFrags = 3;
		
		for (Word<TlsInput> statePrefix : stateCover) {
			FastMealyState<String> state = specification.getState(statePrefix);
			List<Word<TlsInput>> charSuffixes = Automata.stateCharacterizingSet(specification, inputs, state);
			if (charSuffixes.isEmpty())
				charSuffixes = Collections.singletonList(Word.<TlsInput>epsilon());
			for (TlsInput input : inputs) {
				for (Word<TlsInput> suffix : charSuffixes) {
					if (config.getBound() != null && testNumber >= config.getBound()) {
						return report;
					}
					testNumber ++;
					
					Word<TlsInput> regularWord = new WordBuilder<TlsInput>()
							.append(statePrefix)
							.append(input)
							.append(suffix)
							.toWord();
					Word<TlsOutput> regularOutput = tlsOracle.answerQuery(regularWord);
					
					Word<String> specificationOutputString = specification.computeOutput(regularWord);
					
					Word<String> regularOutputString = regularOutput.transform(o -> o.toString());
					if (!specificationOutputString.equals(regularOutputString)) {
						SpecificationBug bug = new SpecificationBug(state, statePrefix, regularWord, specificationOutputString, regularOutputString);
						report.addItem(bug);
						if (!config.isExhaustive())
							break;
						continue;
					}
					
					boolean bugsFound = false;
					for (int numFrags=initNumFrag; numFrags<maxNumFrags; numFrags ++) {
						TlsInput fuzzedInput = new FragmentedTlsInput(input, initNumFrag, FragmentationStrategy.EVEN);
						Word<TlsInput> fuzzedWord = new WordBuilder<TlsInput>()
								.append(statePrefix)
								.append(fuzzedInput)
								.append(suffix)
								.toWord();
						Word<TlsOutput> fuzzedOutput = tlsOracle.answerQuery(fuzzedWord);
						
						if (!fuzzedOutput.equals(regularOutput)) {
							FragmentationBug bug = new FragmentationBug(state, statePrefix, fuzzedWord, regularOutput, fuzzedOutput);
							report.addItem(bug);
							bugsFound = true;
							break;
						}
					}
					
					if (bugsFound && !config.isExhaustive()) {
						break;
					}
				}
			}
		}
		
		return report;
	}
}
