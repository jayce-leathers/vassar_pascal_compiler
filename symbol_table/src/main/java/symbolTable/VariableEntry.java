package symbolTable;

import lex.TokenType;

/**
 * Created by jayce on 4/2/16.
 */
public class VariableEntry extends SymbolTableEntry  {

    private int address;

    public VariableEntry(String name, TokenType type, int address) {
        super(name, type);
        this.address = address;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public boolean isVariable() {
        return true;
    }
}
