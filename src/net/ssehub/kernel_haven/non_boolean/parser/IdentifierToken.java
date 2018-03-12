package net.ssehub.kernel_haven.non_boolean.parser;

/**
 * An identifier token.
 *
 * @author Adam
 */
class IdentifierToken extends CppToken {

    private String name;
    
    /**
     * Creates a new identifier token.
     * 
     * @param pos The position inside the expression where this tokens starts.
     * @param name The name of the identifier.
     */
    public IdentifierToken(int pos, String name) {
        super(pos);
        this.name = name;
    }
    
    /**
     * Returns the name of this identifier.
     * 
     * @return The name of this identifier.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Overrides the name of this identifier.
     * 
     * @param name The new name of this identifier.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return "Identifier('" + name + "')";
    }

    @Override
    public int getLength() {
        return name.length();
    }

}
