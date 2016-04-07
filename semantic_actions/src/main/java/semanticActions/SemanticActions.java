package semanticActions;

import java.lang.*;
import errors.*;
import java.util.*;

import lex.*;
import symbolTable.*;

public class SemanticActions {

	private Stack<Object> semanticStack ;
	private boolean insert ;
	private boolean isArray ;
	private boolean global ;
	private int globalMemory ;
	private int localMemory ;

	private SymbolTable globalTable ;
	private SymbolTable localTable ;


	private SymbolTable constantTable ;

	private int tableSize = 97;
	private boolean isParam;
	private SymbolTableEntry nullEntry = null;
	private Tokenizer tokenizer;

	public SemanticActions(Tokenizer tokenizer) {
		semanticStack = new Stack<Object>();
		insert = false;
		isArray = false;
		isParam = false;
		global = true;
		globalMemory = 0 ;
		localMemory = 0;
		globalTable = new SymbolTable(tableSize);
		constantTable = new SymbolTable(tableSize);
		installBuiltins(globalTable);
		this.tokenizer = tokenizer;
	}

	public void execute(int actionNumber, Token token)  throws SemanticError {

		debug("calling action : " + actionNumber + " with token " + token.getType() + " with value " + token.getValue());

		// TODO Implement actions 1, 2, 3, 4, 6, 7, 9, and 13.
		switch (actionNumber)
		{

			case 1:
				break;


			default:
				// TODO Eventually (i.e. final project) this should throw an exception.
				debug("Action " + actionNumber + " not yet implemented.");

		}
	}

	public SymbolTableEntry lookup(String name)
	{
		return null;
	}

	public ConstantEntry lookupConstant(Token token)
	{
		return null;
	}

	private void installBuiltins(SymbolTable table)
	{

	}


	private void debug(String message) {
		// TODO Uncomment the following line to enable debug output.
//		System.out.println(message);
	}

}