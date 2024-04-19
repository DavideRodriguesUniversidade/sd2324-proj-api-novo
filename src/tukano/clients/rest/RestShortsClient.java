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
import tukano.api.Verifier;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.Short;
import tukano.api.java.Shorts;
import tukano.api.rest.RestShorts;
import tukano.api.rest.RestUsers;

public class RestShortsClient  extends RestClient implements Shorts {

	protected static final int READ_TIMEOUT = 2000;
	protected static final int CONNECT_TIMEOUT = 2000;

	protected static final int MAX_RETRIES = 6;
	protected static final int RETRY_SLEEP = 2000;

	
	final URI serverURI;
	final Client client;
	final ClientConfig config;

	final WebTarget target;
	
	public RestShortsClient( URI serverURI ) {
		this.serverURI = serverURI;
		this.config = new ClientConfig();
		
		config.property( ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
		config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

		this.client = ClientBuilder.newClient(config);

		target = client.target( serverURI ).path(RestShorts.PATH);
	}
	
	 private Result<Short> clt_createShort(String userId, String password) {
			return super.toJavaResult(
	    			target.path( userId )
	    			.queryParam(RestUsers.PWD, password).request()
	    			.accept(MediaType.APPLICATION_JSON)
	    			.post(Entity.json(null)), Short.class);
    }
	 
	 private Result<Void> clt_deleteShort(String userId, String password) {
			return super.toJavaResult(
	    			target.path(userId)
					.queryParam(RestUsers.PWD, password)
					.request()
					.delete(),null);
	    }
	 
	 private Result<Short> clt_getShort(String shortId) {
		 String path = shortId + RestShorts.SHORTS;
			return super.toJavaResult(
					target.path( path )
	    			.request()
	    			.accept(MediaType.APPLICATION_JSON)
	    			.get(), Short.class);
	    }
	 
	 private Result<Void> clt_follow(String userId1, String userId2, boolean isFollowing, String password) {
		 String path = userId1 + "/" + userId2 + RestShorts.FOLLOWERS;
			return super.toJavaResult(
					target.path(path )
					.queryParam(RestUsers.PWD, password)
	    			.request()
	    			.accept(MediaType.APPLICATION_JSON)
	    			.post(Entity.json(null)), null);
	    }

		private Result<List<String>> clt_getShorts(String userId) {
			Response r = target.path( userId )
					.request()
					.accept(MediaType.APPLICATION_JSON)
					.get();
			
				try {
					var status = r.getStatus();
					if( status != Status.OK.getStatusCode() )
						return Result.error( getErrorCodeFrom(status));
					else
						return  Result.ok(r.readEntity(new GenericType<List<String>>(){}));
				}finally {
					r.close();
				}
		}
	 
		private Result<List<String>> clt_followers(String userId, String password) {
			Response r = target.path(userId)
					.queryParam(RestShorts.PWD, password)
					.request()
					.accept(MediaType.APPLICATION_JSON)
					.get();
			
			try {
				var status = r.getStatus();
				if (status != Response.Status.OK.getStatusCode()) {
					return Result.error(getErrorCodeFrom(status));
				} else {
					return Result.ok(r.readEntity(new GenericType<List<String>>() {
					}));
				}
			} finally {
				r.close();
			}
		}
	 
		private Result<Void> clt_like(String shortId, String userId, boolean isLiked, String password) {
			 String path = shortId + "/" + userId + RestShorts.LIKES;
				return super.toJavaResult(
						target.path(path )
						.queryParam(RestUsers.PWD, password)
		    			.request()
		    			.accept(MediaType.APPLICATION_JSON)
		    			.post(Entity.json(null)), null);
		 }
		 
		private Result<List<String>> clt_likes(String shortId, String password) {
			String path = shortId + RestShorts.LIKES;
			Response r = target.path(path)
					.queryParam(RestShorts.PWD, password)
					.request()
					.accept(MediaType.APPLICATION_JSON)
					.get();
			try {
				var status = r.getStatus();
				if (status != Response.Status.OK.getStatusCode()) {
					return Result.error(getErrorCodeFrom(status));
				} else {
					return Result.ok(r.readEntity(new GenericType<List<String>>() {
					}));
				}
			} finally {
				r.close();
			}
		}
		
		private  Result<List<String>> clt_getFeed(String userId, String password) {
			String path = userId + RestShorts.FEED;
			Response r = target.path(path)
					.queryParam(RestShorts.PWD, password)
					.request()
					.accept(MediaType.APPLICATION_JSON)
					.get();
			try {
				var status = r.getStatus();
				if (status != Response.Status.OK.getStatusCode()) {
					return Result.error(getErrorCodeFrom(status));
				} else {
					return Result.ok(r.readEntity(new GenericType<List<String>>() {
					}));
				}
			} finally {
				r.close();
			}
		}
		
		private Result<List<Verifier>> clt_verify(String blobId) {
			String path = blobId+"/verify";
			Response r = target.path(path)
					.request()
					.accept(MediaType.APPLICATION_JSON)
					.get();
			try {
				var status = r.getStatus();
				if (status != Response.Status.OK.getStatusCode()) {
					return Result.error(getErrorCodeFrom(status));
				} else {
					return Result.ok(r.readEntity(new GenericType<List<Verifier>>() {
					}));
				}
			} finally {
				r.close();
			}
		}
		
		private Result<Void> clt_deleteShortsByUser(String userId) {
	        return super.toJavaResult(
	                target.path(userId + "/shorts")
	                        .request()
	                        .delete(), null);
	    }
		
		private Result<Void> clt_deleteLikesByUser(String userId) {
		    return super.toJavaResult(
		            target.path(userId + "/likes")
		                    .request()
		                    .delete(), null);
		}
	
	public Result<Short> createShort(String userId, String password) {
		   return super.reTry( () -> clt_createShort(userId, password));
	}

	@Override
	public Result<Void> deleteShort(String shortId, String password) {
		 return super.reTry( () -> clt_deleteShort(shortId, password));
	}

	@Override
	public Result<Short> getShort(String shortId) {
		 return super.reTry( () -> clt_getShort(shortId));
	}
	
	@Override
	public Result<List<String>> getShorts(String userId) {
		 return super.reTry( () -> clt_getShorts(userId));
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
		 return super.reTry( () -> clt_follow( userId1,  userId2,  isFollowing,  password));
	}

	@Override
	public Result<List<String>> followers(String userId, String password) {
		return super.reTry(() -> clt_followers(userId, password));
	}

	@Override
	public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
		 return super.reTry( () -> clt_like( shortId,  userId,  isLiked,  password));
	}

	@Override
	public Result<List<String>> likes(String shortId, String password) {
		return super.reTry( () -> clt_likes( shortId, password));
	}

	@Override
	public Result<List<String>> getFeed(String userId, String password) {
		return super.reTry( () -> clt_getFeed( userId, password));
	}

	@Override
	public Result<List<Verifier>> verify(String verifier) {
		return super.reTry(()-> clt_verify(verifier));
	}
	
	@Override
    public Result<Void> deleteShortsByUser(String userId) {
        return super.reTry(() -> clt_deleteShortsByUser(userId));
    }
	
	@Override
	public Result<Void> deleteLikesByUser(String userId) {
	    return super.reTry(() -> clt_deleteLikesByUser(userId));
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
