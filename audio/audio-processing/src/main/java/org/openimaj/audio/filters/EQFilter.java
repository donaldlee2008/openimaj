/**
 * 
 */
package org.openimaj.audio.filters;

import org.openimaj.audio.AudioStream;
import org.openimaj.audio.SampleChunk;
import org.openimaj.audio.processor.AudioProcessor;
import org.openimaj.audio.samples.SampleBuffer;

/**
 *	A class that encapsulates a bunch of different EQ-based filter algorithms
 *	that use a standard bi-quad filter (4th order Linkwitz-Riley filter).
 *
 *	@see "http://musicdsp.org/showArchiveComment.php?ArchiveID=266"
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 19 Jul 2012
 *	@version $Author$, $Revision$, $Date$
 */
public class EQFilter extends AudioProcessor
{
	/**
	 * 	A class that represents the bi-quad coefficients for a filter.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 20 Jul 2012
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static class EQVars
	{
		double a0, a1, a2, a3, a4;
		double b0, b1, b2, b3, b4;
	}
	
	/**
	 *	An enumerator for various audio filters.
	 *
	 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
	 *  @created 19 Jul 2012
	 *	@version $Author$, $Revision$, $Date$
	 */
	public static enum EQType
	{
		/**
		 *	Low pass filter 
		 */
		LPF
		{
			@Override
			public EQVars getCoefficients( double frequency, double sampleRate )
			{
				double wc  = 2 * Math.PI * frequency;
				double wc2 = wc * wc;
				double wc3 = wc2 * wc;
				double wc4 = wc2 * wc2;
				double k = wc/Math.tan(Math.PI*frequency/sampleRate);
				double k2 = k * k;
				double k3 = k2 * k;
				double k4 = k2 * k2;
				double sqrt2 = Math.sqrt(2);
				double sq_tmp1 = sqrt2 * wc3 * k;
				double sq_tmp2 = sqrt2 * wc * k3;
				double a_tmp = 4 * wc2 * k2 + 2*sq_tmp1 + k4 + 2*sq_tmp2 + wc4;

				EQVars v = new EQVars();
				
				v.b1 = (4*(wc4+sq_tmp1-k4-sq_tmp2)) / a_tmp;
				v.b2 = (6*wc4-8*wc2*k2+6*k4)/a_tmp;
				v.b3 = (4*(wc4-sq_tmp1+sq_tmp2-k4))/a_tmp;
				v.b4 = (k4-2*sq_tmp1+wc4-2*sq_tmp2+4*wc2*k2)/a_tmp;

				//================================================
				// low-pass
				//================================================
				v.a0 = wc4/a_tmp;
				v.a1 = 4*wc4/a_tmp;
				v.a2 = 6*wc4/a_tmp;
				v.a3 = v.a1;
				v.a4 = v.a0;
				
				return v;
			}
		},
		
		/**
		 *	High pass filter 
		 */
		HPF
		{
			@Override
			public EQVars getCoefficients( double frequency, double sampleRate )
			{
				double wc  = 2 * Math.PI * frequency;
				double wc2 = wc * wc;
				double wc3 = wc2 * wc;
				double wc4 = wc2 * wc2;
				double k = wc/Math.tan(Math.PI*frequency/sampleRate);
				double k2 = k * k;
				double k3 = k2 * k;
				double k4 = k2 * k2;
				double sqrt2 = Math.sqrt(2);
				double sq_tmp1 = sqrt2 * wc3 * k;
				double sq_tmp2 = sqrt2 * wc * k3;
				double a_tmp = 4 * wc2 * k2 + 2*sq_tmp1 + k4 + 2*sq_tmp2 + wc4;

				EQVars v = new EQVars();
				
				v.b1 = (4*(wc4+sq_tmp1-k4-sq_tmp2)) / a_tmp;
				v.b2 = (6*wc4-8*wc2*k2+6*k4)/a_tmp;
				v.b3 = (4*(wc4-sq_tmp1+sq_tmp2-k4))/a_tmp;
				v.b4 = (k4-2*sq_tmp1+wc4-2*sq_tmp2+4*wc2*k2)/a_tmp;

				//================================================
				// high-pass
				//================================================
				v.a0 = k4/a_tmp;
				v.a1 = -4*k4/a_tmp;
				v.a2 = 6*k4/a_tmp;
				v.a3 = v.a1;
				v.a4 = v.a0;
				
				return v;
			}
		};
		
		/**
		 * 	Initialise the filter
		 *	@param frequency The frequency of the filter
		 *	@param sampleRate The sample rate of the samples
		 * 	@return The biquad coefficients for this filter 
		 */
		public abstract EQVars getCoefficients( double frequency, double sampleRate );
	}
	
	/** The type of EQ process */
	private EQType type = null;
	
	/** The cached biquad coefficients for this filter */
	private EQVars vars = null;
	
	/** The frequency of the filter */
	private double frequency = 0;

	// These variables are used during the loop
	// and are stored between loops to avoid clicking
	private double xm1 = 0, xm2 = 0, xm3 = 0, xm4 = 0, 
				   ym1 = 0, ym2 = 0, ym3 = 0, ym4 = 0;
	
	/**
	 * 	Default constructor for ad-hoc processing.
	 *	@param type The type of EQ
	 * 	@param f The frequency of the filter 
	 */
	public EQFilter( EQType type, double f )
	{
		this.type = type;
		this.frequency = f;
	}
	
	/**
	 * 	Chainable constructor for stream processing.
	 *	@param as The audio stream to process
	 *	@param type The type of EQ
	 * 	@param f The frequency of the filter 
	 */
	public EQFilter( AudioStream as, EQType type, double f )
	{
		super( as );
		this.type = type;
		this.frequency = f;
	}
	
	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.processor.AudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process( SampleChunk sample ) throws Exception
	{
		if( vars == null )
			vars = this.type.getCoefficients( frequency, 
					sample.getFormat().getSampleRateKHz() *1000d );

		// Standard bi-quad processing of the sample chunk
		// We have to process each channel independently because the function
		// is recursive.
		SampleBuffer sb = sample.getSampleBuffer();
		int nChans = sample.getFormat().getNumChannels();
		for( int c = 0; c < nChans; c++ )
		{
			for( int n = c; n < sb.size(); n += nChans )
			{
				double tempx = sb.get(n);
				double tempy = vars.a0*tempx + vars.a1*xm1 + vars.a2*xm2 + vars.a3*xm3 + 
						vars.a4*xm4 - vars.b1*ym1 - vars.b2*ym2 - vars.b3*ym3 - vars.b4*ym4;
				
				xm4 = xm3; xm3 = xm2; xm2 = xm1;
				xm1 = tempx;
				ym4 = ym3; ym3 = ym2; ym2 = ym1;
				ym1 = tempy;
		
				sb.set( n, (float)tempy );
			}
		}
			
		return sample;
	}
}
