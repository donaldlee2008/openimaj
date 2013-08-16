package org.openimaj.ml.clustering.spectral;

import org.openimaj.data.DataSource;
import org.openimaj.knn.DoubleNearestNeighbours;
import org.openimaj.knn.DoubleNearestNeighboursExact;
import org.openimaj.ml.clustering.SpatialClusterer;
import org.openimaj.ml.clustering.SpatialClusters;
import org.openimaj.ml.clustering.dbscan.DoubleNNDBSCAN;
import org.openimaj.util.function.Function;
import org.openimaj.util.pair.IndependentPair;

import ch.akuhn.matrix.eigenvalues.Eigenvalues;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <DATATYPE>
 *
 */
public class SpectralClusteringConf<DATATYPE>{
	
	protected static class DefaultClustererFunction<DATATYPE> implements Function<IndependentPair<double[], double[][]>, SpatialClusterer<? extends SpatialClusters<DATATYPE>,DATATYPE>>{


		private SpatialClusterer<? extends SpatialClusters<DATATYPE>, DATATYPE> internal;

		public DefaultClustererFunction( SpatialClusterer<? extends SpatialClusters<DATATYPE>,DATATYPE> internal) {
			this.internal = internal;
		}

		@Override
		public SpatialClusterer<? extends SpatialClusters<DATATYPE>, DATATYPE> apply(IndependentPair<double[], double[][]> in) {
			return internal;
		}
		
	}
	
	/**
	 * The internal clusterer
	 */
	Function<IndependentPair<double[], double[][]>, SpatialClusterer<? extends SpatialClusters<DATATYPE>,DATATYPE>> internal;

	/**
	 * The graph laplacian creator
	 */
	public GraphLaplacian laplacian;

	/**
	 * The method used to select the number of eigen vectors from the lower valued eigenvalues
	 */
	public EigenChooser eigenChooser;

	/**
	 * @param internal the internal clusterer
	 * @param eigK the value handed to {@link HardCodedEigenChooser}
	 *
	 */
	public SpectralClusteringConf(SpatialClusterer<? extends SpatialClusters<DATATYPE>,DATATYPE> internal, int eigK) {
		this.internal = new DefaultClustererFunction<DATATYPE>(internal);
		this.laplacian = new GraphLaplacian.Normalised();
		this.eigenChooser = new HardCodedEigenChooser(eigK);

	}

	/**
	 * The underlying {@link EigenChooser} is set to an {@link ChangeDetectingEigenChooser} which
	 * looks for a 100x gap between eigen vectors to select number of clusters. It also insists upon
	 * a maximum of 0.1 * number of data items (so 10 items per cluster)
	 *
	 * @param internal the internal clusterer
	 *
	 */
	public SpectralClusteringConf(SpatialClusterer<? extends SpatialClusters<DATATYPE>,DATATYPE> internal) {
		this.internal = new DefaultClustererFunction<DATATYPE>(internal);
		this.laplacian = new GraphLaplacian.Normalised();
		this.eigenChooser = new ChangeDetectingEigenChooser(100,0.1);

	}
	
	/**
	 * @param internal an internal clusterer 
	 * @param lap the laplacian
	 * @param top the top eigen vectors
	 */
	public SpectralClusteringConf(SpatialClusterer<? extends SpatialClusters<DATATYPE>, DATATYPE> internal, GraphLaplacian lap, int top) {
		this.internal = new DefaultClustererFunction<DATATYPE>(internal);
		this.laplacian = lap;
		this.eigenChooser = new HardCodedEigenChooser(top);
	}

	/**
	 * The underlying {@link EigenChooser} is set to an {@link ChangeDetectingEigenChooser} which
	 * looks for a 100x gap between eigen vectors to select number of clusters. It also insists upon
	 * a maximum of 0.1 * number of data items (so 10 items per cluster)
	 *
	 * @param internal the internal clusterer
	 * @param laplacian the graph laplacian
	 *
	 */
	public SpectralClusteringConf(SpatialClusterer<? extends SpatialClusters<DATATYPE>,DATATYPE> internal, GraphLaplacian laplacian) {
		this.internal = new DefaultClustererFunction<DATATYPE>(internal);
		this.laplacian = laplacian;
		this.eigenChooser = new ChangeDetectingEigenChooser(100,0.1);

	}
	
	/**
	 * The underlying {@link EigenChooser} is set to an {@link ChangeDetectingEigenChooser} which
	 * looks for a 100x gap between eigen vectors to select number of clusters. It also insists upon
	 * a maximum of 0.1 * number of data items (so 10 items per cluster)
	 * @param internal the internal clusterer
	 *
	 */
	public SpectralClusteringConf(Function<IndependentPair<double[], double[][]>, SpatialClusterer<? extends SpatialClusters<DATATYPE>, DATATYPE>> internal) {
		this.internal = internal;
		this.laplacian = new GraphLaplacian.Normalised();
		this.eigenChooser = new ChangeDetectingEigenChooser(100,0.1);

	}

	
}