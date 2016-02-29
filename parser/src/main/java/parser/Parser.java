/*
 * Copyright 2016 Vassar College
 * All rights reserverd.
 */

package parser;


import errors.LexicalError;
import errors.SyntaxError;
import lex.Tokenizer;

import java.io.File;
import java.io.IOException;


public class Parser
{
	private static final int ERROR = 999;

	private ParseTable parsetable = new ParseTable();
	private RHSTable rhsTable = new RHSTable();

	private Tokenizer tokenizer;


	public Parser (String filename) throws IOException, LexicalError
	{
		this(new File(filename));
	}

	public Parser(File file) throws IOException, LexicalError
	{
		tokenizer = new Tokenizer(file);
	}

	public void parse () throws SyntaxError, LexicalError {
		// TODO Write a parser.
	}

	public boolean error (){
		return true;
	}

}

