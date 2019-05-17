package se.uu.it.modeltester.mutate;

import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.modeltester.execute.ExecutionContext;

public interface Mutator<R> {
//	public R mutate(R result, TlsContext context);
	public Mutation<R> generateMutation(R result, TlsContext context, ExecutionContext exContext);
	public MutatorType getType();
	/**
	 * Provides a compact and readable representation of the mutator.
	 */
	public String toString();
}
