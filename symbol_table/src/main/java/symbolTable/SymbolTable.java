package symbolTable;

import errors.SymbolTableError;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

public class SymbolTable {

	private HashMap<String, SymbolTableEntry> symTable;

	public SymbolTable() {
		symTable = new HashMap<>();
	}

	public SymbolTable (int size)
	{
		symTable = new HashMap<>(size);
	}

	public SymbolTableEntry lookup (String key)
	{
		SymbolTableEntry result = symTable.get(key);
		if(result == null) {

		}
		return result;
	}

	public SymbolTableEntry insert(SymbolTableEntry entry) throws SymbolTableError
	{
		SymbolTableEntry result = symTable.putIfAbsent(entry.getName(), entry);
		if (result == null) {
			//insertion success
			return result;
		}
		else {
			//failure
			throw SymbolTableError.DuplicateEntry(entry.getName());
		}
	}

	public int size() {
		return symTable.size();
	}

	public void dumpTable () {
	}

	public static void installBuiltins(SymbolTable table) {

	}

}
