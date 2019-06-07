package se.uu.it.modeltester;

import net.automatalib.automata.transout.impl.FastMealy;
import net.automatalib.words.Alphabet;
import se.uu.it.modeltester.sut.io.TlsInput;
import se.uu.it.modeltester.sut.io.TlsOutput;

// the only reason we are using FastMealy is to simplify the generics, any Mealy machine implementation
// would do just fine
public class ConformanceTestingTask {
	private FastMealy<TlsInput, TlsOutput> specification;
	private Alphabet<TlsInput> alphabet;
	public ConformanceTestingTask(FastMealy<TlsInput, TlsOutput> specification,
			Alphabet<TlsInput> alphabet) {
		super();
		this.specification = specification;
		this.alphabet = alphabet;
	}

	public FastMealy<TlsInput, TlsOutput> getSpecification() {
		return specification;
	}
	public Alphabet<TlsInput> getAlphabet() {
		return alphabet;
	}

}
