package tukano.clients.rest;

import java.net.URI;
import java.util.List;

import org.glassfish.jersey.client.ClientConfig;

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


public class RestUsersClient implements Users {

	final URI serverURI;
	final Client client;
	final ClientConfig config;

	final WebTarget target;
	
	public RestUsersClient( URI serverURI ) {
		this.serverURI = serverURI;
		this.config = new ClientConfig();
		this.client = ClientBuilder.newClient(config);

		target = client.target( serverURI ).path( RestUsers.PATH );
	}
		
	@Override
	public Result<String> createUser(User user) {
		Response r = target.request()
				.post(Entity.entity(user, MediaType.APPLICATION_JSON));

		var status = r.getStatus();
		if( status != Status.OK.getStatusCode() )
			return Result.error( getErrorCodeFrom(status));
		else
			return Result.ok( r.readEntity( String.class ));
	}

	@Override
	public Result<User> getUser(String name, String pwd) {
		Response r = target.path( name )
				.queryParam(RestUsers.PWD, pwd).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		var status = r.getStatus();
		if( status != Status.OK.getStatusCode() )
			return Result.error( getErrorCodeFrom(status));
		else
			return Result.ok( r.readEntity( User.class ));
	}

	@Override
	public Result<User> updateUser(String userId, String password, User user) {
	    Response r = target.path(userId)
	            .queryParam(RestUsers.PWD, password)
	            .request()
	            .put(Entity.entity(user, MediaType.APPLICATION_JSON));

	    var status = r.getStatus();
	    if (status != Status.OK.getStatusCode()) {
	        return Result.error(getErrorCodeFrom(status));
	    } else {
	        return Result.ok(r.readEntity(User.class));
	    }
	}

	@Override
	public Result<User> deleteUser(String userId, String password) {
	    Response r = target.path(userId)
	            .queryParam(RestUsers.PWD, password)
	            .request()
	            .delete();

	    var status = r.getStatus();
	    if (status != Status.OK.getStatusCode()) {
	        return Result.error(getErrorCodeFrom(status));
	    } else {
	        return Result.ok(r.readEntity(User.class));
	    }
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
