package com.floreysoft.jmte;

import com.floreysoft.jmte.util.MiniParser;
import com.google.caliper.SimpleBenchmark;

public class MiniParserCaliperTest {

	/**
	 * Tests a selection of use cases supposed to be the most frequently used
	 * 
	 * @author olli
	 * 
	 */
	public static class PortfolioBenchmark extends SimpleBenchmark {
		MiniParser miniParser = MiniParser.defaultInstance();
		String input = "Realisticly long string";
		String oldString = "long";
		String newString = "short";
		String toSplit = "1,2,3,4,5,6,7,8";

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

		public void timeRegexpSplitReference(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				MiniParserTest.SPLIT_STRING.split(",");
			}
		}

		public void timeSplit(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				miniParser.split(MiniParserTest.SPLIT_STRING, ',');
			}
		}

		public void timeSplitSet(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				miniParser.split(MiniParserTest.SPLIT_STRING, ",");
			}
		}

		public void timeRegexpWSSplitReference(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				MiniParserTest.WS_SPLIT_STRING.split("( |\t|\r|\n)+");
			}
		}

		public void timeWSSplit(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				miniParser.splitOnWhitespace(MiniParserTest.WS_SPLIT_STRING);
			}
		}
	}
}
