package symbolTable;

import lex.TokenType;

import java.util.ArrayList;

/**
 * Created by jayce on 4/2/16.
 */
public class FunctionEntry extends SymbolTableEntry {

    private int numberOfParameters;
    private ArrayList<Object >parameterInfo;
    private VariableEntry result;

    public FunctionEntry(String name, TokenType type, int numberOfParameters, ArrayList<Object> parameterInfo, VariableEntry result) {
        super(name, type);
        this.numberOfParameters = numberOfParameters;
        this.parameterInfo = parameterInfo;
        this.result = result;
    }

    public int getNumberOfParameters() {
        return numberOfParameters;
    }

    public void setNumberOfParameters(int numberOfParameters) {
        this.numberOfParameters = numberOfParameters;
    }

    public ArrayList<Object> getParameterInfo() {
        return parameterInfo;
    }

    public void setParameterInfo(ArrayList<Object> parameterInfo) {
        this.parameterInfo = parameterInfo;
    }

    public VariableEntry getResult() {
        return result;
    }

    public void setResult(VariableEntry result) {
        this.result = result;
    }

    public boolean isFunction() { return true; }
}
