import java.util.LinkedList;
import java.io.*;

public class CodeGenerator{

    LinkedList <Instruction> instructions = new LinkedList <Instruction>();

    int label=0;

    public void emit(OpCode opCode){
        instructions.add(new Instruction(opCode));
        //aggiunge una nuova instruzione SENZA OPERANDO sulla pila
        //emit(iadd);
        //add e' un metodo delle LinkedList.

    }

    public void emit(OpCode opCode , int operand) {
        instructions.add(new Instruction(opCode, operand));
        //agginge una nuova istruzione CON OPERANDO sulla pila
        //emit(iaload x);
    }

    public void emitLabel(int operand) {
        emit(OpCode.label, operand);
        //aggiunge una label sulla pila.
    }

    public int newLabel() {
        return label++;
        //crea una nuova label
    }

    public void toJasmin() throws IOException{
        PrintWriter out = new PrintWriter(new FileWriter("Output.j"));
        String temp = "";
        temp = temp + header;
        while(instructions.size() > 0){
            Instruction tmp = instructions.remove();
            temp = temp + tmp.toJasmin();
        }
        temp = temp + footer;
        out.println(temp);
        out.flush();
        out.close();
    }

    private static final String header = ".class public Output \n"
        + ".super java/lang/Object\n"
        + "\n"
        + ".method public <init>()V\n"
        + " aload_0\n"
        + " invokenonvirtual java/lang/Object/<init>()V\n"
        + " return\n"
        + ".end method\n"
        + "\n"
        + ".method public static print(I)V\n"
        + " .limit stack 2\n"
        + " getstatic java/lang/System/out Ljava/io/PrintStream;\n"
        + " iload_0 \n"
        + " invokestatic java/lang/Integer/toString(I)Ljava/lang/String;\n"
        + " invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V\n"
        + " return\n"
        + ".end method\n"
        + "\n"
        + ".method public static read()I\n"
        + " .limit stack 3\n"
        + " new java/util/Scanner\n"
        + " dup\n"
        + " getstatic java/lang/System/in Ljava/io/InputStream;\n"
        + " invokespecial java/util/Scanner/<init>(Ljava/io/InputStream;)V\n"
        + " invokevirtual java/util/Scanner/next()Ljava/lang/String;\n"
        + " invokestatic java/lang/Integer.parseInt(Ljava/lang/String;)I\n"
        + " ireturn\n"
        + ".end method\n"
        + "\n"
        + ".method public static run()V\n"
        + " .limit stack 1024\n"
        + " .limit locals 256\n";

    private static final String footer = " return\n"
        + ".end method\n"
        + "\n"
        + ".method public static main([Ljava/lang/String;)V\n"
        + " invokestatic Output/run()V\n"
        + " return\n"
        + ".end method\n";
}

/*
La classe CodeGenerator ha lo scopo di memorizzare in una lista la lista delle istruzioni,
come oggetti di tipo Instruction.

I metodi emit sono usati per aggiungere istruzioni o etichette di salto nel codice.

Header e' una stringa che definisce l'inizio del codice generato dal traduttore per dare a jasmin
la struttura che vuole del codice

Footer e' una stringa che definisce la fine del codice generato dal traduttore per dare a jasmin
la struttura del codice che vuole.
*/
