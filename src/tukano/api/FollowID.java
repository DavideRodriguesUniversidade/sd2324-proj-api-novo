package tukano.api;

import java.io.Serializable;

public class FollowID implements Serializable {

    private String followerId;
    private String followedId;

    public FollowID() {}

    public FollowID(String followerId, String followedId) {
        this.followerId = followerId;
        this.followedId = followedId;
    }
}
