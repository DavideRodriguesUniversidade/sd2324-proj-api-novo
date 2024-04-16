package tukano.api;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

@Entity
public class User {
	
	@Id
	private String userId;
	
	
	private String pwd;
	private String email;
	private String displayName;
	
   // private Set<User> followedUsers = new HashSet<>();


	public User() {}
	
	public User(String userId, String pwd, String email, String displayName) {
		this.pwd = pwd;
		this.email = email;
		this.userId = userId;
		this.displayName = displayName;
	}

	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String userId() {
		return userId;
	}
	
	public String pwd() {
		return pwd;
	}
	
	public String email() {
		return email;
	}
	
	public String displayName() {
		return displayName;
	}
	
	@Override
	public String toString() {
		return "User [userId=" + userId + ", pwd=" + pwd + ", email=" + email + ", displayName=" + displayName + "]";
	}
	
	/*public void addFollowedUser(User user) {
        followedUsers.add(user);
    }

    public void removeFollowedUser(User user) {
        followedUsers.remove(user);
    }

    public Set<User> getFollowedUsers() {
        return followedUsers;
    }*/
	
}
