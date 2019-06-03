package se.uu.it.modeltester.learn;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;

import de.learnlib.api.EquivalenceOracle;
import de.learnlib.api.MembershipOracle;
import de.learnlib.oracles.DefaultQuery;
import net.automatalib.automata.UniversalDeterministicAutomaton;
import net.automatalib.automata.concepts.Output;
import net.automatalib.commons.util.mappings.MutableMapping;
import net.automatalib.words.Word;

public class WSampledTestsEQOracle<A extends UniversalDeterministicAutomaton<?, I, ?, ?, ?> & Output<I, D>, I, D>
		implements EquivalenceOracle<A, I, D> {
	private List<Word<I>> tests;
	private MembershipOracle<I, D> sulOracle;
	private Random rand;
	private int bound;

	public WSampledTestsEQOracle(List<Word<I>> tests, MembershipOracle<I, D> sulOracle, long seed, int bound) {
		this.tests = tests;
		this.sulOracle = sulOracle;
		this.rand = new Random(seed);
		this.bound = bound;
	}

	@Override
	public DefaultQuery<I, D> findCounterExample(A hypothesis, Collection<? extends I> inputs) {
		UniversalDeterministicAutomaton<?, I, ?, ?, ?> aut = hypothesis;
		Output<I, D> out = hypothesis;
		return doFindCounterExample(aut, out, inputs);
	}
	
	/*
	 * Delegate target, used to bind the state-parameter of the automaton
	 */
	private <S> DefaultQuery<I, D> doFindCounterExample(
			UniversalDeterministicAutomaton<S, I, ?, ?, ?> hypothesis,
			Output<I, D> output, Collection<? extends I> inputs) {
		for (int i = 0; i < bound; i++) {
			S randState = hypothesis.getStates().stream().skip(rand.nextInt(hypothesis.getStates().size())).findFirst()
					.get();

			Word<I> randAccSeq = randomAccessSequence(hypothesis, inputs, randState);
			Word<I> randTest = tests.get(rand.nextInt(tests.size()));
			Word<I> randSuffix = randTest.suffix(rand.nextInt(randTest.length()));

			Word<I> test = randAccSeq.concat(randSuffix);

			DefaultQuery<I, D> query = new DefaultQuery<>(test);
			D hypOutput = output.computeOutput(test);
			sulOracle.processQueries(Collections.singleton(query));
			if (!Objects.equals(hypOutput, query.getOutput()))
				return query;
		}
		return null;
	}

	private <S> Word<I> randomAccessSequence(UniversalDeterministicAutomaton<S, I, ?, ?, ?> automaton,
			Collection<? extends I> inputs, S toState) {
		Queue<S> bfsQueue = new ArrayDeque<S>();
		MutableMapping<S, Word<I>> reach = automaton.createStaticStateMapping();
		S init = automaton.getInitialState();
		reach.put(init, Word.<I>epsilon());
		bfsQueue.add(init);

		S curr;

		while ((curr = bfsQueue.poll()) != null && curr != toState) {
			Word<I> as = reach.get(curr);
			List<I> shuffledInputs = new ArrayList<>(inputs);
			Collections.shuffle(shuffledInputs, rand);

			for (I in : inputs) {
				S succ = automaton.getSuccessor(curr, in);
				if (succ == null)
					continue;

				if (reach.get(succ) == null) {
					Word<I> succAs = as.append(in);
					reach.put(succ, succAs);
				}
				bfsQueue.add(succ);
			}
		}

		return reach.get(curr);
	}
}
