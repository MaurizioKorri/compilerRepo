public class NumberTok extends Token {
	// ... completare ... - fatto.
  String lexeme = " ";

  public NumberTok(int tag, String s){
    super(tag);
    lexeme = s;
    //non posso fare int lexeme?
  }

  public String toString() { return "<" + tag + ", " + lexeme + ">"; }

  //NO - public static final NumberTok zero = new NumberTok(Tag.NUM, "0");
}
