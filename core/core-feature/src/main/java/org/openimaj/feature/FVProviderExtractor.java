package org.openimaj.feature;

/**
 * An adaptor for {@link FeatureExtractor}s that return objects that are
 * {@link FeatureVectorProvider}s that is a {@link FeatureExtractor} that
 * returns a {@link FeatureVector}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <FV>
 *            Type of {@link FeatureVector}
 * @param <OBJECT>
 *            Type of object the extractor works on.
 */
public class FVProviderExtractor<FV extends FeatureVector, OBJECT>
		implements FeatureExtractor<FV, OBJECT>
{
	/**
	 * The internal extractor
	 */
	public FeatureExtractor<? extends FeatureVectorProvider<FV>, OBJECT> extractor;

	/**
	 * Construct with the given extractor.
	 * 
	 * @param extractor
	 *            the extractor
	 */
	public FVProviderExtractor(FeatureExtractor<? extends FeatureVectorProvider<FV>, OBJECT> extractor) {
		this.extractor = extractor;
	}

	@Override
	public FV extractFeature(OBJECT object) {
		return extractor.extractFeature(object).getFeatureVector();
	}

	/**
	 * Create a new {@link FVProviderExtractor} with the given extractor.
	 * 
	 * @param extractor
	 *            the extractor
	 * 
	 * @param <FV>
	 *            Type of {@link FeatureVector}
	 * @param <OBJECT>
	 *            Type of object the extractor works on.
	 * @param <EXTRACTOR>
	 *            Type of extractor.
	 * 
	 * @return the new {@link FVProviderExtractor}
	 */
	public static <FV extends FeatureVector, OBJECT, EXTRACTOR extends FeatureExtractor<? extends FeatureVectorProvider<FV>, OBJECT>>
			FVProviderExtractor<FV, OBJECT>
			create(EXTRACTOR extractor)
	{
		return new FVProviderExtractor<FV, OBJECT>(extractor);
	}
}
