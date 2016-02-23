package lex;

import errors.LexicalError;
import symbolTable.KeywordTable;
import symbolTable.SymbolTable;
import symbolTable.SymbolTableEntry;

import static lex.Token.*;
import static lex.CharStream.*;
import static lex.Token.OperatorType.*;
import static lex.TokenType.*;
//import static lex.TokenType.

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.BufferOverflowException;
import java.nio.CharBuffer;

/*
 * TODO: Assignment #1
 */
public class Tokenizer {
	private CharStream stream = null;
	private CharBuffer buffer;
	private Token lastToken = null;
	/**
	 * The KeywordTable is a SymbolTable that comes with all of the KeywordEntries
	 * already inserted.
	 */
	private KeywordTable keywordTable;
	private SymbolTable table;

	public Tokenizer(String filename) throws IOException, LexicalError {
		super();
		init(new CharStream(filename));
	}

	/**
	 * Used during testing to read files from the classpath.
	 */
	public Tokenizer(URL url) throws IOException, LexicalError {
		super();
		init(new CharStream(url));
	}

	public Tokenizer(File file) throws IOException, LexicalError {
		super();
		init(new CharStream(file));
	}

	protected void init(CharStream stream) {
		this.stream = stream;
		keywordTable = new KeywordTable();
		buffer = CharBuffer.allocate(MAX_IDENTIFIER_SIZE);
		// TODO more initialization will be needed...
	}

	public int getLineNumber() {
		return stream.lineNumber();
	}

	public Token getNextToken() throws LexicalError {
		Token token;
//		if (lastToken != null) {
//			lastToken.clear();
//		}
		buffer = CharBuffer.allocate(MAX_IDENTIFIER_SIZE);
		char c = stream.currentChar();
		if (c == CharStream.BLANK){
			c = stream.currentChar();
		}
		if (Character.isLetter(c))
			token = readIdentifier(c);
		else if (Character.isDigit(c))
			token = readNumber(c);
		else
			token = readSymbol(c);
		lastToken = token;
		return token;
	}

	private Token readIdentifier(char nextChar) throws LexicalError {
		Token result;
		while (Character.isDigit(nextChar) || Character.isLetter(nextChar)) {
			try { buffer.put(nextChar); }
			catch (BufferOverflowException e) {
				throw LexicalError.IdentifierTooLong(buffer.toString());
			}
			nextChar = stream.currentChar();
		}

		if (!(nextChar == CharStream.BLANK)) {
			stream.pushBack(nextChar);
		}
		result = new Token();
		char[] dst = new char[buffer.position() + 1];
		buffer.rewind();
		buffer.get(dst);

		String value = new String(dst).toString().trim();
		SymbolTableEntry lookup = keywordTable.lookup(value.toUpperCase());
		if (lookup != null) {
			result.setValue(value.toUpperCase());
			handleTextOps(result);
			if(result.getType() == null) {
				result.setType(lookup.getType());
			}
		}
		else {
			result.setType(IDENTIFIER);
			result.setValue(value);
		}
		return result;

	}
	private void handleTextOps(Token token) {
		if (token.getValue() != null) {
			switch (token.getValue()) {
				case "OR":
					token.setOpType(OR);
					token.setType(ADDOP);
					break;
				case "DIV":
					token.setOpType(INTEGERDIVIDE);
					token.setType(MULOP);
					break;
				case "MOD":
					token.setOpType(MOD);
					token.setType(MULOP);
					break;
				case "AND":
					token.setOpType(AND);
					token.setType(MULOP);
					break;
				default:
					break;
			}
		}
	}

	private Token readNumber(char nextChar) throws LexicalError {
		Token result;
		char peek;
		boolean realFlag = false;
		//read as many digits as possible
		while (Character.isDigit(nextChar)) {
			try { buffer.put(nextChar); }
			catch (BufferOverflowException e) {
//				throw LexicalError.IdentifierTooLong(buffer.toString());
			}
			nextChar = stream.currentChar();
		}
		//if next char after digits is a .
		if (nextChar == '.') {
			peek = stream.currentChar();
			//check for more digits afterwards if so then it's a real
			if (Character.isDigit(peek)) {
				realFlag = true;
				//read all the digits
				try {
					buffer.put(nextChar);
					buffer.put(peek);
					nextChar = stream.currentChar();
					while (Character.isDigit(nextChar)) {
						buffer.put(nextChar);
						nextChar = stream.currentChar();
					}
				}
				catch (BufferOverflowException e) {
//					throw LexicalError.IdentifierTooLong(buffer.toString());
				}
				//if we find an e try and form an exponent
				if (nextChar == 'e') {
					peek = stream.currentChar();
					if (Character.isDigit(peek) || peek == '-' || peek == '+') {
						try {
							buffer.put(nextChar);
							buffer.put(peek);
							nextChar = stream.currentChar();
							while (Character.isDigit(nextChar)) {
								buffer.put(nextChar);
								nextChar = stream.currentChar();
							}
						}
						catch (BufferOverflowException e) {
//							throw LexicalError.IdentifierTooLong(buffer.toString());
						}

						if (!(nextChar == CharStream.BLANK)) {
							stream.pushBack(nextChar);
						}
					}
					else {
						stream.pushBack(peek);
						stream.pushBack(nextChar);
					}
					//TODO: else throw a malformed constant error
				}
				else {
					if (!(nextChar == CharStream.BLANK)) {
						stream.pushBack(nextChar);
					}
				}
			}
			//it's the double dot
			else if (peek == '.') {
				stream.pushBack(peek);
				stream.pushBack(nextChar);
			}
			else {
				//this should error number or dot is required after decimal point
				stream.pushBack(peek);
				stream.pushBack(nextChar);
				//TODO: throw malformed constant error
			}
		}
		else if (nextChar == 'e') {
			peek = stream.currentChar();

			if (Character.isDigit(peek) || peek == '-' || peek == '+') {
				realFlag = true;
				try {
					buffer.put(nextChar);
					buffer.put(peek);
					nextChar = stream.currentChar();
					while (Character.isDigit(nextChar)) {
						buffer.put(nextChar);
						nextChar = stream.currentChar();
					}
				}
				catch (BufferOverflowException e) {
//							throw LexicalError.IdentifierTooLong(buffer.toString());
				}

				if (!(nextChar == CharStream.BLANK)) {
					stream.pushBack(nextChar);
				}
			}
			else {
				stream.pushBack(peek);
				stream.pushBack(nextChar);
			}
			//TODO: else throw a malformed constant error
		}
		else {

			if (!(nextChar == CharStream.BLANK)) {
				stream.pushBack(nextChar);
			}
		}


		result = new Token();
		if (realFlag) {
			result.setType(REALCONSTANT);
		}
		else {
			result.setType(INTCONSTANT);
		}

		result.setValue(buffer.rewind().toString());
		return result;
	}

	private Token readSymbol(char nextChar) throws LexicalError {
		Token result = new Token();
		char peek;
//		if(nextChar == BLANK) {
//			return readSymbol(stream.currentChar());
//		}
		switch (nextChar) {
			case ',':
				result.setType(COMMA);
				break;
			case  ';':
				result.setType(SEMICOLON);
				break;
			case  ':':
				peek = stream.currentChar();
				if (peek == '=') {
					result.setType(ASSIGNOP);
				}
				else {
					result.setType(COLON);
					stream.pushBack(peek);
				}
				break;
			case  ')':
				result.setType(RIGHTPAREN);
				break;
			case  '(':
				result.setType(LEFTPAREN);
				break;
			case  '[':
				result.setType(LEFTBRACKET);
				break;
			case  ']':
				result.setType(RIGHTBRACKET);
				break;
			case  '-':
				result.setOpType(SUBTRACT);
				handlePlusMinus(result);
				break;
			case  '+':
				result.setOpType(ADD);
				handlePlusMinus(result);
				break;
			case  '.':
				peek = stream.currentChar();
				if (peek == '.') {
					result.setType(DOUBLEDOT);
				}
				else {
					stream.pushBack(peek);
					result.setType(ENDMARKER);
				}
				break;
			case  '=':
				result.setOpType(EQUAL);
				result.setType(RELOP);
				break;
			case  '<':
				peek = stream.currentChar();
				if (peek == '=') {
					result.setOpType(LESSTHANOREQUAL);
					result.setType(RELOP);
				}
				else if (peek == '>') {
					result.setOpType(NOTEQUAL);
					result.setType(RELOP);
				}
				else {
					stream.pushBack(peek);
					result.setOpType(LESSTHAN);
					result.setType(RELOP);
				}
				break;
			case  '>':
				peek = stream.currentChar();
				if (peek == '=') {
					result.setOpType(GREATERTHANOREQUAL);
					result.setType(RELOP);
				}
				else {
					stream.pushBack(peek);
					result.setOpType(GREATERTHAN);
					result.setType(RELOP);
				}
				break;
			case  '*':
				result.setOpType(MULTIPLY);
				result.setType(MULOP);
				break;
			case '/':
				result.setOpType(DIVIDE);
				result.setType(MULOP);
				break;
			case  EOF:
				result.setType(ENDOFFILE);
				break;
		}
		return result;
	}


	//returns true if handled otherwise
	private void handlePlusMinus(Token token) {
		if (lastToken != null) {
			switch (lastToken.getType()) {
				case RIGHTPAREN:
				case RIGHTBRACKET:
				case IDENTIFIER:
				case INTCONSTANT:
				case REALCONSTANT:
					token.setType(ADDOP);
					break;
				default:
					if (token.getOpType() == ADD) {
						token.setOpType(null);
						token.setType(UNARYPLUS);
					}
					else {
						token.setOpType(null);
						token.setType(UNARYMINUS);
					}
					break;

			}
		}
		else {
			if (token.getOpType() == ADD) {
				token.setOpType(null);
				token.setType(UNARYPLUS);
			}
			else {
				token.setOpType(null);
				token.setType(UNARYMINUS);
			}		}
	}

	// TODO Much (much) more code goes here...
}
