package semanticActions;

import errors.*;
import lex.TokenType;
import org.junit.After;
import org.junit.Test;
import parser.Parser;
import symbolTable.ArrayEntry;
import symbolTable.ProcedureEntry;
import symbolTable.SymbolTableEntry;
import symbolTable.VariableEntry;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 */
public class SemanticActionsTest3
{
    private static final File RESOURCE = new File("src/test/resources/phase3");

    protected Parser parser;

    @After
    public void cleanup()
    {
        parser = null;
    }

    protected void init(String filename) throws IOException, LexicalError, SyntaxError, SemanticError, SymbolTableError {
        init(new File(RESOURCE, filename));
    }

    protected void init(File file) throws IOException, LexicalError, SemanticError, SyntaxError, SymbolTableError {
        assertTrue("File not found:" + file.getPath(), file.exists());
        parser = new Parser(file);
        parser.parse();
        parser.printTVI();
    }

    @Test
    public void inspectionTest() throws LexicalError, SemanticError, SymbolTableError, SyntaxError, IOException {

        init("phase3-8.pas");
    }

    @Test
    public void invalidRelOpsTest() throws IOException, LexicalError, SyntaxError {
//        System.out.println("SemanticActionsTest2.expressionTest");
        expectException("phase3-5.pas", CompilerError.Type.ILLEGAL_ETYPES);
    }
//
    @Test
    public void invalidArithOpsTest() throws IOException, LexicalError, SyntaxError {
        System.out.println("SemanticActionsTest2.expressionTest");
        expectException("phase3-6.pas", CompilerError.Type.ILLEGAL_ETYPES);
    }
//
//    @Test
//    public void arrayIndicesTest() throws IOException, LexicalError, SyntaxError {
//        System.out.println("SemanticActionsTest2.expressionTest");
//        expectException("phase2-7.pas", CompilerError.Type.INVALID_SUBSCRIPT);
//    }

    private void expectException(String path, CompilerError.Type expected) throws LexicalError, IOException, SyntaxError
    {
        try
        {
            init(path);
            fail("Expected exception not thrown: " + expected);
        }
        catch (SemanticError e)
        {
            assertEquals("Wrong exception type thrown", expected, e.getType());
        } catch (SymbolTableError symbolTableError) {
            symbolTableError.printStackTrace();
        }
    }

    private void checkBuiltIn(String name) throws SemanticError
    {
        SymbolTableEntry entry = parser.lookup(name);
        assertNotNull(entry);
        assertEquals("Wrong entry returned", name.toUpperCase(), entry.getName());
        assertTrue(name + " is not a prodedure", entry.isProcedure());
        assertTrue(name + " is not a reserved", entry.isReserved());
        checkIsA(entry, 2);

        assertTrue("Not a ProcedureEntry", entry instanceof ProcedureEntry);
    }

    private void checkArrayEntry(String name, TokenType type, int address, int lower, int upper) throws SemanticError
    {
        SymbolTableEntry entry = parser.lookup(name);
        assertNotNull(entry);
        assertEquals("Wrong entry returned", name.toUpperCase(), entry.getName());
        assertTrue("Entry is not an array.", entry.isArray());

        checkIsA(entry);
        ArrayEntry array = (ArrayEntry) entry;
        assertEquals("Wrong address assigned.", address, array.getAddress());
        checkType(type, entry.getType());
        assertEquals("Lower bound is wrong", lower, array.getLBound());
        assertEquals("Upper bound is wrong", upper, array.getUBound());
    }

    private void checkVariable(String name, TokenType type, int address) throws SemanticError
    {
        SymbolTableEntry entry = parser.lookup(name);
        assertNotNull(entry);
        assertEquals("Wrong entry returned", name.toUpperCase(), entry.getName());
        assertTrue(entry.isVariable());
        checkIsA(entry);
        VariableEntry ve = (VariableEntry) entry;
        assertEquals("Wrong address assigned.", address, ve.getAddress());
        checkType(type, entry.getType());
    }

    private void print(SymbolTableEntry e)
    {
        System.out.println(e.getName() + ": " + e.getType());
    }

    private void checkType(TokenType expected, TokenType actual)
    {
        assertNotNull(actual);
        String message = String.format("Invalid type. Expected: %s Actual: %s", expected, actual);
        assertEquals(message, expected, actual);
    }

    private void checkIsA(SymbolTableEntry e)
    {
        checkIsA(e, 1);
    }

    private void checkIsA(SymbolTableEntry e, int expected)
    {
        int count = 0;

        if (e.isArray()) ++count;
        if (e.isConstant()) ++count;
        if (e.isFunction()) ++count;
        if (e.isFunctionResult()) ++count;
        if (e.isKeyword()) ++count;
        if (e.isParameter()) ++count;
        if (e.isProcedure()) ++count;
        if (e.isReserved()) ++count;
        if (e.isVariable()) ++count;

        assertEquals("Invalid symbol table entry for " + e.getName(), expected, count);
    }




}
