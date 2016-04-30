/*
 * Copyright 2016 Vassar College
 * All rights reserverd.
 */

package parser;


import errors.LexicalError;
import errors.SemanticError;
import errors.SymbolTableError;
import errors.SyntaxError;
import grammar.GrammarSymbol;
import grammar.NonTerminal;
import grammar.SemanticAction;
import lex.Token;
import lex.TokenType;
import lex.Tokenizer;
import semanticActions.SemanticActions;
import symbolTable.SymbolTableEntry;

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
	private SemanticActions semanticActions;
	private Tokenizer tokenizer;
	private Token currentToken;
	private Token lastToken;

	private Boolean errored = false;


	public Parser (String filename) throws IOException, LexicalError
	{
		this(new File(filename));
	}

	public Parser(File file) throws IOException, LexicalError
	{
		tokenizer = new Tokenizer(file);
		semanticActions = new SemanticActions(tokenizer);
	}

	public void parse () throws SyntaxError, LexicalError, SemanticError, SymbolTableError {
		GrammarSymbol predicted;
		currentToken = tokenizer.getNextToken();
		stack.clear();
		stack.push(TokenType.ENDOFFILE);
		stack.push(NonTerminal.Goal);

		while(!stack.empty()) {
			predicted = stack.pop();
			if (predicted.isToken()) {
				if (currentToken.getType() == predicted) {
					lastToken = currentToken;
					currentToken = tokenizer.getNextToken();
				}
				else { //current != predicted
					errored = true;
                    if(ERROR_RECOVERY) {
                        handleError(predicted);
                    }
                    else {
                        throw SyntaxError.BadToken(currentToken.getType(),tokenizer.getLineNumber(),
                                "expected: " + predicted.toString() + " found: " + currentToken.toString());
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
						throw SyntaxError.BadToken(currentToken.getType(),tokenizer.getLineNumber(),
								"expected: " + predicted.toString() + " found: " + currentToken.toString());
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
//				currentToken = tokenizer.getNextToken();
				semanticActions.execute(((SemanticAction)predicted).getIndex(),lastToken);
			}
		}
	}

	public void printTVI() {
		semanticActions.printTVI();
	}

	public SymbolTableEntry lookup(String name){
		return semanticActions.lookup(name);
	}

    private void dumpStack() {
        if(DEBUGGING) {
            stack.forEach(x-> System.out.println(x.toString()));
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

