package edu.vassar.cs.cmpu331.lex;

import edu.vassar.cs.cmpu331.error.LexicalError;
import edu.vassar.cs.cmpu331.symbolTable.KeywordEntry;
import edu.vassar.cs.cmpu331.symbolTable.SymbolTable;

public class Tokenizer {

    public Tokenizer(String filename) {

    }

    public int getLineNumber() {
       
    }

    public Token getNextToken() throws LexicalError {

    }

    public void installKeywords(SymbolTable KeywordTable) {

        KeywordEntry entry = new KeywordEntry("PROGRAM", TokenType.PROGRAM);
        KeywordTable.insert(entry);
        entry = new KeywordEntry("BEGIN", TokenType.BEGIN);
        KeywordTable.insert(entry);
        entry = new KeywordEntry("END", TokenType.END);
        KeywordTable.insert(entry);
        entry = new KeywordEntry("VAR", TokenType.VAR);
        KeywordTable.insert(entry);
        entry = new KeywordEntry("FUNCTION", TokenType.FUNCTION);
        KeywordTable.insert(entry);
        entry = new KeywordEntry("PROCEDURE", TokenType.PROCEDURE);
        KeywordTable.insert(entry);
        entry = new KeywordEntry("RESULT", TokenType.RESULT);
        KeywordTable.insert(entry);
        entry = new KeywordEntry("INTEGER", TokenType.INTEGER);
        KeywordTable.insert(entry);
        entry = new KeywordEntry("REAL", TokenType.REAL);
        KeywordTable.insert(entry);
        entry = new KeywordEntry("ARRAY", TokenType.ARRAY);
        KeywordTable.insert(entry);
        entry = new KeywordEntry("OF", TokenType.OF);
        KeywordTable.insert(entry);
        entry = new KeywordEntry("IF", TokenType.IF);
        KeywordTable.insert(entry);
        entry = new KeywordEntry("THEN", TokenType.THEN);
        KeywordTable.insert(entry);
        entry = new KeywordEntry("ELSE", TokenType.ELSE);
        KeywordTable.insert(entry);
        entry = new KeywordEntry("WHILE", TokenType.WHILE);
        KeywordTable.insert(entry);
        entry = new KeywordEntry("DO", TokenType.DO);
        KeywordTable.insert(entry);
        entry = new KeywordEntry("AND", TokenType.MULOP);
        KeywordTable.insert(entry);
        entry = new KeywordEntry("OR", TokenType.ADDOP);
        KeywordTable.insert(entry);
        entry = new KeywordEntry("DIV", TokenType.MULOP);
        KeywordTable.insert(entry);
        entry = new KeywordEntry("MOD", TokenType.MULOP);
        KeywordTable.insert(entry);
        entry = new KeywordEntry("NOT", TokenType.NOT);
        KeywordTable.insert(entry);

    }
}
