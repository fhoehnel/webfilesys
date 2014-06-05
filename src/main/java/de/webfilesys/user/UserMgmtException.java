package de.webfilesys.user;

public class UserMgmtException extends Exception {

	private static final long serialVersionUID = 1L;

	public UserMgmtException(String message, Throwable t) {
        super(message, t);
    }

    public UserMgmtException(String message) {
        super(message);
    }
    
    public String toString() {
        
    	final StringBuffer buff = new StringBuffer(super.toString());
        if (this.getCause() != null) {
            buff.append("Nested Exception: ");
            buff.append(this.getCause().toString());
        }

        return buff.toString(); 
    }

}
