package symbolTable;

import errors.SymbolTableError;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

public class SymbolTable {

	private HashMap<String, SymbolTableEntry> symTable;

	public SymbolTable() {

	}

	public SymbolTable (int size)
	{

	}

	public SymbolTableEntry lookup (String key)
	{
		return null;
	}

	public void insert(SymbolTableEntry entry) throws SymbolTableError
	{
	}

	public int size() {
		return -1;
	}

	public void dumpTable () {
	}

	public static void installBuiltins(SymbolTable table) {
	}

}
