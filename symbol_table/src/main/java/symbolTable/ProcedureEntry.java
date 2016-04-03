package symbolTable;

import lex.TokenType;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by jayce on 4/2/16.
 */
public class ProcedureEntry  extends SymbolTableEntry {

    private  int numberOfParameters;
    private ArrayList<Object> parameterInfo;

    public ProcedureEntry(String name, TokenType type, int numberOfParameters, ArrayList<Object> parameterInfo) {
        super(name, type);
        this.numberOfParameters = numberOfParameters;
        this.parameterInfo = parameterInfo;
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

    @Override
    public boolean isProcedure() { return true; }
}
