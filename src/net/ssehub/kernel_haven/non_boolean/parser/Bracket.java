package net.ssehub.kernel_haven.non_boolean.parser;

/**
 * A bracket token.
 *
 * @author Adam
 */
class Bracket extends CppToken {

    private boolean closing;
    
    /**
     * Creates a bracket token.
     * 
     * @param pos The position inside the expression where this tokens starts.
     * @param closing Whether this is a closing (<code>false</code>) or opening (<code>true</code>) bracket.
     */
    public Bracket(int pos, boolean closing) {
        super(pos);
        this.closing = closing;
    }
    
    /**
     * Returns whether this is a closing bracket.
     * 
     * @return Whether this is a closing bracket.
     */
    public boolean isClosing() {
        return closing;
    }
    
    /**
     * Returns whether this is a opening bracket.
     * 
     * @return Whether this is a opening bracket.
     */
    public boolean isOpening() {
        return !closing;
    }
    
    @Override
    public int getLength() {
        return 1;
    }
    
    @Override
    public String toString() {
        return "Bracket('" + (closing ? ')' : '(') + "')";
    }
    
}
