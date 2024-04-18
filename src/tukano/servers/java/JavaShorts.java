package tukano.servers.java;

import java.net.URI;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.Follow;
import tukano.api.ShortLike;
import tukano.api.Short;
import tukano.api.User;
import tukano.api.Verifier;
import tukano.api.clients.BlobsClient;
import tukano.api.clients.UsersClient;
import tukano.api.java.Shorts;
import tukano.api.java.Users;
import tukano.discovery.Discovery;
import tukano.persistence.Hibernate;
import tukano.servers.rest.RestBlobsServer;
import tukano.servers.rest.RestUsersServer;

public class JavaShorts implements Shorts {

    private static Logger Log = Logger.getLogger(JavaShorts.class.getName());
    
    @Override
    public Result<Short> createShort(String userId, String password) {
        Log.info("createShort : userId = " + userId);

        try {
            Users usersClient = UsersClient.getClient();
            
            // Retrieve user details via GET request
            Result<User> userResult = usersClient.getUser(userId, password);
            
            if (!userResult.isOK()) {
                Log.info("User retrieval failed or incorrect password.");
                return Result.error(ErrorCode.BAD_REQUEST);
            }

            String shortId = generateUniqueShortId();
            String verifier = UUID.randomUUID().toString();
            
            Verifier v = new Verifier(shortId, verifier);
            Hibernate hibernate = Hibernate.getInstance();
            hibernate.persist(v);
            
            String blobUrl = getUrl() + "/blobs/" + verifier;

            // Create a new short
            Short newShort = new Short(shortId, userId, blobUrl);

            // Persist the short using Hibernate
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

        // Check if shortId or password is null
        if (shortId == null || password == null) {
            Log.info("Short ID or Password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        // Retrieve the short from the database
        Result<Short> result = getShort(shortId);
        if (!result.isOK()) {
            return Result.error(ErrorCode.NOT_FOUND);
        }

        // Check if the password matches
        Result<User> userResult = UsersClient.getClient().getUser(result.value().getOwnerId(), password);
        if (!userResult.isOK()) {
            Log.info("Incorrect password.");
            return Result.error(ErrorCode.FORBIDDEN);
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
        
        Users usersClient = UsersClient.getClient();

        // Check if the user exists
        Result<User> userResult = usersClient.activeUser(userId);
        if (!userResult.isOK()) {
            Log.info("User not found: " + userId);
            return Result.error(ErrorCode.NOT_FOUND);
        }

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
        Result<User> userResult1 = UsersClient.getClient().getUser(userId1, password);
        if (!userResult1.isOK()) {
            Log.info("User retrieval failed or incorrect password for userId1.");
            return Result.error(ErrorCode.FORBIDDEN);
        }
        
        // Check if the follow relationship already exists
        List<Follow> existingFollows = hibernate.jpql("SELECT f FROM Follow f WHERE f.followerId = '" + userId1 + "' AND f.followedId = '" + userId2 + "'", Follow.class);

        if (isFollowing && existingFollows.isEmpty()) {
            // Create a new Follow entity and persist it
            Follow follow = new Follow(userId1, userId2, isFollowing);
            hibernate.persist(follow);
        } else if (isFollowing && !existingFollows.isEmpty()) {
            return Result.error(ErrorCode.CONFLICT);
        } else if (!isFollowing && !existingFollows.isEmpty()) {
        	Follow existingFollow = existingFollows.get(0);
            hibernate.delete(existingFollow);
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
        Result<User> userResult = UsersClient.getClient().getUser(userId, password);
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
            Log.info("shortId, userId, or Password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        Hibernate hibernate = Hibernate.getInstance();

        // Check if password is correct for userId
        Result<User> userResult = UsersClient.getClient().getUser(userId, password);
        if (!userResult.isOK()) {
            Log.info("User retrieval failed or incorrect password.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // Check if the short exists
        Short existingShort = hibernate.jpql("SELECT s FROM Short s WHERE s.shortId = '" + shortId + "'", Short.class).stream().findFirst().orElse(null);
        if (existingShort == null) {
            Log.info("Short not found.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        // Check if the like relationship already exists
        List<ShortLike> existingLike = hibernate.jpql("SELECT l FROM ShortLike l WHERE l.shortId = '" + shortId + "' AND l.userId = '" + userId + "'", ShortLike.class);
        if (isLiked && existingLike.isEmpty()) {
            // Create a new Like entity and persist it
            ShortLike like = new ShortLike(shortId, userId, isLiked);
            hibernate.persist(like);

            // Update the totalLikes count in the Short entity
            existingShort.setTotalLikes(existingShort.getTotalLikes() + 1);
            hibernate.updateShort(existingShort); 
        } else if (!isLiked && !existingLike.isEmpty()) {
            // Remove the existing Like entity
            hibernate.delete(existingLike.get(0));

            // Update the totalLikes count in the Short entity
            existingShort.setTotalLikes(existingShort.getTotalLikes() - 1);
            hibernate.updateShort(existingShort); 
        } else if (isLiked && !existingLike.isEmpty()) {
            return Result.error(ErrorCode.CONFLICT);
        }

        return Result.ok(null);
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        Log.info("likes : shortId = " + shortId);

        // Check if shortId and password are valid
        if (shortId == null || password == null) {
            Log.info("shortId or Password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        Hibernate hibernate = Hibernate.getInstance();

        // Check if password is correct for the owner of the short
        Short existingShort = hibernate.jpql("SELECT s FROM Short s WHERE s.shortId = '" + shortId + "'", Short.class).stream().findFirst().orElse(null);
        if (existingShort == null) {
            Log.info("Short not found.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        Result<User> userResult = UsersClient.getClient().getUser(existingShort.getOwnerId(), password);
        if (!userResult.isOK()) {
            Log.info("User retrieval failed or incorrect password.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // Retrieve all the userIds of the users who liked the short
        List<ShortLike> likes = hibernate.jpql("SELECT l FROM ShortLike l WHERE l.shortId = '" + shortId + "' AND l.isLiked = true", ShortLike.class);

        List<String> likedUserIds = likes.stream().map(ShortLike::getUserId).collect(Collectors.toList());

        return Result.ok(likedUserIds);
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        Log.info("getFeed : userId = " + userId);

        // Check if userId and password are valid
        if (userId == null || password == null) {
            Log.info("userId or Password null.");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        
        Hibernate hibernate = Hibernate.getInstance();

        
        Result<User> userResult = UsersClient.getClient().getUser(userId, password);
        if (!userResult.isOK() && userResult.error() == ErrorCode.FORBIDDEN) {
            Log.info("Incorrect password.");
            return Result.error(ErrorCode.FORBIDDEN);
        } else if (!userResult.isOK()) {
            Log.info("User retrieval failed.");
            return Result.error(ErrorCode.NOT_FOUND);
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

        return Result.ok(feed);
    }
    
    public Result<List<Verifier>> verify(String verifier) {
        Log.info("verify : verifier = " + verifier);

		List<Verifier> verifiers = Hibernate.getInstance().jpql2(
				"SELECT v FROM Verifier v WHERE (v.verifier = :verifier)",
				Verifier.class,
				Map.of("verifier", verifier));
		if (verifiers.isEmpty()) {
			Log.info("Verifier does not exist.");
			return Result.error(ErrorCode.NOT_IMPLEMENTED);
		}
		return Result.ok(verifiers);
	}
    
    @Override
    public Result<Void> deleteShortsByUser(String userId) {
        Log.info("deleteShortsByUser : userId = " + userId);

        // Fetch the list of shorts associated with the user
        Hibernate hibernate = Hibernate.getInstance();
        List<Short> userShorts = hibernate.jpql("SELECT s FROM Short s WHERE s.ownerId = '" + userId + "'", Short.class);

        if (userShorts.isEmpty()) {
            Log.info("No shorts found for user: " + userId);
            return Result.ok(null);
        }
        
        // Delete each short associated with the user
        for (Short s : userShorts) {
            Blobs blobsClient = BlobsClient.getClient();
            
            List<Verifier> existingVerifiers = Hibernate.getInstance().jpql2(
    				"SELECT v FROM Verifier v WHERE v.shortId = :shortId",
    				Verifier.class,
    				Map.of("shortId", s.getShortId()));
            
            if (!existingVerifiers.isEmpty()) {
    			for (Verifier Verifiers : existingVerifiers) {
    				blobsClient.deleteBlob(Verifiers.getVerifier());
    				Hibernate.getInstance().delete(Verifiers);
    			}
    		}

            // Delete the short from the database
            hibernate.delete(s);
        }

        Log.info("Deleted shorts and blobs for user: " + userId);
        return Result.ok(null);
    }
    
    
    @Override
    public Result<Void> deleteLikesByUser(String userId) {
        Log.info("deleteLikesByUser : userId = " + userId);

        // Fetch the list of likes associated with the user
        Hibernate hibernate = Hibernate.getInstance();
        List<ShortLike> userLikes = hibernate.jpql("SELECT l FROM ShortLike l WHERE l.userId = '" + userId + "'", ShortLike.class);

        if (userLikes.isEmpty()) {
            Log.info("No likes found for user: " + userId);
            return Result.ok(null);
        }

        // Delete each like associated with the user
        for (ShortLike like : userLikes) {
            // Retrieve the short associated with the like
            Short existingShort = hibernate.jpql("SELECT s FROM Short s WHERE s.shortId = '" + like.getShortId() + "'", Short.class).stream().findFirst().orElse(null);
            
            if (existingShort != null) {
                // Update the totalLikes count in the Short entity
                existingShort.setTotalLikes(existingShort.getTotalLikes() - 1);
                hibernate.updateShort(existingShort); 
            }

            // Delete the like from the database
            hibernate.delete(like);
        }

        Log.info("Deleted likes for user: " + userId);
        return Result.ok(null);
    }
    
    private static int counter = 0;

    private String getUrl() {
        Discovery discovery = Discovery.getInstance();
        var res = discovery.knownUrisOf(RestBlobsServer.SERVICE, 1);

        // Use the counter to alternate between the two servers
        var port = res[counter % res.length].getPort();
        var hostname = res[counter % res.length].getHost();

        // Increment the counter for the next request
        counter++;

        return String.format(RestUsersServer.SERVER_URI_FMT, hostname, port);
    }



    // Helper method to generate a unique short ID
    private String generateUniqueShortId() {
        return java.util.UUID.randomUUID().toString();
    }
}
