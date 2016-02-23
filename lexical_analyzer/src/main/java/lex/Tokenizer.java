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
import java.nio.BufferUnderflowException;
import java.nio.CharBuffer;

/*
 * TODO: Assignment #1
 */
public class Tokenizer {
	private CharStream stream = null; //stream characters are read from
	private CharBuffer buffer;//buffer to hold read characters
	private Token lastToken = null; //keep track of the last token returned
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
	}

	//flushes buffer and resets
	private void flushBuffer() {
		for(int i = 0; i < buffer.position(); i++) {
			buffer.put(i, ' ');
		}
		buffer.clear();
	}

	public int getLineNumber() {
		return stream.lineNumber();
	}

	public Token getNextToken() throws LexicalError {
		Token token;
		flushBuffer();//clear the buffer for the new token
		char c = stream.currentChar();//read the first char
		if (c == CharStream.BLANK){// if the char is BLANK just discard and try again
			c = stream.currentChar();//all consecutive white space is skipped so we no this isn't BLANK
		}
		//Call the correct stream tokenizer method
		if (Character.isLetter(c))
			token = readIdentifier(c);
		else if (Character.isDigit(c))
			token = readNumber(c);
		else
			token = readSymbol(c);
		lastToken = token;//keep track of the last token
		return token;
	}
	//returns a properly formatted string from the buffer contents
	private String getBufferValue() {
		char[] dst = new char[buffer.position() + 1];
		buffer.rewind();
		buffer.get(dst);
		return new String(dst).toString().trim();
	}
	//reads letter tokens ie identifiers, keywords, and text ops (AND etc)
	private Token readIdentifier(char nextChar) throws LexicalError {
		Token result;
		while (Character.isDigit(nextChar) || Character.isLetter(nextChar)) {//read all the digits or letters we can
			try { buffer.put(nextChar); }//put the char in the buffer
			catch (BufferOverflowException|BufferUnderflowException e) {
				throw LexicalError.IdentifierTooLong(buffer.toString());//don't allow overflow
			}
			nextChar = stream.currentChar();//get the next char
		}

		if (!(nextChar == CharStream.BLANK)) {//if we overread something that wasn't a blank push it back
			stream.pushBack(nextChar);
		}
		//construct return token
		result = new Token();
		String value = getBufferValue();
		SymbolTableEntry lookup = keywordTable.lookup(value.toUpperCase());//Check for reserved keyword
		if (lookup != null) {
			result.setValue(value.toUpperCase());
			handleTextOps(result);//helper to handle operations with text identifiers
			if(result.getType() == null) {
				result.setType(lookup.getType());
			}
		}
		else {//otherwise it's an identifer
			result.setType(IDENTIFIER);
			result.setValue(value);
		}
		return result;

	}
	//assigns types and optypes to text addops and mulops **VALUE MUST BE SET TO THE LEXEME
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
	private char readDigits() throws LexicalError {

		char nextChar = stream.currentChar();
		try {
			while (Character.isDigit(nextChar)) {
				buffer.put(nextChar);
				nextChar = stream.currentChar();
			}
		}
		catch (BufferOverflowException|BufferUnderflowException e) {
			throw LexicalError.IdentifierTooLong(buffer.toString());//don't allow overflow
		}
		return nextChar;
	}

//	private boolean checkPeak

	//creates numerical tokens
	private Token readNumber(char nextChar) throws LexicalError {
		Token result;
		char peek;
		boolean realFlag = false;//keeps track of whether or not we have a real number
		//read as many digits as possible
		try {
			buffer.put(nextChar);
		} catch (BufferOverflowException|BufferUnderflowException e) {
			throw LexicalError.IdentifierTooLong(buffer.toString());//don't allow overflow
		}
		nextChar = readDigits();
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
				} catch (BufferOverflowException|BufferUnderflowException e) {
					throw LexicalError.IdentifierTooLong(buffer.toString());//don't allow overflow
				}
				nextChar = readDigits();
				//if we find an e try and form an exponent
				if (nextChar == 'e') {
					peek = stream.currentChar();
					if (Character.isDigit(peek) || peek == '-' || peek == '+') {//check if valid format for exponent
						if (peek == '-' || peek == '+') { //TODO: this ugly fix it
							char peek2 = stream.currentChar();
							if (Character.isDigit(peek2)) { //verifies valid format 3e-7
								try {
									buffer.put(nextChar);
									buffer.put(peek);
									buffer.put(peek2);
								} catch (BufferOverflowException|BufferUnderflowException e) {
									throw LexicalError.IdentifierTooLong(buffer.toString());//don't allow overflow
								}
								nextChar = readDigits();
								if (!(nextChar == CharStream.BLANK)) {
									stream.pushBack(nextChar);
								}
							}
							else {
								//TODO: throw malformed constant error
							}
						}
						else if(Character.isDigit(peek)) {
							try {
								buffer.put(nextChar);
								buffer.put(peek);
							} catch (BufferOverflowException|BufferUnderflowException e) {
								throw LexicalError.IdentifierTooLong(buffer.toString());//don't allow overflow
							}

							nextChar = readDigits();

							if (!(nextChar == CharStream.BLANK)) {
								stream.pushBack(nextChar);
							}
						}

					}
					//TODO: else throw a malformed constant error
					else {
						stream.pushBack(peek);
						stream.pushBack(nextChar);
					}
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
		else if (nextChar == 'e') { // handle scientific notation
			peek = stream.currentChar();

			if (Character.isDigit(peek) || peek == '-' || peek == '+') {
				realFlag = true;
				try {
					buffer.put(nextChar);
					buffer.put(peek);
				} catch (BufferOverflowException|BufferUnderflowException e) {
					throw LexicalError.IdentifierTooLong(buffer.toString());//don't allow overflow
				}
				nextChar = readDigits();
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

		result.setValue(getBufferValue());
		return result;
	}

	//handle everything that is isn't identifiers or numbers
	private Token readSymbol(char nextChar) throws LexicalError {
		Token result = new Token();
		char peek;
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

	//specific check for how + and - should be handled
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
