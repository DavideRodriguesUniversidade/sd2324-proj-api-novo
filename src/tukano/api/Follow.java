package tukano.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Follow {

    @Id
    private String followId; // Combined string of followerId and followedId

    private String followerId;
    private String followedId;
    private boolean isFollowing;

    // Constructors, getters, setters
    public Follow() {}

    public Follow(String followerId, String followedId, boolean isFollowing) {
        this.followerId = followerId;
        this.followedId = followedId;
        this.followId = followerId + "-" + followedId;
        this.isFollowing = isFollowing;
    }

    // Getters and setters
    public String getFollowId() {
        return followId;
    }

    public void setFollowId(String followId) {
        this.followId = followId;
    }

    public String getFollowerId() {
        return followerId;
    }

    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }

    public String getFollowedId() {
        return followedId;
    }

    public void setFollowedId(String followedId) {
        this.followedId = followedId;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }
}
