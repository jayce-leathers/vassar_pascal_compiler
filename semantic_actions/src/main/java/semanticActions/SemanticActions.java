package semanticActions;

import static errors.SemanticError.*;
import static java.lang.Math.abs;
import static lex.Token.OperatorType.*;

import errors.CompilerError;
import errors.SemanticError;

import errors.SymbolTableError;
import lex.Token;
import lex.Token.OperatorType;
import lex.TokenType;
import lex.Tokenizer;
import symbolTable.*;

import java.util.Stack;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class SemanticActions {

	private Stack<Object> semanticStack ;
	private boolean insert ;
	private boolean isArray ;
	private boolean global ;
	private int globalMemory ;
	private int localMemory ;
	private int globalStore;
	private static int tempCounter;
	private final String TEMP_NAME = "$$TEMP";
	private SymbolTable globalTable ;
	private SymbolTable localTable ;
	private SymbolTable constantTable ;

	public  enum ETYPE {
		ARITHMETIC,RELATIONAL
	}

	public  enum OFFSET {
		NULL
	}
	private int tableSize = 97;
	private boolean isParam;
	private SymbolTableEntry nullEntry = null;
	private Tokenizer tokenizer;

	private Quadruples quads;

	public SemanticActions(Tokenizer tokenizer) {
		semanticStack = new Stack<Object>();
		insert = false;
		isArray = false;
		isParam = false;
		global = true;
		globalMemory = 0;
		localMemory = 0;
		globalStore = 0;
		globalTable = new SymbolTable(tableSize);
		constantTable = new SymbolTable(tableSize);
		installBuiltins(globalTable);
		this.tokenizer = tokenizer;
		this.quads = new Quadruples();
	}

	public void execute(int actionNumber, Token token) throws SemanticError {

		debug("calling action : " + actionNumber + " with token " + token.getType() + " with value " + token.getValue());

		switch (actionNumber) {
			case 1:
				this.insert = true;
				break;
			case 2:
				this.insert = false;
				break;
			case 3:
				TokenType type = ((Token) semanticStack.pop()).getType();
				if (isArray) {
					int upperBound = Integer.parseInt(((Token) semanticStack.pop()).getValue());
					int lowerBound = Integer.parseInt(((Token) semanticStack.pop()).getValue());
					int memSize = (upperBound - lowerBound) + 1;
					Token id;
					while (!semanticStack.isEmpty()) {
						id = (Token) semanticStack.pop();
						int address;
						if (global) {
							address = globalMemory;
							globalMemory = globalMemory + memSize;
							try {
								globalTable.insert(new ArrayEntry(id.getValue(), type, address, upperBound, lowerBound));
							} catch (SymbolTableError symbolTableError) {
								if (globalTable.lookup(id.getValue()).isReserved()) {
									throw ReservedName(id.getValue(), tokenizer.getLineNumber());
								} else {
									throw MultipleDeclaration(id.getValue(), tokenizer.getLineNumber());
								}
							}
						} else {
							address = localMemory;
							localMemory = localMemory + memSize;
							try {
								localTable.insert(new ArrayEntry(id.getValue(), id.getType(), address, upperBound, lowerBound));
							} catch (SymbolTableError symbolTableError) {
								if (localTable.lookup(id.getValue()).isReserved()) {
									throw ReservedName(id.getValue(), tokenizer.getLineNumber());
								} else {
									throw MultipleDeclaration(id.getValue(), tokenizer.getLineNumber());
								}
							}
						}
					}
				} else {
					Token id;
					while (!semanticStack.isEmpty()) {
						id = (Token) semanticStack.pop();
						int address;
						if (global) {
							address = globalMemory;
							globalMemory++;
							try {
								globalTable.insert(new VariableEntry(id.getValue(), type, address));
							} catch (SymbolTableError symbolTableError) {
								if (globalTable.lookup(id.getValue()).isReserved()) {
									throw ReservedName(id.getValue(), tokenizer.getLineNumber());
								} else {
									throw MultipleDeclaration(id.getValue(), tokenizer.getLineNumber());
								}
							}
						} else {
							address = localMemory;
							localMemory++;
							try {
								localTable.insert(new VariableEntry(id.getValue(), id.getType(), address));
							} catch (SymbolTableError symbolTableError) {
								if (localTable.lookup(id.getValue()).isReserved()) {
									throw ReservedName(id.getValue(), tokenizer.getLineNumber());
								} else {
									throw MultipleDeclaration(id.getValue(), tokenizer.getLineNumber());
								}
							}
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
			case 9: {
				Token id1 = (Token) semanticStack.pop();
				SymbolTableEntry result1 = globalTable.lookup(id1.getValue());
				if (result1 == null) {
					throw new SemanticError(Type.RESERVED_NAME,
							">>> ERROR on line " + tokenizer.getLineNumber() + " : Expected Input IO Declaration Not Found");
				}
				Token id2 = (Token) semanticStack.pop();
				SymbolTableEntry result2 = globalTable.lookup(id2.getValue());
				if (result2 == null) {
					throw new SemanticError(Type.RESERVED_NAME,
							">>> ERROR on line " + tokenizer.getLineNumber() + " : Expected Input IO Declaration Not Found");
				}
				Token id3 = (Token) semanticStack.pop();
				ProcedureEntry entry3 = new ProcedureEntry(id3.getValue(), id3.getType(), 0, null);
				entry3.setReserved(true);
				try {
					globalTable.insert(entry3);
				} catch (SymbolTableError symbolTableError) {
					throw MultipleDeclaration(id2.getValue(), tokenizer.getLineNumber());
				}
				insert = false;
				generate("CODE");
				generate("call", "main", "0");
				generate("exit");
			}
			break;
			case 13:
				semanticStack.push(token);
				break;
			case 30: {
				SymbolTableEntry id = globalTable.lookup(token.getValue());
				if (id == null) {
					throw UndeclaredVariable(token.getValue(), tokenizer.getLineNumber());
				}
				semanticStack.push(id);
				semanticStack.push(ETYPE.ARITHMETIC);
			}
				break;
			case 31: {
				ETYPE etype = (ETYPE) semanticStack.pop();
				if (etype != ETYPE.ARITHMETIC) {
					throw ETypeMismatch(tokenizer.getLineNumber());
				} else {
					SymbolTableEntry id2 = (SymbolTableEntry) semanticStack.pop();
					Object offset = semanticStack.pop();
					SymbolTableEntry id1 = (SymbolTableEntry) semanticStack.pop();
					int typeCheckResult = typeCheck(id1, id2);
					if (typeCheckResult == 3) {
						throw TypeMismatch(tokenizer.getLineNumber());
					}
					if (typeCheckResult == 2) {
						VariableEntry temp = create(TokenType.REAL);
						generate("ltof", id2, temp);
						if (offset == OFFSET.NULL) {
							generate("move", temp, id1);
						}
						else {
							generate("stor", temp, (SymbolTableEntry) offset,id1);
						}
					} else if (offset == OFFSET.NULL) {
						generate("move", id2, id1);
					} else {
						generate("stor", id2, (SymbolTableEntry) offset, id1);
					}
				}
			}
				break;
			case 32:
			{
				ETYPE etype = (ETYPE) semanticStack.pop();//pop etype?
				if(etype != ETYPE.ARITHMETIC) {
					throw ETypeMismatch(tokenizer.getLineNumber());
				}
				SymbolTableEntry id = (SymbolTableEntry) semanticStack.peek();
				if(!id.isArray()) {
					throw NotArray(tokenizer.getLineNumber());
				}
			}
				break;
			case 33: {
				ETYPE etype = (ETYPE) semanticStack.pop();
				if (etype != ETYPE.ARITHMETIC) {
					throw ETypeMismatch(tokenizer.getLineNumber());
				}
				SymbolTableEntry id = (SymbolTableEntry) semanticStack.pop();
				if(id.getType() != TokenType.INTEGER) {
					throw InvalidSubscript(tokenizer.getLineNumber());
				}
				VariableEntry temp = create(TokenType.INTEGER);
				//MARK: maybe semanticstack.lastElement
				ArrayEntry arrayEntry = (ArrayEntry) semanticStack.lastElement();
				ConstantEntry lbound = new ConstantEntry(Integer.toString(arrayEntry.getLBound()),TokenType.INTEGER);
				generate("sub",id,lbound,temp);
				semanticStack.push(temp);
			}
				break;
			case 34: {
				semanticStack.pop();//pop etype
				SymbolTableEntry id = (SymbolTableEntry) semanticStack.peek();
				if (id.isFunction()) {
					execute(52,token);
				} else {
					semanticStack.push(OFFSET.NULL);
				}
			}
				break;
			case 40://uminus uplus
				semanticStack.push(token);
				break;
			case 41: //uminus uplus
				ETYPE etype = (ETYPE) semanticStack.pop();
				if (etype == ETYPE.ARITHMETIC) {
					semanticStack.push(token);
				}
				Token sign = (Token) semanticStack.pop();
				SymbolTableEntry id = (SymbolTableEntry) semanticStack.pop();
				if (sign.getType() == TokenType.UNARYMINUS) {
					VariableEntry temp = create(id.getType());
					generate("uminus", id, temp);
					semanticStack.push(temp);
				} else {
					semanticStack.push(id);
				}
				semanticStack.push(ETYPE.ARITHMETIC);
			break;
			case 42:
				etype = (ETYPE) semanticStack.pop();
				if(token.getOpType() == OR) {
					//TODO: RELATIONAL
				} else {
					if(etype != ETYPE.ARITHMETIC) {
						throw ETypeMismatch(tokenizer.getLineNumber());
					}
				}
				semanticStack.push(token);
				break;
			case 43: {
				//TODO: RELATIONAL CHANGES
				etype = (ETYPE) semanticStack.pop();
				//TODO:check order of pops
				SymbolTableEntry id2 = (SymbolTableEntry) semanticStack.pop();
				Token operator = (Token) semanticStack.pop();
				SymbolTableEntry id1 = (SymbolTableEntry) semanticStack.pop();
				if (etype != ETYPE.ARITHMETIC) {
					throw ETypeMismatch(tokenizer.getLineNumber());
				}
				String tvicode;

				switch (operator.getOpType()) {
					case ADD:
						tvicode = "add";
						break;
					case SUBTRACT:
						tvicode = "sub";
						break;
					case MULTIPLY:
						tvicode = "mul";
						break;
					case DIVIDE:
					case INTEGERDIVIDE:
						tvicode = "div";
						break;
					default:
						tvicode = "ERROR";
						break;
				}
				int typeCheckResult = typeCheck(id1, id2);
				VariableEntry temp;
				VariableEntry temp2;
				if (typeCheckResult == 0) {
					temp = create(TokenType.INTEGER);
					generate(tvicode,id1,id2,temp);
					semanticStack.push(temp);
				}
				else if (typeCheckResult == 1) {
					temp = create(TokenType.REAL);
					generate("f" + tvicode,id1,id2,temp);
					semanticStack.push(temp);
				}
				else if (typeCheckResult == 2) {
					temp = create(TokenType.REAL);
					generate("ltof",id2,temp);
					temp2 = create(TokenType.REAL);
					generate("f" + tvicode, id1,temp, temp2);
					semanticStack.push(temp2);
				}
				else {
					temp = create(TokenType.REAL);
					generate("ltof",id1,temp);
					temp2 = create(TokenType.REAL);
					generate("f" + tvicode, id2,temp, temp2);
					semanticStack.push(temp2);
				}
				semanticStack.push(ETYPE.ARITHMETIC);
			}
				break;
			case 44:
				//TODO:RELATIONAL
				semanticStack.pop();
				semanticStack.push(token);
				break;
			case 45: {
				etype = (ETYPE) semanticStack.pop();
				SymbolTableEntry id1 = (SymbolTableEntry) semanticStack.pop();
				Token operator = (Token) semanticStack.pop();
				SymbolTableEntry id2 = (SymbolTableEntry) semanticStack.pop();
				if (etype != ETYPE.ARITHMETIC) {
					throw ETypeMismatch(tokenizer.getLineNumber());
				}

				String tvicode;
				switch (operator.getOpType()) {
					case ADD:
						tvicode = "add";
						break;
					case SUBTRACT:
						tvicode = "sub";
						break;
					case MULTIPLY:
						tvicode = "mul";
						break;
					case DIVIDE:
						tvicode = "div";
						break;
					default:
						tvicode = "ERROR";
						break;
				}
				int typeCheckResult = typeCheck(id1,id2);
//				if(operator.getValue() == "MOD" && typeCheckResult != 0) {
//					throw BadMODoperands(tokenizer.getLineNumber());
//				}
				switch (typeCheckResult) {
					case 0:
//						if(operator.getOpType() == MOD) {
//							VariableEntry temp1 = create(INTEGER);
//							generate("move",id1,temp1);
//							VariableEntry temp2 = create(INTEGER);
//							generate("move",temp1,temp2);
//							generate("sub", temp2, id2,temp1);
//							generate("bge",temp1,id2,Integer.toString(quads.getNextQuad() - 2));
//							semanticStack.push(temp2); //is this right?!?
//
//						}
						if (operator.getOpType() == DIVIDE) {
							VariableEntry temp1 = create(TokenType.REAL);
							generate("ltof",id1,temp1);
							VariableEntry temp2 = create(TokenType.REAL);
							generate("ltof",id2,temp2);
							VariableEntry temp3 = create(TokenType.REAL);
							generate("fdiv",temp1,temp2,temp3);
							semanticStack.push(temp3);
						}
						else {
							VariableEntry temp = create(TokenType.INTEGER);
							generate(tvicode,id1,id2,temp);
							semanticStack.push(temp);
						}
						break;
					case 1:
						if (operator.getOpType() == INTEGERDIVIDE) {
							VariableEntry temp1 = create(TokenType.INTEGER);
							generate("ftol",id1,temp1);
							VariableEntry temp2 = create(TokenType.INTEGER);
							generate("ftol",id2,temp2);
							VariableEntry temp3 = create(TokenType.INTEGER);
							generate("div",temp1,temp2,temp3);
							semanticStack.push(temp3);
						}
						else {
							VariableEntry temp = create(TokenType.REAL);
							generate("f" + tvicode,id1,id2,temp);
							semanticStack.push(temp);
						}
						break;
					case 2:
						if (operator.getOpType() == INTEGERDIVIDE) {
							VariableEntry temp1 = create(TokenType.INTEGER);
							generate("ftol",id1,temp1);
							VariableEntry temp2 = create(TokenType.INTEGER);
							generate("div",temp1,id2,temp2);
							semanticStack.push(temp2);
						}
						else {
							VariableEntry temp1 = create(TokenType.REAL);
							generate("ltof",id2,temp1);
							VariableEntry temp2 = create(TokenType.REAL);
							generate("f" + tvicode,id1,temp1,temp2);
							semanticStack.push(temp2);
						}
						break;
					case 3:
						if (operator.getOpType() == INTEGERDIVIDE) {
							VariableEntry temp1 = create(TokenType.INTEGER);
							generate("ftol",id2,temp1);
							VariableEntry temp2 = create(TokenType.INTEGER);
							generate("div",id1,temp1,temp2);
							semanticStack.push(temp2);
						}
						else {
							VariableEntry temp1 = create(TokenType.REAL);
							generate("ltof",id1,temp1);
							VariableEntry temp2 = create(TokenType.REAL);
							generate("f" + tvicode,temp1,id2,temp2);
							semanticStack.push(temp2);
						}
						break;
				}
				semanticStack.push(ETYPE.ARITHMETIC);
			}
				break;
			case 46:
				switch (token.getType()){
					case IDENTIFIER:
						SymbolTableEntry var = (SymbolTableEntry) globalTable.lookup(token.getValue());
						if(var == null) {
							throw UndeclaredVariable(token.getValue(),tokenizer.getLineNumber());
						}
						semanticStack.push(var);
						break;
					case INTCONSTANT:
					case REALCONSTANT:
						ConstantEntry constant = (ConstantEntry) constantTable.lookup(token.getValue());
						if (constant == null) {
							ConstantEntry newConst;
							if(token.getType() == TokenType.INTCONSTANT) {
								newConst = new ConstantEntry(token.getValue(),TokenType.INTEGER);
							}
							else {
								newConst = new ConstantEntry(token.getValue(), TokenType.REAL);
							}
							try {
								constantTable.insert(newConst);
							} catch (SymbolTableError symbolTableError) {
								throw MultipleDeclaration(token.getValue(),tokenizer.getLineNumber());
							}
							semanticStack.push(newConst);
						}
						else {
							semanticStack.push(constant);
						}
						break;
				}
				semanticStack.push(ETYPE.ARITHMETIC);
				break;
			case 48:
				//TODO: VERIFY pop order
				Object offset =  semanticStack.pop();
				if (offset != OFFSET.NULL) {
					id = (SymbolTableEntry) semanticStack.pop();
//					semanticStack.pop();//pop Etype which apparently we don't need?
					VariableEntry temp = create(id.getType());
					generate("load", id, (SymbolTableEntry) offset,temp);
					semanticStack.push(temp);
				}
				semanticStack.push(ETYPE.ARITHMETIC);
				break;
			case 53:

				break;
			case 54:

				break;
			case 55:
				//backPatch(globalStore,globalMemory);
				quads.setField(globalStore,1,Integer.toString(globalMemory));
				generate("free","_" +  Integer.toString(globalMemory));
				generate("PROCEND");
				break;
			case 56:
				generate("PROCBEGIN", "main");
				globalStore = quads.getNextQuad();
				generate("alloc","");
				break;
			case 57:
				ConstantEntry constant = (ConstantEntry) constantTable.lookup(token.getValue());
				if(constant == null) {
					try {
						constant = new ConstantEntry(token.getValue(),TokenType.INTEGER);
						constantTable.insert(constant);
					} catch (SymbolTableError symbolTableError) {
						//never executes
					}
				}
				token.setEntry(constant);
				break;
			case 58:
				constant = (ConstantEntry) constantTable.lookup(token.getValue());
				if(constant == null) {
					try {
						constant = new ConstantEntry(token.getValue(),TokenType.REAL);
						constantTable.insert(constant);
					} catch (SymbolTableError symbolTableError) {
						//never executes
					}
				}
				token.setEntry(constant);
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

	private VariableEntry create(TokenType type) {
		VariableEntry temp = new VariableEntry(TEMP_NAME + tempCounter, type, -globalMemory);
		tempCounter++;
		globalMemory++;
		try {
			globalTable.insert(temp);
		} catch (SymbolTableError symbolTableError) {
			//should never error
		}
		return temp;
	}

	private int typeCheck(SymbolTableEntry id1, SymbolTableEntry id2){
		int result;
		if( id1.getType() == id2 .getType() && id1.getType() == TokenType.INTEGER) {
			result = 0;
		}
		else if (id1.getType() == id2 .getType() && id1.getType() == TokenType.REAL) {
			result = 1;
		}
		else if (id1.getType() == TokenType.REAL && id2.getType() == TokenType.INTEGER) {
			result = 2;
		}
		else {
			result = 3;
		}
		return result;
	}

	public void printTVI() {
		quads.print();
	}
	private String generatePrefix() {
		String prefix;
		if(global) {
			prefix = "_";
		}
		else {
			prefix = "%";
		}
		return prefix;
	}

	private String[] constructAddresses(SymbolTableEntry[] ids,String prefix) {
		String[] result = new String[ids.length];

		IntStream.range(0,ids.length)
				.forEach((i) -> {
					if(ids[i].isConstant()){
						VariableEntry temp = create(ids[i].getType());
						generate("move", ids[i].getName(),temp);
						result[i] = prefix +  Integer.toString(abs(temp.getAddress()));
					}
					else if (ids[i].isArray()) {
						result[i] = prefix + Integer.toString(abs(((ArrayEntry)ids[i]).getAddress()));

					}
					else {
						result[i] = prefix + Integer.toString(abs(((VariableEntry)ids[i]).getAddress()));
					}
		});

		return result;
	}


	private void generate (String tviCode, SymbolTableEntry operand1,
						   SymbolTableEntry operand2, SymbolTableEntry operand3) {
		String[] adr = constructAddresses(new SymbolTableEntry[]{operand1, operand2, operand3},generatePrefix());
		String[] quad = {tviCode, adr[0],adr[1],adr[2]};
		quads.addQuad(quad);
	}

	private void generate (String tviCode, SymbolTableEntry operand1,
						   String operand2, SymbolTableEntry operand3) {
		String prefix = generatePrefix();
		String[] adr = constructAddresses(new SymbolTableEntry[]{operand1, operand3},prefix);

		String[] quad = {tviCode, adr[0],prefix + operand2,adr[1]};
		quads.addQuad(quad);
	}

	private void generate (String tviCode, SymbolTableEntry operand1,
						   SymbolTableEntry operand2, String operand3) {
		String prefix = generatePrefix();
		String[] adr = constructAddresses(new SymbolTableEntry[]{operand1, operand2},prefix);

		String[] quad = {tviCode, adr[0],adr[1],prefix + operand3};
		quads.addQuad(quad);
	}

	private void generate (String tviCode, SymbolTableEntry operand1, String operand2) {
		String prefix = generatePrefix();
		String[] adr = constructAddresses(new SymbolTableEntry[]{operand1},prefix);
		String[] quad = {tviCode, adr[0],prefix + operand2, null};
		quads.addQuad(quad);
	}

	private void generate (String tviCode, SymbolTableEntry operand1, SymbolTableEntry operand2) {
		String prefix = generatePrefix();
		String[] adr = constructAddresses(new SymbolTableEntry[]{operand1, operand2},prefix);
		String[] quad = {tviCode, adr[0],adr[1], null};
		quads.addQuad(quad);
	}

	private void generate (String tviCode, String operand1, SymbolTableEntry operand2) {
		String prefix = generatePrefix();
		String[] adr = constructAddresses(new SymbolTableEntry[]{operand2},prefix);
		String[] quad = {tviCode, prefix + operand1,adr[0], null};
		quads.addQuad(quad);
	}

	private void generate (String tviCode, String operand1, String operand2) {
		String prefix = generatePrefix();
		String[] quad = {tviCode,  prefix + operand1, prefix + operand2, null};
		quads.addQuad(quad);
	}

	private void generate (String tviCode, String operand1) {
		String prefix = generatePrefix();
		String[] quad = {tviCode,  prefix + operand1, null, null};
		quads.addQuad(quad);
	}

	private void generate (String tviCode) {
		String[] quad = {tviCode, null, null, null};
		quads.addQuad(quad);
	}

}