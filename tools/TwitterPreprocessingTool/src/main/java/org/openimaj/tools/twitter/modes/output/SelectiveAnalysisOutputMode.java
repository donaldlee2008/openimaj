/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.tools.twitter.modes.output;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.tools.twitter.options.AbstractTwitterPreprocessingToolOptions;
import org.openimaj.twitter.GeneralJSON;

/**
 * An ouput mode which alters the tweets being outputted
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SelectiveAnalysisOutputMode extends AnalysisOutputMode{
	List<String> selectiveAnalysis;

	/**
	 * Non selective, output everything 
	 */
	public SelectiveAnalysisOutputMode() {
		this.selectiveAnalysis = new ArrayList<String>();
	}

	@Override
	public void output(GeneralJSON twitterStatus, PrintWriter outputWriter) throws IOException {
		twitterStatus.writeASCIIAnalysis(outputWriter,this.selectiveAnalysis);
		if(delim != null){
			outputWriter.print(this.delim);
		}
	}
	
	@Override
	public void validate(AbstractTwitterPreprocessingToolOptions twitterPreprocessingModes){
		this.selectiveAnalysis = new ArrayList<String>();
		for (TwitterPreprocessingMode<?> mode : twitterPreprocessingModes.modeOptionsOp) {
			this.selectiveAnalysis.add(mode.getAnalysisKey());
		}
	}
}

