package lex;

import errors.LexicalError;
import symbolTable.KeywordTable;
import symbolTable.SymbolTable;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/*
 * TODO: Assignment #1
 */
public class Tokenizer
{
	private CharStream stream = null;

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
		// TODO more initialization will be needed...
	}

	public int getLineNumber()
	{
		return -1;
	}

	public Token getNextToken() throws LexicalError
	{
		return null;
	}

	// TODO Much (much) more code goes here...
}
