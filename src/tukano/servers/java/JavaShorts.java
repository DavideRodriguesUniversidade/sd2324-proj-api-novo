package tukano.servers.java;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.Response;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.Follow;
import tukano.api.Short;
import tukano.api.User;
import tukano.api.factory.UsersClientFactory;
import tukano.api.java.Shorts;
import tukano.api.java.Users;
import tukano.persistence.Hibernate;

public class JavaShorts implements Shorts {

    private static Logger Log = Logger.getLogger(JavaShorts.class.getName());


    @Override
    public Result<Short> createShort(String userId, String password) {
        Log.info("createShort : userId = " + userId);

        try {
            Users usersClient = UsersClientFactory.getClient();
            
            // Retrieve user details via GET request
            Result<User> userResult = usersClient.getUser(userId, password);
            
            if (!userResult.isOK()) {
                Log.info("User retrieval failed or incorrect password.");
                return Result.error(ErrorCode.BAD_REQUEST);
            }

            String shortId = generateUniqueShortId();
            
            String blobUrl = "";

            // Create a new short
            Short newShort = new Short(shortId, userId, blobUrl);

            // Persist the short using Hibernate
            Hibernate hibernate = Hibernate.getInstance();
            hibernate.persistShort(newShort);

            return Result.ok(newShort);
        } catch (Exception ce) {
            Log.severe("Connection refused: " + ce.getMessage());
            ce.printStackTrace();
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }


    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        Log.info("deleteShort : shortId = " + shortId);

        // Check if shortId is valid
        if (shortId == null) {
            Log.info("Short ID null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        // Retrieve the short from the database
        Result<Short> result = getShort(shortId);
        if (!result.isOK()) {
            return Result.error(ErrorCode.NOT_FOUND);
        }

        // Delete the short using Hibernate
        Hibernate hibernate = Hibernate.getInstance();
        hibernate.deleteShort(result.value());

        return Result.ok(null);
    }

    @Override
    public Result<Short> getShort(String shortId) {
        Log.info("getShort : shortId = " + shortId);

        // Check if shortId is valid
        if (shortId == null) {
            Log.info("Short ID null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }
        // Retrieve the short from the database using Hibernate
        Hibernate hibernate = Hibernate.getInstance();
        List<Short> shorts = hibernate.jpql("SELECT s FROM Short s WHERE s.shortId = '" + shortId + "'", Short.class);
        if (shorts.isEmpty()) {
            Log.info("Short does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        return Result.ok(shorts.get(0));
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        Log.info("getShorts : userId = " + userId);

        // Retrieve the shorts for the user from the database using Hibernate
        Hibernate hibernate = Hibernate.getInstance();
        List<Short> userShorts = hibernate.jpql("SELECT s FROM Short s WHERE s.ownerId = '" + userId + "'", Short.class);
        List<String> shortIds = userShorts.stream().map(Short::getShortId).collect(Collectors.toList());

        return Result.ok(shortIds);
    }


    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        Log.info("follow : userId1 = " + userId1 + ", userId2 = " + userId2 + ", isFollowing = " + isFollowing);

        // Check if userId1 and userId2 are valid
        if (userId1 == null || userId2 == null || password == null) {
            Log.info("userId1, userId2 or Password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }
        
        Hibernate hibernate = Hibernate.getInstance();

        // Check if password is correct for userId1
        Result<User> userResult1 = UsersClientFactory.getClient().getUser(userId1, password);
        if (!userResult1.isOK()) {
            Log.info("User retrieval failed or incorrect password for userId1.");
            return Result.error(ErrorCode.FORBIDDEN);
        }
        
        // Check if the follow relationship already exists
        List<Follow> existingFollows = hibernate.jpql("SELECT f FROM Follow f WHERE f.followerId = '" + userId1 + "' AND f.followedId = '" + userId2 + "'", Follow.class);

        if (isFollowing && existingFollows.isEmpty()) {
            // Create a new Follow entity and persist it
            Follow follow = new Follow(userId1, userId2, isFollowing);
            hibernate.persistFollow(follow);
        } else if (isFollowing && !existingFollows.isEmpty()) {
            // Remove the existing Follow entity
        	/*Follow follow = existingFollows.get(0);
            follow.setFollowing(isFollowing);
            hibernate.persistFollow(follow);*/
            return Result.error(ErrorCode.CONFLICT);
        }

        return Result.ok(null);
    }


    @Override
    public Result<List<String>> followers(String userId, String password) {
        Log.info("followers : userId = " + userId);

        // Check if userId and password are valid
        if (userId == null || password == null) {
            Log.info("userId or Password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }
        
        Hibernate hibernate = Hibernate.getInstance();

        // Check if password is correct for userId
        Result<User> userResult = UsersClientFactory.getClient().getUser(userId, password);
        if (!userResult.isOK()) {
            Log.info("User retrieval failed or incorrect password.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // Query the database to get the list of followers for the given userId
        List<Follow> followerList = hibernate.jpql("SELECT f FROM Follow f WHERE f.followedId = '" + userId + "' AND f.isFollowing = true", Follow.class);

        List<String> followers = followerList.stream()
                                            .map(Follow::getFollowerId)
                                            .collect(Collectors.toList());

        return Result.ok(followers);
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        Log.info("like : shortId = " + shortId + ", userId = " + userId + ", isLiked = " + isLiked);

        // Check if shortId, userId, and password are valid
        if (shortId == null || userId == null || password == null) {
            Log.info("Short ID, userId or Password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        // Validate the user password (You can implement password validation logic here)

        // Retrieve the Short object from the database using Hibernate
        Result<Short> shortResult = getShort(shortId);
        
        if (!shortResult.isOK()) {
            return Result.error(ErrorCode.NOT_FOUND);
        }

        Short shortObj = shortResult.value();

        // Check if the Short object exists
        if (shortObj == null) {
            Log.info("Short does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        // Update the totalLikes field based on the isLiked flag
        if (isLiked) {
            shortObj.setTotalLikes(shortObj.getTotalLikes() + 1);
        } else {
            shortObj.setTotalLikes(shortObj.getTotalLikes() - 1);
        }

        // Update the Short object in the database using Hibernate
        Hibernate hibernate = Hibernate.getInstance();
        hibernate.updateShort(shortObj);

        return Result.ok(null);
    }


    @Override
    public Result<List<String>> likes(String shortId, String password) {
        Log.info("likes : shortId = " + shortId);

        // Check if shortId is valid
        if (shortId == null) {
            Log.info("Short ID null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        // Retrieve the Short object from the database using Hibernate
        Result<Short> shortResult = getShort(shortId);
        
        if (!shortResult.isOK()) {
            return Result.error(ErrorCode.NOT_FOUND);
        }

        Short shortObj = shortResult.value();

        // Check if the Short object exists
        if (shortObj == null) {
            Log.info("Short does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        // Retrieve the totalLikes from the Short object
        int totalLikes = shortObj.getTotalLikes();

        // Return the totalLikes as a single-element list
        List<String> likesCount = new ArrayList<>();
        likesCount.add(String.valueOf(totalLikes));

        return Result.ok(likesCount);
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        Log.info("getFeed : userId = " + userId);

        // Check if userId and password are valid
        if (userId == null || password == null) {
            Log.info("userId or Password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }
        
        Hibernate hibernate = Hibernate.getInstance();

        // Check if password is correct for userId
        Result<User> userResult = UsersClientFactory.getClient().getUser(userId, password);
        if (!userResult.isOK()) {
            Log.info("User retrieval failed or incorrect password.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // Query the database to get the list of users that the given userId follows
        List<String> following = hibernate.jpql("SELECT f.followedId FROM Follow f WHERE f.followerId = '" + userId + "'", String.class);

        // Add userId to include their own shorts in the feed
        following.add(userId);

        // If following list is empty, return an empty feed
        if (following.isEmpty()) {
            Log.info("No users followed by userId: " + userId);
            return Result.ok(Collections.emptyList());
        }

        // Query the database to get the list of shorts for the users that userId follows
        List<Short> shortsList = new ArrayList<>();
        for (String followingId : following) {
            List<Short> userShorts = hibernate.jpql("SELECT s FROM Short s WHERE s.ownerId = '" + followingId + "'", Short.class);
            shortsList.addAll(userShorts);
        }

        // Sort the shorts by timestamp (age) in descending order
        shortsList.sort(Comparator.comparing(Short::getTimestamp).reversed());

        // Extract the shortIds from the shortsList
        List<String> feed = shortsList.stream()
                                      .map(Short::getShortId)
                                      .collect(Collectors.toList());

        Log.info("Feed size: " + feed.size()); // Log the size of the feed
        Log.info("ShortsList size: " + shortsList.size()); // Log the size of the shortsList

        return Result.ok(feed);
    }




    // Helper method to generate a unique short ID
    private String generateUniqueShortId() {
        return java.util.UUID.randomUUID().toString();
    }

}
