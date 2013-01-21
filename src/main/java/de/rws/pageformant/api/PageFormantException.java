package de.rws.pageformant.api;

public class PageFormantException extends Exception {
	private static final long serialVersionUID = 7036443894529521811L;
	int errCode;
	public PageFormantException(String message) {
        super(message);
        this.errCode = -1;
    }
	
	public PageFormantException(String message, int errCode) {
        super(message);
        this.errCode = errCode;
    }
}
