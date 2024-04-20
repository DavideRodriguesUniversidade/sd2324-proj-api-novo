package tukano.clients.rest;

import java.net.URI;
import java.util.List;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.User;
import tukano.api.java.Users;
import tukano.api.rest.RestUsers;

public class RestUsersClient extends RestClient implements Users {
	
	protected static final int READ_TIMEOUT = 2000;
	protected static final int CONNECT_TIMEOUT = 2000;

	protected static final int MAX_RETRIES = 6;
	protected static final int RETRY_SLEEP = 2000;

	final URI serverURI;
	final Client client;
	final ClientConfig config;

	final WebTarget target;
	
	public RestUsersClient( URI serverURI ) {
		this.serverURI = serverURI;
		this.config = new ClientConfig();
		
		config.property( ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
		config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

		this.client = ClientBuilder.newClient(config);

		target = client.target( serverURI ).path( RestUsers.PATH );
	}
	
	private Result<String> clt_createUser(User user) {
    	return super.toJavaResult( 
    		target.request()
    		.accept(MediaType.APPLICATION_JSON)
    		.post(Entity.entity(user, MediaType.APPLICATION_JSON)), String.class );
    }
	
	 private Result<User> clt_getUser(String userId, String pwd) {
	    	return super.toJavaResult(
	    			target.path( userId )
	    			.queryParam(RestUsers.PWD, pwd).request()
	    			.accept(MediaType.APPLICATION_JSON)
	    			.get(), User.class);
	    }
	 
	 private Result<User> clt_updateUser(String userId, String pwd, User user) {
	    	return super.toJavaResult(
	    			target.path(userId)
					.queryParam(RestUsers.PWD, pwd)
					.request().put(Entity.entity(user, MediaType.APPLICATION_JSON)),User.class);
	    }
	 
	 private Result<User> clt_deleteUser(String userId, String pwd) {
	    	return super.toJavaResult(
	    			target.path(userId)
					.queryParam(RestUsers.PWD, pwd)
					.request()
					.delete(),User.class);
	    }
	 
	 private Result<User> clt_activeUser(String userId) {
		 	return super.toJavaResult(
		 			target.path(userId + "/activeUser")
		 			.request()
		 			.accept(MediaType.APPLICATION_JSON)
		 			.get(), User.class);
	    }
	 
	 
	 	@Override
		public Result<String> createUser(User user) {
			return super.reTry( () -> clt_createUser(user));
		}

		@Override
		public Result<User> getUser(String name, String pwd) {
			return super.reTry( () -> clt_getUser(name,pwd));
		}
		
		@Override
		public Result<User> updateUser(String userId, String password, User user) {
			return super.reTry( () -> clt_updateUser(userId,password,user));
		}

		@Override
		public Result<User> deleteUser(String userId, String password) {
			return super.reTry( () -> clt_deleteUser(userId,password));
		}
		@Override
		public Result<List<User>> searchUsers(String pattern) {
		    Response r = target.queryParam(RestUsers.QUERY, pattern)
		            .request()
		            .accept(MediaType.APPLICATION_JSON)
		            .get();
	
		    var status = r.getStatus();
		    if (status != Status.OK.getStatusCode()) {
		        return Result.error(getErrorCodeFrom(status));
		    } else {
		    	List<User> users = r.readEntity(new GenericType<List<User>>() {});
		        return Result.ok(users);
		    }
		}
		
		@Override
		public Result<User> activeUser(String userId) {
			return super.reTry( () -> clt_activeUser(userId));
		}

	public static ErrorCode getErrorCodeFrom(int status) {
		return switch (status) {
		case 200, 209 -> ErrorCode.OK;
		case 409 -> ErrorCode.CONFLICT;
		case 403 -> ErrorCode.FORBIDDEN;
		case 404 -> ErrorCode.NOT_FOUND;
		case 400 -> ErrorCode.BAD_REQUEST;
		case 500 -> ErrorCode.INTERNAL_ERROR;
		case 501 -> ErrorCode.NOT_IMPLEMENTED;
		default -> ErrorCode.INTERNAL_ERROR;
		};
	}

	
}
