package es.ucm.sexp;

import java.io.StreamTokenizer;

/**
 * This is the base S-Expression parser.
 *
 * It understands just a few constructs of the Lisp language, namely QUOTE,
 * QUASIQUOTE and UNQUOTE. In the future it may be desirable to embed a
 * readtable, as Lisp does, in order to customize the behavior of alien
 * characters which may carry meaning on dedicated s-expressions.
 *
 * This class is inspired on former work from Joel F. Klein, which was
 * released under the terms of the GNU GPL and the GNU GFDL.
 * Although, this one was written from scratch
 * <p>
 * See: http://jfkbits.blogspot.com.es/2008/05/thoughts-on-s-expression-parser.html
 *
 * @author Santiago Saavedra López
 */
public class SexpParser {
    SexpTokenizer tokenizer;

    public SexpParser(SexpTokenizer input) {
        tokenizer = input;
    }

    public Expr parseExpr() throws ParseException {
        Token token = tokenizer.next();
        switch (token.type) {
            case '(':
                return parseCons(token);
            case '"':
                return new StringAtom(token.text);
            case '\'':
                return new Cons(new Atom("QUOTE"), parseExpr());
            case '`':
                return new Cons(new Atom("QUASIQUOTE"), parseExpr());
            case ',':
                return new Cons(new Atom("UNQUOTE"), parseExpr());
            default:
                if (token.text.equalsIgnoreCase("NIL"))
                    return null;
                return new Atom(token.text);
        }
    }

    protected Expr parseCons(Token openParen) throws ParseException {
        if (tokenizer.peekToken().type == ')') {
            tokenizer.next();
            return null;
        }
        if (tokenizer.peekToken().type == StreamTokenizer.TT_WORD &&
                tokenizer.peekToken().text.equals(".")) {
            tokenizer.next();
            Expr e = parseExpr();

            if (tokenizer.peekToken().type != ')') {
                throw new ParseException();
            }
            tokenizer.next();

            return e;
        }

        Expr car = parseExpr();
        Expr cdr = parseCons(openParen);
        return new Cons(car, cdr);
    }

    /**
     * This is the datatype for representing any valid s-expression in the
     * system. It only has methods for casting them to their proper
     * subclasses. Please refer to the subclasses for more information.
     *
     * @see Atom
     * @see Cons
     * @see StringAtom
     */
    public interface Expr {
        boolean isAtom();

        Atom getAtom();

        boolean isList();

        Cons getList();

        /**
         * Returns the s-expression-readable representation of the object.
         */
        String toString();
    }

    public class ParseException extends Exception {

    }

}