package com.floreysoft.jmte;

import java.util.ArrayList;
import java.util.List;


public class TokenStream {
	private final String sourceName;
	private final String input;
	private final List<StartEndPair> scan;
	private final Lexer lexer;
	private final String splitStart;
	private final String splitEnd;

	private transient List<Token> tokens = null;
	private transient int currentTokenIndex = -1;
	private transient Token currentToken = null;
	
	public TokenStream(String sourceName, String input,
			List<StartEndPair> scan, Lexer lexer, String splitStart,
			String splitEnd) {
		this.sourceName = sourceName;
		this.input = input;
		this.scan = scan;
		this.lexer = lexer;
		this.splitStart = splitStart;
		this.splitEnd = splitEnd;
	}

	private void fillTokens() {
		this.tokens = new ArrayList<Token>();
		final char[] inputChars = input.toCharArray();
		int offset = 0;
		int index = 0;
		for (StartEndPair startEndPair : scan) {
			int plainTextLengthBeforeNextToken = startEndPair.start
					- splitStart.length() - offset;
			if (plainTextLengthBeforeNextToken != 0) {
				AbstractToken token = new PlainTextToken(new String(inputChars,
						offset, plainTextLengthBeforeNextToken));
				token.setTokenIndex(index++);
				tokens.add(token);
			}
			offset = startEndPair.end + splitEnd.length();

			AbstractToken token = lexer.nextToken(sourceName, inputChars,
					startEndPair.start, startEndPair.end);
			token.setTokenIndex(index++);
			tokens.add(token);
		}

		// do not forget to add the final chunk of pure text (might be the
		// only
		// chunk indeed)
		int remainingChars = input.length() - offset;
		if (remainingChars != 0) {
			AbstractToken token = new PlainTextToken(new String(inputChars,
					offset, remainingChars));
			token.setTokenIndex(index++);
			tokens.add(token);
		}
	}

	private void initTokens() {
		if (this.tokens == null) {
			fillTokens();
			this.currentTokenIndex = 0;
		}
	}

	public Token nextToken() {
		initTokens();
		if (currentTokenIndex < tokens.size()) {
			currentToken = tokens.get(currentTokenIndex++);
		} else {
			currentToken = null;
		}
		return currentToken;
	}
	
	public void consume() {
		nextToken();
	}
	
	public Token currentToken() {
		return currentToken;
	}

	public void rewind(Token tokenToRewindTo) {
		initTokens();
		this.currentTokenIndex = tokenToRewindTo.getTokenIndex() + 1;
	}
	
	public List<Token> getAllTokens() {
		initTokens();
		return this.tokens;
	}

	
}
