import java.io.*;
import java.util.*;

public class Lexer {
    public static int line = 1;
    private char peek = ' ';

    private void readch(BufferedReader br){      //legge un singolo carattere da br e lo mette in peek.
        try {
            peek = (char) br.read();    //br.read() legge un singolo carattere da br.
        }
        catch (IOException exc) {
            peek = (char) -1;
        }
    }

    public Token lexical_scan(BufferedReader br){
        while (peek == ' ' || peek == '\t' || peek == '\n'  || peek == '\r') {
            if (peek == '\n') line++;
            readch(br);
        }
        switch (peek){
            case '!':
                peek = ' ';
                return Token.not;

            case '(':
                peek = ' ';
                return Token.lpt;

            case ')':
                peek = ' ';
                return Token.rpt;

            case '{':
                peek = ' ';
                return Token.lpg;

            case '}':
                peek = ' ';
                return Token.rpg;

            case '+':
                peek = ' ';
                return Token.plus;

            case '-':
                peek = ' ';
                return Token.minus;

            case '*':
                peek = ' ';
                return Token.mult;

            case '/':
                readch(br);
                switch(peek){

                    case '/':
                        readch(br);
                        while(peek!=(char)-1 && peek != '\n'){
                            readch(br);
                        }
                        return lexical_scan(br);

                    case '*':
                        boolean comment = true;
                        readch(br);
                        while(comment){
                            if(peek=='*'){
                                readch(br);
                                if(peek=='/'){
                                    comment = false;
                                    peek = ' ';
                                    return lexical_scan(br);
                                }
                            }
                            else if(peek==(char)-1){
                                System.err.print("Commento non chiuso");
                                return null;
                            }
                            else readch(br);
                        }

                    default:
                        return Token.div;
                }

            case ';':
                peek = ' ';
                return Token.semicolon;

            case '&':
                readch(br);
                if (peek == '&'){
                    peek = ' ';
                    return Word.and;
                }
                else {
                    System.err.println("Erroneous character" + " after & : " + peek);
                    return null;
                }

            case '|':
                readch(br);
                if(peek=='|'){
                    peek = ' ';
                    return Word.or;
                }
                else {
                    System.err.println("Erroneous character" + " after | : " + peek);
                    return null;
                }

            case '<':
                readch(br);
                if(peek=='='){
                    peek = ' ';
                    return Word.le;
                }
                else if(peek=='>'){
                    peek = ' ';
                    return Word.ne;
                }
                else return Word.lt;

            case '>':
                readch(br);
                if(peek=='='){
                    peek = ' ';
                    return Word.ge;
                }
                else return Word.gt;

            case '=':
                readch(br);
                if(peek == '='){
                    peek = ' ';
                    return Word.eq;
                }
                else {
                    return Token.assign;
                }

            case (char)-1:
                return new Token(Tag.EOF);

            default:
                if (Character.isLetter(peek)){
                    String s = "";
                    while(Character.isLetter(peek) || Character.isDigit(peek) || peek == '_'){
                        s = s + peek;
                        readch(br);
                    }
                    if(s.equals("cond")) return Word.cond;
                    else if(s.equals("when")) return Word.when;
                    else if(s.equals("then")) return Word.then;
                    else if(s.equals("else")) return Word.elsetok;
                    else if(s.equals("while")) return Word.whiletok;
                    else if(s.equals("do")) return Word.dotok;
                    else if(s.equals("seq")) return Word.seq;
                    else if(s.equals("print")) return Word.print;
                    else if(s.equals("read")) return Word.read;
                    else return new Word(Tag.ID, s);
                }
                else if(Character.isDigit(peek)){
                    String s = "";
                    if(peek=='0'){
                        readch(br);
                        if(Character.isDigit(peek) || Character.isLetter(peek) || peek == '_'){
                            System.err.println("Erroneous character: " + peek );
                            return null;
                        }
                        else{
                            s = "0";
                            return new NumberTok(Tag.NUM, s);
                        }
                    }
                    else{
                        while(Character.isDigit(peek)){
                            s = s + peek;
                            readch(br);
                        }
                    }
                    if(Character.isLetter(peek) || peek == '_'){
                        System.err.println("Not an Identifier");
        							  return null;
                    }
                    else{
                        return new NumberTok(Tag.NUM, s);
                    }
                }
                else if(peek=='_'){
                    String s = "";
                    while(peek=='_'){
                        s = s + peek;
                        readch(br);
                    }
                    if(Character.isDigit(peek) || Character.isLetter(peek)){
        						    while(Character.isDigit(peek) || Character.isLetter(peek)){
        								    s = s+peek;
        								    readch(br);
        							  }
        							  return new Word(Tag.ID, s);
                    }
                    else{
                        System.err.println("Not an Identifier");
                        return null;
                    }
                }
            else{
                System.err.println("Erroneous character: " + peek );
                return null;
            }
        }
    }

    public static void main(String[] args){
        Lexer lex = new Lexer();
        String path = "/home/maurizio/Desktop/PROGETTO LFT/2.Lexer/testodiprova.txt"; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Token tok;
            do{
                tok = lex.lexical_scan(br);
                System.out.println("Scan: " + tok);
              } while (tok.tag != Tag.EOF);
            br.close();
        } catch (IOException e) {e.printStackTrace();}
    }
}
