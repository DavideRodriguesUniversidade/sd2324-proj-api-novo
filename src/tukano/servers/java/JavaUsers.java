package tukano.servers.java;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.api.java.Result.ErrorCode;
import tukano.api.User;
import tukano.api.clients.ShortsClient;
import tukano.api.java.Users;
import tukano.persistence.Hibernate;

public class JavaUsers implements Users {

    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

    private Hibernate hibernate = Hibernate.getInstance();;
    
    @Override
    public Result<String> createUser(User user) {
        Log.info("createUser : " + user);

        // Check if user data is valid
        if (user.userId() == null || user.pwd() == null || user.displayName() == null || user.email() == null) {
            Log.info("User object invalid.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }
        
        // Check if user already exists in the database
        List<User> existingUsers = hibernate.jpql("SELECT u FROM User u WHERE u.userId = '" + user.userId() + "'", User.class);
        
        if (!existingUsers.isEmpty()) {
            Log.info("User already exists.");
            return Result.error(ErrorCode.CONFLICT);
        }

        // Persist the new user
        try {
            hibernate.persistUser(user); 
            
        } catch (Exception e) {
            Log.info("Failed to persist user.");
            e.printStackTrace();
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        return Result.ok(user.userId());
    }

    @Override
    public Result<User> getUser(String userId, String pwd) {
        Log.info("getUser : user = " + userId + "; pwd = " + pwd);

        // Check if user is valid
        if (userId == null || pwd == null) {
            Log.info("Name or Password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        // Fetch the user from the database using Hibernate
        List<User> users = hibernate.jpql("SELECT u FROM User u WHERE u.userId = '" + userId + "'", User.class);

        // Check if user exists and if the password matches
        switch (userVerifications(users, pwd)) {
            case 1:
                return Result.error(ErrorCode.NOT_FOUND);
            case 2:
                return Result.error(ErrorCode.FORBIDDEN);
        }

        User user = users.get(0);
        
        return Result.ok(user);
    }
    
    @Override
    public Result<User> updateUser(String userId, String pwd, User updatedUser) {
        Log.info("updateUser : user = " + userId);

        // Check if userId, password, and updatedUser are valid
        if (userId == null || pwd == null || updatedUser == null) {
            Log.info("Invalid input.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        // Fetch the user from the database using Hibernate
        List<User> users = hibernate.jpql("SELECT u FROM User u WHERE u.userId = '" + userId + "'", User.class);

        // Check if user exists and if the password matches
        switch (userVerifications(users, pwd)) {
            case 1:
                return Result.error(ErrorCode.NOT_FOUND);
            case 2:
                return Result.error(ErrorCode.FORBIDDEN);
        }

        User user = users.get(0);

        // Check if userId is being changed
        if (!userId.equals(updatedUser.userId())) {
        	if(updatedUser.userId() == null) {
        		user.setUserId(userId);
        	}else {
        		Log.info("Cannot change userId.");
                return Result.error(ErrorCode.BAD_REQUEST);
        	}
        }

        // Update user details
        if (updatedUser.pwd() != null) {
            user.setPwd(updatedUser.pwd());
        }
        if (updatedUser.email() != null) {
            user.setEmail(updatedUser.email());
        }
        if (updatedUser.displayName() != null) {
            user.setDisplayName(updatedUser.displayName());
        }

        // Update the user in the database
        try {
            hibernate.updateUser(user);
        } catch (Exception e) {
            Log.info("Failed to update user.");
            e.printStackTrace();
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        return Result.ok(user);
    }

    @Override
    public Result<User> deleteUser(String userId, String pwd) {
        Log.info("deleteUser : user = " + userId);

        // Check if user is valid
        if (userId == null || pwd == null) {
            Log.info("Name or Password null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        // Retrieve user
        List<User> users = hibernate.jpql("SELECT u FROM User u WHERE u.userId = '" + userId + "'", User.class);

        // Check if user exists and if the password matches
        switch (userVerifications(users, pwd)) {
            case 1:
                return Result.error(ErrorCode.NOT_FOUND);
            case 2:
                return Result.error(ErrorCode.FORBIDDEN);
        }
        User user = users.get(0);

        Shorts shortsClient = ShortsClient.getClient();

        // Delete shorts associated with the user
        Result<Void> deleteShortsResult = shortsClient.deleteShortsByUser(userId);
        if (!deleteShortsResult.isOK()) {
            Log.info("Error deleting shorts for user: " + userId);
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
        
        Result<Void> deleteLikesResult = shortsClient.deleteLikesByUser(userId);
        if (!deleteLikesResult.isOK()) {
            Log.info("Error deleting shorts for user: " + userId);
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }

        // Delete user
        hibernate.delete(user);

        return Result.ok(user);
    }


    @Override
    public Result<List<User>> searchUsers(String pattern) {
        Log.info("searchUsers : pattern = " + pattern);

        // Check if pattern is valid
        if (pattern == null) {
            Log.info("Pattern null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }
        
        List<User> matchedUsers = hibernate.jpql("SELECT u FROM User u WHERE LOWER(u.userId) LIKE '%" + pattern.toLowerCase() + "%'", User.class);
        
        return Result.ok(matchedUsers);
    }
    
    
    public Result<User> activeUser(String userId) {
        Log.info("activeUser: userId = " + userId);
        
        // Retrieve the user from the database using Hibernate
        List<User> users = hibernate.jpql2(
                "SELECT u FROM User u WHERE u.userId = :userId",
                User.class,
                Map.of("userId", userId));
        
        // Check if the user exists
        if (users.isEmpty()) {
            Log.info("User does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        
        User activeUser = users.get(0);
        return Result.ok(activeUser);
    }

    private int userVerifications(List<User> users, String pwd){
        // Check if user exists
        if (users.isEmpty()) {
            Log.info("User does not exist.");
            return 1;
        }
        
        // Check if the password is correct
        if (!users.get(0).pwd().equals(pwd)) {
            Log.info("Password is incorrect.");
            return 2;
        }
        return 0;
    }
}