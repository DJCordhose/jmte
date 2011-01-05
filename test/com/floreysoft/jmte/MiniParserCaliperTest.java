package com.floreysoft.jmte;

import com.google.caliper.SimpleBenchmark;

public class MiniParserCaliperTest {

	/**
	 * Tests a selection of use cases supposed to be the most frequently used
	 * 
	 * @author olli
	 * 
	 */
	public static class PortfolioBenchmark extends SimpleBenchmark {
		MiniParser miniParser = new MiniParser();
		String input = "Realisticly long string";
		String oldString = "long";
		String newString ="short";
		
		public void timeRegexpReplaceReference(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				input.replace(oldString, newString);
			}
		}
		public void timeCharReplaceReference(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				input.replace('l', 's');
			}
		}
		public void timeReplace(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				miniParser.replace(input, oldString, newString);
			}
		}
	}
}
