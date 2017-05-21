package simplenem12;

/**
 * Created by David on 20/05/2017.
 */
public class ParsingException extends Exception {

    public ParsingException(int lineNumber, String message) {
        System.out.println(String.format("Parse Error at line %d: %s",lineNumber, message));
        System.exit(1);
    }
}
