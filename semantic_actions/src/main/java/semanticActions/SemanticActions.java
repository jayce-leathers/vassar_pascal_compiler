package semanticActions;

import static errors.SemanticError.*;
import static java.lang.Math.abs;
import static lex.Token.OperatorType.*;
import static semanticActions.SemanticActions.ETYPE.*;
import static semanticActions.SemanticActions.ETYPE.RELATIONAL;

import com.sun.org.apache.regexp.internal.RE;
import errors.CompilerError;
import errors.SemanticError;

import errors.SymbolTableError;
import lex.Token;
import lex.Token.OperatorType;
import lex.TokenType;
import lex.Tokenizer;
import symbolTable.*;

import java.util.LinkedList;
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
	private SymbolTableEntry currentFunction;
	private LinkedList<Integer> ETrue;
	private LinkedList<Integer> EFalse;
	private LinkedList<Integer> SKIP_ELSE;
	private int beginLoop;
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
			case 22:
				ETYPE etype = (ETYPE) semanticStack.pop();
				if(etype != RELATIONAL) {
					throw ETypeMismatch(tokenizer.getLineNumber());
				}
				backPatch(ETrue,Integer.toString(quads.getNextQuad()));
				break;
			case 24:
				beginLoop = quads.getNextQuad();
				semanticStack.push(beginLoop);
				break;
			case 25:
				etype = (ETYPE) semanticStack.pop();
				if(etype != RELATIONAL) {
					throw ETypeMismatch(tokenizer.getLineNumber());
				}
				backPatch(ETrue, Integer.toString(quads.getNextQuad()));
				break;
			case 26:
				LinkedList Efalse1 = (LinkedList) semanticStack.pop();
				semanticStack.pop();//pop etrue
				int beginLoop1 = (int) semanticStack.pop();
				generate("goto", Integer.toString(beginLoop1));
				backPatch(Efalse1, Integer.toString(quads.getNextQuad()));
				break;
			case 27:
				SKIP_ELSE = makeList(quads.getNextQuad());
				semanticStack.push(SKIP_ELSE);
				generate("goto","_");
				backPatch(EFalse, Integer.toString(quads.getNextQuad()));
				break;
			case 28:
				LinkedList SKIP_ELSE1 = (LinkedList) semanticStack.pop();
				backPatch(SKIP_ELSE1, Integer.toString(quads.getNextQuad()));
				semanticStack.pop();
				semanticStack.pop();
				break;
			case 29:
				LinkedList EFalse1 = (LinkedList) semanticStack.pop();
				semanticStack.pop();
				backPatch(EFalse1,Integer.toString(quads.getNextQuad()));
				break;
			case 30: {
				SymbolTableEntry id = globalTable.lookup(token.getValue());
				if (id == null) {
					throw UndeclaredVariable(token.getValue(), tokenizer.getLineNumber());
				}
				semanticStack.push(id);
				semanticStack.push(ARITHMETIC);
			}
				break;
			case 31: {
				etype = (ETYPE) semanticStack.pop();
				if (etype != ARITHMETIC) {
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
				etype = (ETYPE) semanticStack.pop();
				if(etype != ARITHMETIC) {
					throw ETypeMismatch(tokenizer.getLineNumber());
				}
				SymbolTableEntry id = (SymbolTableEntry) semanticStack.peek();
				if(!id.isArray()) {
					throw NotArray(tokenizer.getLineNumber());
				}
			}
				break;
			case 33: {
				etype = (ETYPE) semanticStack.pop();
				if (etype != ARITHMETIC) {
					throw ETypeMismatch(tokenizer.getLineNumber());
				}
				SymbolTableEntry id = (SymbolTableEntry) semanticStack.pop();
				if(id.getType() != TokenType.INTEGER) {
					throw InvalidSubscript(tokenizer.getLineNumber());
				}
				VariableEntry temp = create(TokenType.INTEGER);
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
			case 38:
				etype = (ETYPE) semanticStack.pop();
				if(etype != ARITHMETIC) {
					throw ETypeMismatch(tokenizer.getLineNumber());
				}
				semanticStack.push(token);
				break;
			case 39:
				etype = (ETYPE) semanticStack.pop();
				if (etype != ARITHMETIC){
					throw ETypeMismatch(tokenizer.getLineNumber());
				}
				SymbolTableEntry id2 = (SymbolTableEntry) semanticStack.pop();
				Token operator = (Token) semanticStack.pop();
				SymbolTableEntry id1 = (SymbolTableEntry) semanticStack.pop();
				int typeCheck = typeCheck(id1,id2);
				String tviCode;

				switch (operator.getOpType()) {
					case LESSTHAN:
						tviCode = "blt";
						break;
					case  LESSTHANOREQUAL:
						tviCode = "ble";
						break;
					case GREATERTHAN:
						tviCode = "bgt";
						break;
					case GREATERTHANOREQUAL:
						tviCode = "bge";
						break;
					case EQUAL:
						tviCode = "beq";
						break;
					case NOTEQUAL:
						tviCode = "bne";
						break;
					default:
						tviCode = "ERROR";
						break;
				}
				VariableEntry temp;
				switch (typeCheck) {
					case 0:
					case 1:
						generate(tviCode, id1,id2,"_");
						break;
					case 2:
						temp = create(TokenType.REAL);
						generate("ltof", id2, temp);
						generate(tviCode,id1,temp,"_");
						break;
					case 3:
						temp = create(TokenType.REAL);
						generate("ltof", id1, temp);
						generate(tviCode,temp,id2,"_");
						break;
				}
				generate("goto","_");
				int nextQuad = quads.getNextQuad();
				ETrue = makeList(nextQuad - 2);
				EFalse = makeList(nextQuad - 1);
				semanticStack.push(ETrue);
				semanticStack.push(EFalse);
				semanticStack.push(RELATIONAL);
				break;
			case 40://uminus uplus
				semanticStack.push(token);
				break;
			case 41: //uminus uplus
				etype = (ETYPE) semanticStack.pop();
				if (etype != ARITHMETIC) {
					throw ETypeMismatch(tokenizer.getLineNumber());
				}
				SymbolTableEntry id = (SymbolTableEntry) semanticStack.pop();
				Token sign = (Token) semanticStack.pop();
				if (sign.getType() == TokenType.UNARYMINUS) {
					temp = create(id.getType());
					generate("uminus", id, temp);
					semanticStack.push(temp);
				} else {
					semanticStack.push(id);
				}
				semanticStack.push(ARITHMETIC);
			break;
			case 42:
				etype = (ETYPE) semanticStack.pop();
				if(token.getOpType() == OR) {
					if (etype != RELATIONAL)	{
						throw ETypeMismatch(tokenizer.getLineNumber());
					}
					backPatch(EFalse, Integer.toString(quads.getNextQuad()));
				} else {
					if(etype != ARITHMETIC) {
						throw ETypeMismatch(tokenizer.getLineNumber());
					}
				}
				semanticStack.push(token);
				break;
			case 43: {
				etype = (ETYPE) semanticStack.pop();
				if (etype == RELATIONAL) {
					LinkedList EFalse2 = (LinkedList) semanticStack.pop();
					LinkedList ETrue2 = (LinkedList) semanticStack.pop();
					operator = (Token) semanticStack.pop();
					EFalse1 = (LinkedList) semanticStack.pop();
					LinkedList ETrue1 = (LinkedList) semanticStack.pop();
//					semanticStack.pop(); //Pop etype?
					ETrue = merge(ETrue1,ETrue2);
					EFalse = EFalse2;
					semanticStack.push(ETrue);
					semanticStack.push(EFalse);
					semanticStack.push(RELATIONAL);
				} else {
					//TODO:check order of pops
					id2 = (SymbolTableEntry) semanticStack.pop();
					operator = (Token) semanticStack.pop();
					id1 = (SymbolTableEntry) semanticStack.pop();
					if (etype != ARITHMETIC) {
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
					VariableEntry temp2;
					if (typeCheckResult == 0) {
						temp = create(TokenType.INTEGER);
						generate(tvicode, id1, id2, temp);
						semanticStack.push(temp);
					} else if (typeCheckResult == 1) {
						temp = create(TokenType.REAL);
						generate("f" + tvicode, id1, id2, temp);
						semanticStack.push(temp);
					} else if (typeCheckResult == 2) {
						temp = create(TokenType.REAL);
						generate("ltof", id2, temp);
						temp2 = create(TokenType.REAL);
						generate("f" + tvicode, id1, temp, temp2);
						semanticStack.push(temp2);
					} else {
						temp = create(TokenType.REAL);
						generate("ltof", id1, temp);
						temp2 = create(TokenType.REAL);
						generate("f" + tvicode, id2, temp, temp2);
						semanticStack.push(temp2);
					}
					semanticStack.push(ARITHMETIC);
				}
			}
				break;
			case 44:
				//TODO:RELATIONAL
				etype = (ETYPE) semanticStack.pop();
				if (etype == RELATIONAL) {
					if(token.getOpType() == AND) {
						backPatch(ETrue, Integer.toString(quads.getNextQuad()));
					}
				}
				semanticStack.push(token);
				break;
			case 45: {
				etype = (ETYPE) semanticStack.pop();


				if (etype == RELATIONAL) {
					LinkedList EFalse2 = (LinkedList) semanticStack.pop();
					LinkedList ETrue2 = (LinkedList) semanticStack.pop();
					operator = (Token) semanticStack.pop();
					if (operator.getOpType() == AND) {
						EFalse1 = (LinkedList) semanticStack.pop();
						LinkedList ETrue1 = (LinkedList) semanticStack.pop();
						ETrue = ETrue2;
						EFalse = merge(EFalse1, EFalse2);
						semanticStack.push(ETrue);
						semanticStack.push(EFalse);
						semanticStack.push(RELATIONAL);

					}
					else {
						throw ETypeMismatch(tokenizer.getLineNumber());
					}
				} else {
					id1 = (SymbolTableEntry) semanticStack.pop();
					operator = (Token) semanticStack.pop();
					id2 = (SymbolTableEntry) semanticStack.pop();
					if (etype != ARITHMETIC) {
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
					int typeCheckResult = typeCheck(id1, id2);
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
								generate("ltof", id1, temp1);
								VariableEntry temp2 = create(TokenType.REAL);
								generate("ltof", id2, temp2);
								VariableEntry temp3 = create(TokenType.REAL);
								generate("fdiv", temp1, temp2, temp3);
								semanticStack.push(temp3);
							} else {
								temp = create(TokenType.INTEGER);
								generate(tvicode, id1, id2, temp);
								semanticStack.push(temp);
							}
							break;
						case 1:
							if (operator.getOpType() == INTEGERDIVIDE) {
								VariableEntry temp1 = create(TokenType.INTEGER);
								generate("ftol", id1, temp1);
								VariableEntry temp2 = create(TokenType.INTEGER);
								generate("ftol", id2, temp2);
								VariableEntry temp3 = create(TokenType.INTEGER);
								generate("div", temp1, temp2, temp3);
								semanticStack.push(temp3);
							} else {
								temp = create(TokenType.REAL);
								generate("f" + tvicode, id1, id2, temp);
								semanticStack.push(temp);
							}
							break;
						case 2:
							if (operator.getOpType() == INTEGERDIVIDE) {
								VariableEntry temp1 = create(TokenType.INTEGER);
								generate("ftol", id1, temp1);
								VariableEntry temp2 = create(TokenType.INTEGER);
								generate("div", temp1, id2, temp2);
								semanticStack.push(temp2);
							} else {
								VariableEntry temp1 = create(TokenType.REAL);
								generate("ltof", id2, temp1);
								VariableEntry temp2 = create(TokenType.REAL);
								generate("f" + tvicode, id1, temp1, temp2);
								semanticStack.push(temp2);
							}
							break;
						case 3:
							if (operator.getOpType() == INTEGERDIVIDE) {
								VariableEntry temp1 = create(TokenType.INTEGER);
								generate("ftol", id2, temp1);
								VariableEntry temp2 = create(TokenType.INTEGER);
								generate("div", id1, temp1, temp2);
								semanticStack.push(temp2);
							} else {
								VariableEntry temp1 = create(TokenType.REAL);
								generate("ltof", id1, temp1);
								VariableEntry temp2 = create(TokenType.REAL);
								generate("f" + tvicode, temp1, id2, temp2);
								semanticStack.push(temp2);
							}
							break;
					}
					semanticStack.push(ARITHMETIC);
				}
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
				semanticStack.push(ARITHMETIC);
				break;
			case 47:
				etype = (ETYPE) semanticStack.pop();
				if (etype != RELATIONAL) {
					throw ETypeMismatch(tokenizer.getLineNumber());
				}
				ETrue = (LinkedList<Integer>) semanticStack.pop();
				EFalse = (LinkedList<Integer>) semanticStack.pop();
				semanticStack.push(ETrue);
				semanticStack.push(EFalse);
				semanticStack.push(RELATIONAL);
				break;
			case 48:
				//TODO: VERIFY pop order
				Object offset =  semanticStack.pop();
				if (offset != OFFSET.NULL) {
					id = (SymbolTableEntry) semanticStack.pop();
					//TODO: CHECK for etyp
//					semanticStack.pop();//pop Etype which apparently we don't need?
					temp = create(id.getType());
					generate("load", id, (SymbolTableEntry) offset,temp);
					semanticStack.push(temp);
				}
				semanticStack.push(ARITHMETIC);
				break;
//			case 52:
//				etype = (ETYPE) semanticStack.pop();
//				id = (SymbolTableEntry) semanticStack.pop();
//				if(!id.isFunction()) {
//					throw IllegalProcedureCall(id.getName(),tokenizer.getLineNumber());
//				}
//				if(((FunctionEntry)id).getNumberOfParameters() > 0) {
//					throw WrongNumberParms(id.getName(),tokenizer.getLineNumber());
//				}
//				generate("call",id,"0");
//				SymbolTableEntry temp = create(id.getType());
//				generate("move", (SymbolTableEntry)((FunctionEntry)id).getResult(),temp);//result is function name?
//				semanticStack.push(temp);
//				semanticStack.push(ETYPE.ARITHMETIC);
//				break;
			case 53:
				etype = (ETYPE) semanticStack.pop();
				id = (SymbolTableEntry) semanticStack.pop();
				if(id.isFunction()) {
					if(id != currentFunction) {
						throw IllegalFunctionName(id.getName(),tokenizer.getLineNumber());
					}
					semanticStack.push(((FunctionEntry)id).getResult());//function name
					semanticStack.push(ARITHMETIC);
				} else {
					semanticStack.push(id);
					semanticStack.push(etype);
				}
				break;
			case 54:
				id = (SymbolTableEntry) semanticStack.peek();
				if(!id.isProcedure()) {
					throw IllegalProcedureCall(id.getName(),tokenizer.getLineNumber());
				}
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

	private LinkedList<Integer> makeList(int i) {
		LinkedList result = new LinkedList<>();
		result.add(i);
		return result;
	}

	private LinkedList<Integer> merge(LinkedList a, LinkedList b) {
		LinkedList result = new LinkedList<>();
		result.addAll(a);
		result.addAll(b);
		return result;
	}

	private void backPatch(int quadIndex, String value) {
		quads.setTargetQuad(quadIndex,value);
	}

	private void backPatch(LinkedList<Integer> quadIndices, String value) {
		quadIndices.forEach((i)-> {
					quads.setTargetQuad(i,value);
				});
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

		String[] quad = {tviCode, adr[0],operand2,adr[1]};
		quads.addQuad(quad);
	}

	private void generate (String tviCode, SymbolTableEntry operand1,
						   SymbolTableEntry operand2, String operand3) {
		String prefix = generatePrefix();
		String[] adr = constructAddresses(new SymbolTableEntry[]{operand1, operand2},prefix);

		String[] quad = {tviCode, adr[0],adr[1],operand3};
		quads.addQuad(quad);
	}

	private void generate (String tviCode, SymbolTableEntry operand1, String operand2) {
		String prefix = generatePrefix();
		String[] adr = constructAddresses(new SymbolTableEntry[]{operand1},prefix);
		String[] quad = {tviCode, adr[0],operand2, null};
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
		String[] quad = {tviCode, operand1,adr[0], null};
		quads.addQuad(quad);
	}

	private void generate (String tviCode, String operand1, String operand2) {
		String prefix = generatePrefix();
		String[] quad = {tviCode,  operand1, prefix + operand2, null};
		quads.addQuad(quad);
	}

	private void generate (String tviCode, String operand1) {
		String prefix = generatePrefix();
		String[] quad = {tviCode,  operand1, null, null};
		quads.addQuad(quad);
	}

	private void generate (String tviCode) {
		String[] quad = {tviCode, null, null, null};
		quads.addQuad(quad);
	}

}