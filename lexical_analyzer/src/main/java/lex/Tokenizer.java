package lex;

import errors.LexicalError;
import symbolTable.KeywordTable;
import symbolTable.SymbolTable;

import static lex.Token.MAX_IDENTIFIER_SIZE;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.nio.CharBuffer;

/*
 * TODO: Assignment #1
 */
public class Tokenizer
{
	private CharStream stream = null;
	private CharBuffer buffer;

	/** The KeywordTable is a SymbolTable that comes with all of the KeywordEntries
	 *  already inserted.
	 */
	private KeywordTable keywordTable;
	private SymbolTable table;

	public Tokenizer(String filename) throws IOException, LexicalError
	{
		super();
		init(new CharStream(filename));
	}

	/** Used during testing to read files from the classpath. */
	public Tokenizer(URL url) throws IOException, LexicalError
	{
		super();
		init(new CharStream(url));
	}

	public Tokenizer(File file) throws IOException, LexicalError
	{
		super();
		init(new CharStream(file));
	}

	protected void init(CharStream stream)
	{
		this.stream = stream;
		keywordTable = new KeywordTable();
		buffer = CharBuffer.allocate(MAX_IDENTIFIER_SIZE);
		// TODO more initialization will be needed...
	}

	public int getLineNumber()
	{
		return -1;
	}

	public Token getNextToken() throws LexicalError
	{
		Token token;
		buffer.clear();
		char c = stream.currentChar();
		if (Character.isLetter
				(c))
			token = readIdentifier(c);
		else if (Character.isDigit(c))
			token = readNumber(c);
		else
			token = readSymbol(c);
		return null;
	}

	private Token readIdentifier(char nextChar) throws LexicalError {
		Token result;
		while (Character.isDigit(nextChar) || Character.isLetter(nextChar)) {
			try { buffer.append(nextChar); }
			catch (BufferOverflowException e) {
				throw LexicalError.IdentifierTooLong(buffer.toString());
			}
			nextChar = stream.currentChar();
		}

		if (!(nextChar == CharStream.BLANK)) {
			stream.pushBack(nextChar);
		}
		result = new Token();
		result.setType(TokenType.IDENTIFIER);
		result.setValue(buffer.toString());
		return result;
	}

	private Token readNumber(char nextChar) throws LexicalError {
		Token result;
		while (Character.isDigit(nextChar)) {
			try { buffer.append(nextChar); }
			catch (BufferOverflowException e) {
				throw LexicalError.IdentifierTooLong(buffer.toString());
			}
			nextChar = stream.currentChar();
		}

		if (!(nextChar == CharStream.BLANK)) {
			stream.pushBack(nextChar);
		}
		result = new Token();
		result.setType(TokenType.IDENTIFIER);
		result.setValue(buffer.toString());
		return result;
	}

	private Token readSymbol(char c) {

	}

	// TODO Much (much) more code goes here...
}
