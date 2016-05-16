package semanticActions;

import grammar.GrammarSymbol;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import symbolTable.SymbolTableEntry;

public class Quadruples {


    private Vector<String[]> Quadruple;
    private int nextQuad;

    public Quadruples() {
        Quadruple = new Vector<String[]>();
        nextQuad = 0;
        String[] dummy_quadruple = new String [4];
        dummy_quadruple[0]=dummy_quadruple[1]=dummy_quadruple[2]=dummy_quadruple[3]= null;
        Quadruple.add(nextQuad,dummy_quadruple);
        nextQuad++;
    }

    public String getField(int quadIndex, int field) {
        return Quadruple.elementAt(quadIndex)[field];
    }

    public void setField(int quadIndex, int index, String field) {
        Quadruple.elementAt(quadIndex)[index] = field;
    }

    public int getNextQuad() {
        return nextQuad;
    }

    public void incrementNextQuad() {
        nextQuad++;
    }

    public String[] getQuad(int index) {
        return (String []) Quadruple.elementAt(index);
    }

    public void setTargetQuad(int quadIndex, String field) {
        String[] targetQuad = Quadruple.get(quadIndex);
        for(int i = 0;i < targetQuad.length; i++) {
            if(targetQuad[i] == "_") {
                targetQuad[i] = field;
            }
        }
    }
    public void addQuad(String[] quad) {
        Quadruple.add(nextQuad, quad);
        nextQuad++;
    }

    public void print() {
        int quadLabel = 1;
        String separator;

        System.out.println("CODE");
        Enumeration<String[]> e = this.Quadruple.elements() ;
        e.nextElement();

        while ( e.hasMoreElements() ) {
            String[] quad = e.nextElement();
            separator = " ";
            System.out.print(quadLabel + ":  " + quad[0]);
            if (quad[1] != null) {
                System.out.print(separator + quad[1]);
            }
            if (quad[2] != null) {
                separator = ", ";
                System.out.print(separator + quad[2]);
            }
            if (quad[3] != null)
                System.out.print(separator + quad[3]);

            System.out.println();
            quadLabel++;
        }
    }

}
