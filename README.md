# compilerRepo
A small compiler that translates a made up programming language to Java JVM instructions 
then with the help of an assembler called Jasmin executes it.


in the file testTrans.pas we can write our made up programming language, in the file Output.j after 
running the compiler with the command "java -jar jasmin.jar Output.j" we can find the assembly code that was just translated,
The command also creates the file Output.class, which is out java program that we can run with "java Output"


