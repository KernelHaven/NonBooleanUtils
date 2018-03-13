package net.ssehub.kernel_haven.non_boolean.parser;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;

/**
 * Tests the lexer from {@link CppParser}.
 * 
 * @author Adam
 */
public class CppLexerTest {

    /**
     * Tests parsing a single identifier.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testSingleIdentifier() throws ExpressionFormatException {
        CppParser parser = new CppParser();
        
        assertThat(parser.lex("A"), is(new CppToken[] {new IdentifierToken(0, "A")}));
        assertThat(parser.lex("Longer_Variable_12_Name"),
                is(new CppToken[] {new IdentifierToken(0, "Longer_Variable_12_Name")}));
        assertThat(parser.lex("123"),
                is(new CppToken[] {new IdentifierToken(0, "123")}));
    }
    
    /**
     * Tests parsing brackets.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testBrackets() throws ExpressionFormatException {
        CppParser parser = new CppParser();
        
        assertThat(parser.lex("(A)"), is(new CppToken[] {
                new Bracket(0, false), new IdentifierToken(1, "A"), new Bracket(2, true)}));
        assertThat(parser.lex("()()(())"), is(new CppToken[] {
                new Bracket(0, false), new Bracket(1, true), new Bracket(2, false), new Bracket(3, true),
                new Bracket(4, false), new Bracket(5, false), new Bracket(6, true), new Bracket(7, true)}));
    }
    
    /**
     * Tests parsing single operators.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testSingleOperators() throws ExpressionFormatException {
        CppParser parser = new CppParser();
        
        assertThat(parser.lex("+"), is(new CppToken[] {new OperatorToken(0, CppOperator.INT_ADD)}));
        assertThat(parser.lex("-"), is(new CppToken[] {new OperatorToken(0, CppOperator.INT_SUB)}));
        assertThat(parser.lex("*"), is(new CppToken[] {new OperatorToken(0, CppOperator.INT_MUL)}));
        assertThat(parser.lex("/"), is(new CppToken[] {new OperatorToken(0, CppOperator.INT_DIV)}));
        assertThat(parser.lex("%"), is(new CppToken[] {new OperatorToken(0, CppOperator.INT_MOD)}));
        assertThat(parser.lex("++"), is(new CppToken[] {new OperatorToken(0, CppOperator.INT_INC)}));
        assertThat(parser.lex("--"), is(new CppToken[] {new OperatorToken(0, CppOperator.INT_DEC)}));
        
        assertThat(parser.lex("=="), is(new CppToken[] {new OperatorToken(0, CppOperator.CMP_EQ)}));
        assertThat(parser.lex("!="), is(new CppToken[] {new OperatorToken(0, CppOperator.CMP_NE)}));
        assertThat(parser.lex(">"), is(new CppToken[] {new OperatorToken(0, CppOperator.CMP_GT)}));
        assertThat(parser.lex(">="), is(new CppToken[] {new OperatorToken(0, CppOperator.CMP_GE)}));
        assertThat(parser.lex("<"), is(new CppToken[] {new OperatorToken(0, CppOperator.CMP_LT)}));
        assertThat(parser.lex("<="), is(new CppToken[] {new OperatorToken(0, CppOperator.CMP_LE)}));
        
        assertThat(parser.lex("&&"), is(new CppToken[] {new OperatorToken(0, CppOperator.BOOL_AND)}));
        assertThat(parser.lex("||"), is(new CppToken[] {new OperatorToken(0, CppOperator.BOOL_OR)}));
        assertThat(parser.lex("!"), is(new CppToken[] {new OperatorToken(0, CppOperator.BOOL_NOT)}));
        
        assertThat(parser.lex("&"), is(new CppToken[] {new OperatorToken(0, CppOperator.BIN_AND)}));
        assertThat(parser.lex("|"), is(new CppToken[] {new OperatorToken(0, CppOperator.BIN_OR)}));
        assertThat(parser.lex("^"), is(new CppToken[] {new OperatorToken(0, CppOperator.BIN_XOR)}));
        assertThat(parser.lex("~"), is(new CppToken[] {new OperatorToken(0, CppOperator.BIN_INV)}));
        assertThat(parser.lex(">>"), is(new CppToken[] {new OperatorToken(0, CppOperator.BIN_SHR)}));
        assertThat(parser.lex("<<"), is(new CppToken[] {new OperatorToken(0, CppOperator.BIN_SHL)}));
    }
    
    /**
     * Tests that whitespaces are ignored correctly.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testWhitesapace() throws ExpressionFormatException {
        CppParser parser = new CppParser();
        
        assertThat(parser.lex("A + (VAR_2 * -3)"), is(new CppToken[] {
                new IdentifierToken(0, "A"), new OperatorToken(2, CppOperator.INT_ADD), new Bracket(4, false),
                new IdentifierToken(5, "VAR_2"), new OperatorToken(11, CppOperator.INT_MUL),
                new OperatorToken(13, CppOperator.INT_SUB), new IdentifierToken(14, "3"), new Bracket(15, true)}));
    }
    
    /**
     * Tests that invalid characters correctly throw exceptions.
     * 
     * @throws ExpressionFormatException wanted.
     */
    @Test(expected = ExpressionFormatException.class)
    public void testInvalidCharacter() throws ExpressionFormatException {
        CppParser parser = new CppParser();
        
        parser.lex("A && Ü");
    }
    
}
