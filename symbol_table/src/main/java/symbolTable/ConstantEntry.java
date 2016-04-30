package symbolTable;

import lex.TokenType;

/**
 * Created by jayce on 4/2/16.
 */
public class ConstantEntry extends SymbolTableEntry {
    public ConstantEntry(String name, TokenType type) {
        super(name, type);
    }

    public boolean isConstant() { return true; }
}
