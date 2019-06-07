package se.uu.it.modeltester.mutate.fragment;

import java.util.List;

import de.rub.nds.tlsattacker.core.protocol.message.DtlsHandshakeMessageFragment;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import se.uu.it.modeltester.execute.ExecutionContext;
import se.uu.it.modeltester.execute.FragmentationResult;
import se.uu.it.modeltester.mutate.Mutation;
import se.uu.it.modeltester.mutate.MutationType;

/**
 * Mutates the fragmentation result by splitting the original message according
 * to a given fragmentation. Ignores the current fragment configuration.
 */
public class SplittingMutation implements Mutation<FragmentationResult> {

	private Fragmentation fragmentation;
	private transient HandshakeMessageFragmenter dtlsMessageFragmenter;

	private SplittingMutation() {
		this.dtlsMessageFragmenter = new HandshakeMessageFragmenter();
	}

	public SplittingMutation(Fragmentation fragmentation) {
		this();
		this.fragmentation = fragmentation;
	}

	@Override
	public FragmentationResult mutate(FragmentationResult result,
			TlsContext context, ExecutionContext exContext) {
		List<DtlsHandshakeMessageFragment> newDtlsFragments = dtlsMessageFragmenter
				.generateDtlsFragments(fragmentation, result.getMessage(),
						context);
		return new FragmentationResult(result.getMessage(), newDtlsFragments);
	}

	@Override
	public MutationType getType() {
		return MutationType.MESSAGE_SPLITTING;
	}

	public String toString() {
		return "SplittingMutation(" + fragmentation.toString() + ")";
	}

}
