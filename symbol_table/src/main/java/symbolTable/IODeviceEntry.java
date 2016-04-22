package symbolTable;

/**
 * Created by jayce on 4/2/16.
 */
public class IODeviceEntry  extends SymbolTableEntry  {

    public IODeviceEntry(String name) {
        super(name);
    }

    @Override
    public boolean isVariable() {
        return true;
    }
}
