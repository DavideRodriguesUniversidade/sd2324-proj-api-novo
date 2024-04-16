package tukano.servers.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.Short;
import tukano.api.User;
import tukano.api.factory.UsersClientFactory;
import tukano.api.factory.UsersShortsFactory;
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

        // Retrieve the users from the database using Hibernate
        Result<User> user1Result = getUser(userId1);
        Result<User> user2Result = getUser(userId2);
        
        if (!user1Result.isOK() || !user2Result.isOK()) {
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
        
        User user1 = user1Result.value();
        User user2 = user2Result.value();

        // Check if the follow relationship already exists
      /*  if (isFollowing(user1, user2) && isFollowing) {
            Log.info("Already following.");
            return Result.error(ErrorCode.CONFLICT);
        }*/

        // Update the follow relationship using Hibernate
       /* if (isFollowing) {
            user1.addFollowedUser(user2);
            hibernate.updateUser(user1);
        } else {
            user1.removeFollowedUser(user2);
            hibernate.updateUser(user1);
        }*/

        return Result.ok(null);
    }

    /*private boolean isFollowing(User follower, User followed) {
        return follower.getFollowedUsers().contains(followed);
    }*/

    private Result<User> getUser(String userId) {
    	Hibernate hibernate = Hibernate.getInstance();
        List<User> users = hibernate.jpql("SELECT u FROM User u WHERE u.userId = :userId", User.class);
        if (users.isEmpty()) {
            return Result.error(ErrorCode.NOT_FOUND);
        }
        return Result.ok(users.get(0));
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        Log.info("followers : userId = " + userId);

        // Check if userId is valid
        if (userId == null || password == null) {
            Log.info("userId or Password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        // Retrieve the followers for the user from the database using Hibernate
        Result<User> userResult = getUser(userId);
        
        if (!userResult.isOK()) {
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        User user = userResult.value();

        // Retrieve the followers' IDs
        /*List<String> userFollowers = user.getFollowedUsers().stream()
                .map(User::getUserId)
                .sorted()
                .collect(Collectors.toList());*/

        return Result.ok();
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

        // Check if userId is valid
        if (userId == null || password == null) {
            Log.info("userId or Password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        // Validate the user password (You can implement password validation logic here)

        // Retrieve the shorts created by the user
        Result<List<String>> userShortsResult = getShorts(userId);
        if (!userShortsResult.isOK()) {
            return Result.error(ErrorCode.NOT_FOUND);
        }

        List<String> userShorts = userShortsResult.value();

        // Retrieve the list of users that the given user is following
        Result<List<String>> followersResult = followers(userId, password);
        if (!followersResult.isOK()) {
            return Result.error(ErrorCode.FORBIDDEN);
        }

        List<String> followingUsers = followersResult.value();

        // Retrieve the shorts created by the users followed by the user
        List<String> feed = new ArrayList<>();

        for (String followingUserId : followingUsers) {
            Result<List<String>> followingUserShortsResult = getShorts(followingUserId);
            if (followingUserShortsResult.isOK()) {
                feed.addAll(followingUserShortsResult.value());
            }
        }

        // Sort the feed by the age of the shorts (You may need to implement a method to compare the timestamps)
        // For this example, let's assume the feed is already sorted by the age of the shorts

        return Result.ok(feed);
    }




    // Helper method to generate a unique short ID
    private String generateUniqueShortId() {
        return java.util.UUID.randomUUID().toString();
    }
}
