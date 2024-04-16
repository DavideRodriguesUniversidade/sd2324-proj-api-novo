package tukano.servers.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.User;
import tukano.api.java.Users;
import tukano.discovery.Discovery;
import tukano.persistence.Hibernate;

public class JavaUsers implements Users {

    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

    @Override
    public Result<String> createUser(User user) {
        Log.info("createUser : " + user);

        // Check if user data is valid
        if (user.userId() == null || user.pwd() == null || user.displayName() == null || user.email() == null) {
            Log.info("User object invalid.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        // Check if user already exists in the database
        Hibernate hibernate = Hibernate.getInstance();
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
        Hibernate hibernate = Hibernate.getInstance();
        List<User> users = hibernate.jpql("SELECT u FROM User u WHERE u.userId = '" + userId + "'", User.class);

        // Check if user exists
        if (users.isEmpty()) {
            Log.info("User does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        User user = users.get(0);

        // Check if the password is correct
        if (!user.pwd().equals(pwd)) {
            Log.info("Password is incorrect.");
            return Result.error(ErrorCode.FORBIDDEN);
        }
        
        try {
            Discovery discovery = Discovery.getInstance();
            discovery.announce("users", "http://172.18.0.4:3546/rest");  
        } catch (Exception e) {
            Log.info("Failed to announce user details.");
            e.printStackTrace();
        }

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
        Hibernate hibernate = Hibernate.getInstance();
        List<User> users = hibernate.jpql("SELECT u FROM User u WHERE u.userId = '" + userId + "'", User.class);

        // Check if user exists
        if (users.isEmpty()) {
            Log.info("User does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        User user = users.get(0);

        // Check if the password is correct
        if (!user.pwd().equals(pwd)) {
            Log.info("Password is incorrect.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

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

        Hibernate hibernate = Hibernate.getInstance();
        List<User> users = hibernate.jpql("SELECT u FROM User u WHERE u.userId = '" + userId + "'", User.class);
        
        // Check if user exists
        if (users.isEmpty()) {
            Log.info("User does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        
        User user = users.get(0);

        // Check if the password is correct
        if (!user.pwd().equals(pwd)) {
            Log.info("Password is incorrect.");
            return Result.error(ErrorCode.FORBIDDEN);
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
         
        Hibernate hibernate = Hibernate.getInstance();
        List<User> matchedUsers = hibernate.jpql("SELECT u FROM User u WHERE LOWER(u.userId) LIKE '%" + pattern.toLowerCase() + "%'", User.class);
        
        
        return Result.ok(matchedUsers);
    }


}