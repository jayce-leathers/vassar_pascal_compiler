package symbolTable;

import lex.TokenType;

/**
 * Created by jayce on 4/2/16.
 */
public class ArrayEntry extends SymbolTableEntry {
    private int address;
    private int upperBound;
    private int lowerBound;

    public ArrayEntry(String name, TokenType type, int address,int upperBound, int lowerBound) {
        super(name, type);
        this.address = address;
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
    }

    public int getAddress() {
        return address;
    }

    public int getUBound() {
        return upperBound;
    }

    public int getLBound() {
        return lowerBound;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public void setUpperBound(int upperBound) {
        this.upperBound = upperBound;
    }

    public void setLowerBound(int lowerBound) {
        this.lowerBound = lowerBound;
    }

    public boolean isArray() { return true; }

}
