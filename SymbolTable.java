import java.util.*;

public class SymbolTable {

  Map <String, Integer> OffsetMap = new HashMap <String,Integer>();

	public void insert( String s, int address ) {
    //aggiunge un nuovo identificatore
    if(!OffsetMap.containsValue(address)) OffsetMap.put(s,address);
    else
      throw new IllegalArgumentException("Reference to a memory location already occupied by another variable");
	}

	public int lookupAddress ( String s ) {
    //per controllare se c'e' gia un indentificatore o no
    if(OffsetMap.containsKey(s)) return OffsetMap.get(s);
    else return -1;
	}
}

/*
La classe SymbolTable triene traccia degli identificatori.
*/
