package com.floreysoft.jmte;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NestedParser {
	MiniParser miniParser = MiniParser.rawOutputInstance();
	MiniParser innerMiniParser = MiniParser.trimmedInstance();

	// TODO create a version that allows for multi character operators
	public List<Object> parse(final String input, final List<String> operators) {
		final List<Object> result = new ArrayList<Object>();
		if (operators.size() != 0) {
			final boolean innerLoop = operators.size() == 1;
			final MiniParser currentParser = innerLoop ? innerMiniParser
					: miniParser;
			final String operator = operators.get(0);
			final List<String> segments;
			if (operator.length() == 1) {
				segments = currentParser.split(input, operator.charAt(0));
			} else if (operator.length() == 2) {
				
				List<String> allSegments = currentParser.scan(input, String.valueOf(operator
						.charAt(0)), String.valueOf(operator.charAt(1)), true);
				if (allSegments.size() >  0) {
					// the first part is not processed any further
					result.add(allSegments.get(0));
				}
				if (allSegments.size() >  1) {
					segments = allSegments.subList(1, allSegments.size());
				} else {
					segments = Collections.emptyList();
				}
				
			} else {
				throw new IllegalArgumentException(
						"Operators must either be start/end pairs or single characters");

			}
			if (innerLoop) {
				result.addAll(segments);
			} else {
				for (String segment : segments) {
					List<Object> parse = parse(segment, operators.subList(1,
							operators.size()));
					if (parse.size() == 1 && parse.get(0) instanceof String) {
						result.add(parse.get(0));
					} else {
						result.add(parse);
					}
				}
			}
		}
		return result;
	}

	public List<Object> parse(final String input, final String[] strings) {
		return parse(input, Arrays.asList(strings));
	}

}
