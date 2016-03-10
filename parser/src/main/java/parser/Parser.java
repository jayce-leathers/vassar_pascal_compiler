/*
 * Copyright 2016 Vassar College
 * All rights reserverd.
 */

package parser;


import errors.LexicalError;
import errors.SyntaxError;
import grammar.GrammarSymbol;
import grammar.NonTerminal;
import lex.Token;
import lex.TokenType;
import lex.Tokenizer;

import java.io.File;
import java.io.IOException;
import java.util.Stack;


public class Parser
{
	private static final int ERROR = 999;
    private static final boolean ERROR_RECOVERY = false;
    private static final boolean DEBUGGING = false;
	private ParseTable parsetable = new ParseTable();
	private RHSTable rhsTable = new RHSTable();
	private Stack<GrammarSymbol> stack = new Stack<>();

	private Tokenizer tokenizer;
	private Token currentToken;

	private Boolean errored = false;


	public Parser (String filename) throws IOException, LexicalError
	{
		this(new File(filename));
	}

	public Parser(File file) throws IOException, LexicalError
	{
		tokenizer = new Tokenizer(file);
	}

	public void parse () throws SyntaxError, LexicalError {
		GrammarSymbol predicted;
		currentToken = tokenizer.getNextToken();
		stack.clear();
		stack.push(TokenType.ENDOFFILE);
		stack.push(NonTerminal.Goal);

		while(!stack.empty()) {
			predicted = stack.pop();
			if (predicted.isToken()) {
				if (currentToken.getType() == predicted) {
					currentToken = tokenizer.getNextToken();
				}
				else { //current != predicted
					errored = true;
                    if(ERROR_RECOVERY) {
                        handleError(predicted);
                    }
                    else {
                        System.out.println(SyntaxError.BadToken(currentToken.getType(),tokenizer.getLineNumber(),
                                "expected: " + predicted.toString() + " found: " + currentToken.toString()).getMessage());
                        break;
                    }
				}
			}
			else if (predicted.isNonTerminal()) {
				int parseValue = parsetable.getEntry(currentToken.getType(), (NonTerminal) predicted);
				if(parseValue == ERROR) {
					errored = true;
                    if(ERROR_RECOVERY) {
                        handleError(predicted);
                    }
                    else {
                        System.out.println(SyntaxError.BadToken(currentToken.getType(),tokenizer.getLineNumber(),
                                "expected: " + predicted.toString() + " found: " + currentToken.toString()).getMessage());
                        break;
                    }
                }
				else {
                    if(parseValue > 0) {
                        GrammarSymbol[] rhs = rhsTable.getRule(parseValue);
                        for (int i = rhs.length - 1; i >= 0; i--) {
                            stack.push(rhs[i]);
                        }
                    }
				}
			}
			else {//predicted is action
			}
		}
	}

    private void dumpStack() {
        if(DEBUGGING) {
            while (!stack.empty()) {
                System.out.println(stack.pop().toString());
            }
        }
    }

    private void handleError(GrammarSymbol predicted) throws LexicalError, SyntaxError {
        currentToken = tokenizer.getNextToken();
        int count = 0;
        int COUNT_MAX = 25;
        while (currentToken.getType() != TokenType.SEMICOLON && currentToken.getType() != TokenType.ENDOFFILE) {
            count++;
            currentToken = tokenizer.getNextToken();
            stack.pop();
        }
    }

	public boolean error (){
		return errored;
	}

}

