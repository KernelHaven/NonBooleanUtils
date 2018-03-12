package net.ssehub.kernel_haven.non_boolean.parser.ast;

/**
 * Abstract superclass for CPP expression AST.
 *
 * @author Adam
 */
public abstract class CppExpression {

    @Override
    public String toString() {
        return toString("");
    }
    
    /**
     * Turns this node into a string.
     * 
     * @param indentation The indentation to prepend at the start.
     * 
     * @return A string representation of this node with <code>indentation</code> at the start.
     */
    protected abstract String toString(String indentation);
    
    /**
     * Accepts the given visitor.
     * 
     * @param visitor The visitor to accept.
     * @param <T> The return type of the visitor.
     * 
     * @return The return value of the visitor.
     */
    public abstract <T> T accept(ICppExressionVisitor<T> visitor);
    
}
