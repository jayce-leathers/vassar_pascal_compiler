package semanticActions;

import static errors.SemanticError.*;

import errors.CompilerError;
import errors.SemanticError;

import errors.SymbolTableError;
import lex.Token;
import lex.TokenType;
import lex.Tokenizer;
import symbolTable.*;

import java.util.Stack;

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

	public void execute(int actionNumber, Token token) throws SemanticError {

		debug("calling action : " + actionNumber + " with token " + token.getType() + " with value " + token.getValue());

		// TODO Implement actions 1, 2, 3, 4, 6, 7, 9, and 13.
		switch (actionNumber)
		{

			case 1:
				this.insert = true;
				break;
			case 2:
				this.insert = false;
				break;
			case 3:
				TokenType type = ((Token)semanticStack.pop()).getType();
				if(isArray){
					int upperBound = Integer.parseInt(((Token)semanticStack.pop()).getValue());
					int lowerBound = Integer.parseInt(((Token)semanticStack.pop()).getValue());
					int memSize = (upperBound - lowerBound) + 1;
					Token id;
					while(!semanticStack.isEmpty()){
						id = (Token)semanticStack.pop();
						int address;
						if(global) {
							address = globalMemory;
							globalMemory = globalMemory + memSize;
							try {
								globalTable.insert(new ArrayEntry(id.getValue(),type,address,upperBound,lowerBound));
							} catch (SymbolTableError symbolTableError) {
								if(globalTable.lookup(id.getValue()).isReserved()) {
									throw ReservedName(id.getValue(),tokenizer.getLineNumber());
								}
								else {
									throw MultipleDeclaration(id.getValue(),tokenizer.getLineNumber());
								}
							}
						}
						else {
							address = localMemory;
							localMemory = localMemory + memSize;
//							localTable.insert(new ArrayEntry(id.getValue(),id.getType(),address,upperBound,lowerBound));
						}
					}
				}
				else {
					Token id;
					while(!semanticStack.isEmpty()){
						id = (Token)semanticStack.pop();
						int address;
						if(global) {
							address = globalMemory;
							globalMemory++;
							try {
								globalTable.insert(new VariableEntry(id.getValue(),type,address));
							} catch (SymbolTableError symbolTableError) {
								if(globalTable.lookup(id.getValue()).isReserved()) {
									throw ReservedName(id.getValue(),tokenizer.getLineNumber());
								}
								else {
									throw MultipleDeclaration(id.getValue(),tokenizer.getLineNumber());
								}
							}
						}
						else {
							address = localMemory;
							localMemory++;
//							localTable.insert(new VariableEntry(id.getValue(),id.getType(),address));
						}
					}
				}
				isArray = false;
				break;
			case 4:
				semanticStack.push(token);
				break;
			case 6:
				this.isArray = true;
				break;
			case 7:
				semanticStack.push(token);
				break;
			case 9:
				Token id1 = (Token) semanticStack.pop();
				SymbolTableEntry result1 = globalTable.lookup(id1.getValue());
				if(result1 == null){
					throw new SemanticError(Type.RESERVED_NAME,
							">>> ERROR on line " + tokenizer.getLineNumber() + " : Expected Input IO Declaration Not Found");
				}
				Token id2 = (Token) semanticStack.pop();
				SymbolTableEntry result2 = globalTable.lookup(id2.getValue());
				if(result2 == null){
					throw new SemanticError(Type.RESERVED_NAME,
							">>> ERROR on line " + tokenizer.getLineNumber() + " : Expected Input IO Declaration Not Found");
				}
				Token id3 = (Token) semanticStack.pop();
				ProcedureEntry entry3 = new ProcedureEntry(id3.getValue(),id3.getType(),0,null);
				entry3.setReserved(true);
				try {
					globalTable.insert(entry3);
				} catch (SymbolTableError symbolTableError) {
					throw MultipleDeclaration(id2.getValue(),tokenizer.getLineNumber());
				}
				insert = false;
				break;
			case 13:
				semanticStack.push(token);
				break;


			default:
				// TODO Eventually (i.e. final project) this should throw an exception.
				debug("Action " + actionNumber + " not yet implemented.");

		}
	}

	public SymbolTableEntry lookup(String name)
	{	return globalTable.lookup(name);
	}

	public ConstantEntry lookupConstant(Token token)
	{
		return (ConstantEntry)constantTable.lookup(token.getValue());
	}

	private void installBuiltins(SymbolTable table)
	{
		SymbolTable.installBuiltins(table);
	}


	private void debug(String message) {
		// TODO Uncomment the following line to enable debug output.
		System.out.println(message);
	}

}