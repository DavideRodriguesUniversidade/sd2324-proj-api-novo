package tukano.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
@Entity
@IdClass(VerifierID.class)
public class Verifier {

	@Id
	String shortId;
	@Id
	String  verifier;
	
	public Verifier(){}
	
	public Verifier(String shortId, String verifier){
		this.shortId = shortId;
		this.verifier = verifier;
	}
	
	public String getShortId() {
		return shortId;
	}

	public String getVerifier() {
		return verifier;
	}
}
