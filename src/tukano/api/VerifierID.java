package tukano.api;

import java.io.Serializable;

public class VerifierID  implements  Serializable{
	
	
	    private String shortId;

	    private String verifier;

	    // default constructor

		public VerifierID(){}
		
	    public VerifierID(String shortId, String verifier) {
	        this.shortId = shortId;
	        this.verifier = verifier;
	    }
}
