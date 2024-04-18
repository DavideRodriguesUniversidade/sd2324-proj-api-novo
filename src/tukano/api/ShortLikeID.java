package tukano.api;

import java.io.Serializable;
import java.util.Objects;

public class ShortLikeID implements Serializable {

    private String shortId;
    private String userId;

    // Default constructor
    public ShortLikeID() {}

    // Constructor with fields
    public ShortLikeID(String shortId, String userId) {
        this.shortId = shortId;
        this.userId = userId;
    }

    // Getters and setters
    public String getShortId() {
        return shortId;
    }

    public void setShortId(String shortId) {
        this.shortId = shortId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Equals and hashCode methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShortLikeID likeID = (ShortLikeID) o;
        return Objects.equals(shortId, likeID.shortId) &&
               Objects.equals(userId, likeID.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortId, userId);
    }
}
