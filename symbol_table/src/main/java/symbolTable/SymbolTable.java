package symbolTable;

import com.sun.javafx.fxml.expression.Expression;
import errors.SymbolTableError;
import lex.TokenType;
import sun.security.jgss.TokenTracker;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

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
			//key not found. Should we do something  ¯\_(ツ)_/¯
		}
		return result;
	}

	public void insert(SymbolTableEntry entry) throws SymbolTableError
	{
		SymbolTableEntry result = symTable.putIfAbsent(entry.getName(), entry);
		if (result == null) {
			//insertion success
//			return result; // it doesn't make sense to return anything since it would always be null also the caller already has a reference to entry so returning that would be superfluous.
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
		symTable.forEach((key,entry) -> System.out.println(entry.getName()));
	}

	public static void installBuiltins(SymbolTable table) {
			Stream.of(
					new ProcedureEntry("MAIN", TokenType.PROCEDURE, 0, null),
					new ProcedureEntry("READ", TokenType.PROCEDURE, 0, null),
					new ProcedureEntry("WRITE", TokenType.PROCEDURE, 0, null),
					new IODeviceEntry("INPUT"),
					new IODeviceEntry("OUTPUT"))
					.forEach((entry) -> {
							entry.setReserved(true);//reserve the keyword
							table.insertQuietly(entry);//insert quietly
					});
	}

	//moved from keywordtable in order to use here. protected for access in extended classes
	protected void insertQuietly(SymbolTableEntry entry) {
		try
		{
			this.insert(entry);
		}
		catch (SymbolTableError symbolTableError)
		{
			// Ignore
		}
	}

}
