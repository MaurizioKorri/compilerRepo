import java.io.*;

public class Translator{
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;

    SymbolTable st = new SymbolTable();         //struttura dati degli identificatori (composti da nome e indirizzo)
    CodeGenerator code = new CodeGenerator();   //la nostra lista di istruzioni (e di label)
    int count=0;                                //viene utilizzato per garantire che ogni id e' associato con un indirizzo diverso

    public Translator(Lexer l, BufferedReader br){
        lex = l;
        pbr = br;
        move();
    }

    void move(){
        look = lex.lexical_scan(pbr);
        System.out.println("token = " + look);
    }

    void error(String s){
        throw new Error("near line " + lex.line + ": " + s);
    }

    void match(int t){
        if (look.tag == t){
          if (look.tag != Tag.EOF) move();
        }
        else error("syntax error");
    }

    public void prog(){
        switch(look.tag){
            case '(':
                int lnext_prog = code.newLabel(); //e' la label che sara' stampata alla fine del codice, viene passata tra ogni metodo successivo
                stat(lnext_prog);
                code.emitLabel(lnext_prog);  //la stampo alla fine
                match(Tag.EOF);
                try{
        	          code.toJasmin();
                }
                catch(java.io.IOException e){
        	          System.out.println("IO error\n");
                };
                break;

            default:
                error("Error in grammar (prog)");
        }
    }

    private void statlist(int lnext_statlist){ //lnext_statlist e' la label che viene ereditata da statp, (statp a sua volta eredita questa label da stat(stat eredita questa label da prog))
        switch(look.tag){
            case '(':
                int lnext_stat = code.newLabel(); //e' la label che sara stampata alla fine di stat, per questo gliela passo come paramentro
                stat(lnext_stat);
                code.emitLabel(lnext_stat);   //stampo la label di stat dopo la l'esecuzione di stat
                statlistp(lnext_statlist);    //la label lnext_statlist viene passata a sua volta a statlistp(e' la label iniziale creata in prog)
                break;

            default:
                error("Error in grammar (statlist)");
        }
    /*statlist e' una lista di stat, e dopo ogni stat noi stampiamo una label */
    }

    private void statlistp(int lnext_statlistp){
        switch(look.tag){
            case '(':
                int lnext_stat = code.newLabel(); //e' la label che verra stampata alla fine di stat
                stat(lnext_stat);
                code.emitLabel(lnext_stat);   //stampo la label di stat, che mi delimita la fine di stat
                statlistp(lnext_statlistp);   //continuo a passare la label iniziale (ereditata da tutti i metodi precedenti, il primo (padre) e' prog)
                break;

            case ')':
                break;

            default:
                error("Error in grammar (statlistp)");
        }
    }

    private void stat(int lnext_stat){   //lnext_stat e' la label iniziale ereditata da prog
        switch(look.tag){
            case '(':
                match(Token.lpt.tag);
                statp(lnext_stat);    //continuo a passare la label iniziale
                match(Token.rpt.tag);
                break;

            default:
                error("Error in grammar (stat)");
        }
    }

    private void statp(int lnext_statp){
        switch(look.tag){
            case '=':
                match(Token.assign.tag);
                if(look.tag==Tag.ID){ //se abbiamo un id,
                    int assign_id_addr = st.lookupAddress(((Word)look).lexeme);  //guardiamo se esiste gia l'identificatore
                    if (assign_id_addr==-1){     //cioe' se non esiste questo identificatore
                        assign_id_addr = count;
                        st.insert(((Word)look).lexeme,count++);   //creiamo un identificatore nuovo
                    }
                    match(Tag.ID);
                    expr(0);
                    code.emit(OpCode.istore, assign_id_addr);
                }
                else error("Error in grammar (statp)");
                break;

            case Tag.COND:        //cond <bexpr> <stat> <elseopt>
                match(Tag.COND);
                int bexpr_true = code.newLabel(); //creo 2 label, 1 per true 1 per false
                int bexpr_false = code.newLabel();
                bexpr(bexpr_true, bexpr_false);
                /* BEXPRP EMETTE QUESTE 2 ISTRUZIONI
                code.emit(OpCode.if_icmpeq, bexprp_true);
                code.emit(OpCode.GOto, bexprp_false);
                */
                //caso true
                code.emitLabel(bexpr_true);         //stampo la label1(vero)
                stat(lnext_statp);    //continuo a ereditare la label inizale
                code.emit(OpCode.GOto, lnext_statp);  //finisce stat e quindi vado alla fine
                //caso false
                code.emitLabel(bexpr_false);  //stampo la label2 (falso)
                elseopt(lnext_statp);   //nel caso in cui sia false continuo a ereditare la label iniziale
                break;
                /*
                ESEMPIO DI CODICE
                IFICMPEQ VAI A LABEL TRUE       //EMESSA DA BEXPRP
                GOTO LABEL FALSE                //EMESSA DA BEXPRP
    LABEL TRUE: ISTRUZIONI DI STAT
                GOTO LABEL FINALE
   LABEL FALSE: FAI ISTRUZIONI DI ELSEOPT
                */
            case Tag.WHILE:
                match(Tag.WHILE);
                int startWhile = code.newLabel();   //creo label di inizio while
                int wtrue = code.newLabel();        //label che passo a bexpr,
                int wfalse = lnext_statp;           //se falso finisce tutto e vado a lnext_statp

                code.emitLabel(startWhile);  //stampo la label di inizio while
                bexpr(wtrue, wfalse); //se true stampa go to wtrue, se false stampa goto lnext_stap

                code.emitLabel(wtrue);    //questo blocco avviene se sono entrato nel while, stampo la label del true
                stat(startWhile);     //faccio le operazioni,
                code.emit(OpCode.GOto, startWhile);   //torno all'inizio e ripeto l'operazione
                break;
                //se false ho finito, e quindi vado a lnext_statp
                /*
   START WHILE: IFICMPEQ BAI A LABEL TRUE
                GOTO LABEL FALSE
      LABEL TRUE: ISTRUZIONI DI STAT
                GOTO STARTWHILE
                */

            case Tag.DO:
                match(Tag.DO);
                int lnext_statlist = lnext_statp; //continuo a ereditare la label iniziale
                statlist(lnext_statlist);
                break;

            case Tag.PRINT:
                match(Tag.PRINT);
                int lnext_exprlist = Tag.PRINT;
                exprlist(lnext_exprlist);   //passo TAG.PRINT per far capire a exprlist che voglio printare
                break;

            case Tag.READ:
                match(Tag.READ);
                if (look.tag==Tag.ID){
                    int read_id_addr = st.lookupAddress(((Word)look).lexeme);
                    if (read_id_addr==-1){
                        read_id_addr = count;
                        st.insert(((Word)look).lexeme,count++);
                    }
                    match(Tag.ID);
                    code.emit(OpCode.invokestatic,0);
                    code.emit(OpCode.istore,read_id_addr);
                }
                else error("Error in grammar (statp)");
                break;

            default:
                error("Error in grammar (statp)");
        }
    }

    private void elseopt(int lnext_elseopt){
        switch(look.tag){
            case '(':
                match(Token.lpt.tag);
                match(Tag.ELSE);
                stat(lnext_elseopt);    //continuiamo a passare la label inizale
                match(Token.rpt.tag);
                break;

            case ')':
                break;

            default:
                error("Error in grammar (elseopt)");
        }
    }

    private void bexpr(int bexpr_true, int bexpr_false){
        switch(look.tag){
            case '(':
                match(Token.lpt.tag);
                bexprp(bexpr_true, bexpr_false);
                match(Token.rpt.tag);
                break;

            default:
                error("Error in grammar (bexpr)");
        }
    }

    private void bexprp(int bexprp_true, int bexprp_false){
        if(look.tag == Tag.RELOP){
            switch(((Word)look).lexeme) {
                case "==":
                    match(Tag.RELOP);
                    expr(0);
                    expr(0);
                    code.emit(OpCode.if_icmpeq, bexprp_true);
                    code.emit(OpCode.GOto,bexprp_false);
                    break;

                case "<=":
                    match(Tag.RELOP);
                    expr(0);
                    expr(0);
                    code.emit(OpCode.if_icmple, bexprp_true);
                    code.emit(OpCode.GOto, bexprp_false);
                    break;

                case "<":
                    match(Tag.RELOP);
                    expr(0);
                    expr(0);
                    code.emit(OpCode.if_icmplt, bexprp_true);
                    code.emit(OpCode.GOto, bexprp_false);
                    break;

                case ">=":
                    match(Tag.RELOP);
                    expr(0);
                    expr(0);
                    code.emit(OpCode.if_icmpge, bexprp_true);
                    code.emit(OpCode.GOto, bexprp_false);
                    break;

                case ">":
                    match(Tag.RELOP);
                    expr(0);
                    expr(0);
                    code.emit(OpCode.if_icmpgt, bexprp_true);
                    code.emit(OpCode.GOto, bexprp_false);
                    break;

                case "<>":
                    match(Tag.RELOP);
                    expr(0);
                    expr(0);
                    code.emit(OpCode.if_icmpne, bexprp_true);
                    code.emit(OpCode.GOto, bexprp_false);
                    break;

                default:
                    error("Error , wrong relational operand");
            }
      }
      else{
          error("Error in bexprp");
      }
    }

    private void expr(int expr_val){
      //expr_val mi dice se voglio stampare o no, se e' uguale a Tag.PRINT stampo, se e' 0 (o altro) non faccio niente
        switch(look.tag){
            case Tag.NUM:
                code.emit(OpCode.ldc, Integer.parseInt(((NumberTok) look).lexeme));
                match(Tag.NUM);
                if(expr_val == Tag.PRINT) code.emit(OpCode.invokestatic, 1);
                break;

            case Tag.ID:
                int id_addr = st.lookupAddress(((Word)look).lexeme);
                if (id_addr==-1){
                    id_addr = count;
                    st.insert(((Word)look).lexeme,count++);
                }
                code.emit(OpCode.iload, id_addr);
                match(Tag.ID);
                if(expr_val == Tag.PRINT) code.emit(OpCode.invokestatic, 1);
                break;

            case '(':
                match(Token.lpt.tag);
                exprp();
                match(Token.rpt.tag);
                if(expr_val == Tag.PRINT) code.emit(OpCode.invokestatic,1);
                break;
        }
    }

    private void exprp(){
        switch(look.tag){
            case '+':
                match(Token.plus.tag);
                int optypeplus = '+';
                exprlist(optypeplus);
                break;

            case '*':
                match(Token.mult.tag);
                int optypetimes = '*';
                exprlist(optypetimes);
                break;

            case '-':
                match(Token.minus.tag);
                expr(0);
                expr(0);
                code.emit(OpCode.isub);
                break;

            case '/':
                match(Token.div.tag);
                expr(0);
                expr(0);
                code.emit(OpCode.idiv);
                break;

            default:
                error("Error in grammar (exprp)");
        }
    }

    public void exprlist(int op_type){
        switch(look.tag){
            case Tag.NUM:
            case Tag.ID:
            case '(':
                expr(op_type);
                exprlistp(op_type);
                break;

            default:
                error("Error in grammar (exprlist)");
        }
    }

    private void exprlistp(int optype){
        switch(look.tag){
            case Tag.NUM:
            case Tag.ID:
            case '(':
                expr(optype);
                if(optype == '+') code.emit(OpCode.iadd);
                else if(optype == '*') code.emit(OpCode.imul);
                exprlistp(optype);
                break;

            case ')':
                break;

            default:
                error("Error in grammar (exprlistp)");
        }

    }

    public static void main(String[] args){
        Lexer lex = new Lexer();
        String path = "/home/maurizio/Desktop/PROGETTO LFT/5.Traduttore/testTrans.pas";
        try{
          BufferedReader br = new BufferedReader(new FileReader(path));
          Translator translator = new Translator(lex, br);
          translator.prog();
          br.close();
        } catch(IOException e){
          e.printStackTrace();
        }
    }
}
