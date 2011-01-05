package com.floreysoft.jmte;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Iterator;
import java.util.List;

import com.floreysoft.jmte.EngineTest.MyBean;
import com.google.caliper.SimpleBenchmark;

public class EngineCaliperTest {

	/**
	 * Tests a selection of scripts supposed to be the most frequently used
	 * 
	 * @author olli
	 * 
	 */
	public static class PortfolioBenchmark extends SimpleBenchmark {
		EngineTest engineTest = new EngineTest();

		public void timeStringBuilderReference(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				StringBuilder output = new StringBuilder();
				output.append("PREFIX").append(
						EngineTest.DEFAULT_MODEL.get("address").toString())
						.append("SUFFIX");
				assertEquals("PREFIX" + EngineTest.DEFAULT_MODEL.get("address")
						+ "SUFFIX", output.toString());
			}
		}

		public void timeStringConcatReference(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				String output = "PREFIX"
						+ EngineTest.DEFAULT_MODEL.get("address").toString()
						+ "SUFFIX";
				assertEquals("PREFIX" + EngineTest.DEFAULT_MODEL.get("address")
						+ "SUFFIX", output);
			}
		}

		public void timePrintfReference(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				String output = String.format("PREFIX%sSUFFIX",
						EngineTest.DEFAULT_MODEL.get("address").toString());
				assertEquals("PREFIX" + EngineTest.DEFAULT_MODEL.get("address")
						+ "SUFFIX", output);
			}
		}

		public void timeExpression(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				engineTest.suffixPrefix();
			}
		}

		public void timeNativeIfReference(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				final Object empty = EngineTest.DEFAULT_MODEL.get("empty");
				final String output;
				if ((empty instanceof Boolean && (Boolean) empty == true)
						|| (empty != null && !empty.equals(""))) {
					output = EngineTest.DEFAULT_MODEL.get("address").toString();
				} else {
					output = "NIX";
				}
				assertEquals("NIX", output);
			}
		}

		public void timeIf(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				engineTest.ifEmptyFalseExpression();
			}
		}

		@SuppressWarnings("unchecked")
		public void timeNativeForeachReference(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				StringBuilder output = new StringBuilder();
				List<MyBean> list = (List<MyBean>) EngineTest.DEFAULT_MODEL
						.get("list");
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					MyBean myBean = (MyBean) iterator.next();
					output.append(myBean.getProperty1().toString());
					if (iterator.hasNext()) {
						output.append("\n");
					}
				}
				assertEquals("1.1\n2.1", output.toString());
				assertNull(EngineTest.DEFAULT_MODEL.get("item"));
			}
		}

		@SuppressWarnings("unchecked")
		public void timeNativeForeachNoStringBuilderReference(int reps)
				throws Exception {
			for (int i = 0; i < reps; i++) {
				String output = "";
				List<MyBean> list = (List<MyBean>) EngineTest.DEFAULT_MODEL
						.get("list");
				for (Iterator iterator = list.iterator(); iterator.hasNext();) {
					MyBean myBean = (MyBean) iterator.next();
					output += myBean.getProperty1().toString();
					if (iterator.hasNext()) {
						output += "\n";
					}
				}
				assertEquals("1.1\n2.1", output);
				assertNull(EngineTest.DEFAULT_MODEL.get("item"));
			}
		}

		public void timeForeach(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				engineTest.newlineForeachSeparator();
			}
		}

		public void timeRenderer(int reps) throws Exception {
			for (int i = 0; i < reps; i++) {
				engineTest.renderer();
			}
		}
	}
}
