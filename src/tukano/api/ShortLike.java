package tukano.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

@Entity
@IdClass(ShortLikeID.class)
public class ShortLike {

    @Id
    private String shortId;

    @Id
    private String userId;

    private boolean isLiked;

    // Constructors, getters, setters
    public ShortLike() {}

    public ShortLike(String shortId, String userId, boolean isLiked) {
        this.shortId = shortId;
        this.userId = userId;
        this.isLiked = isLiked;
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

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }
}
