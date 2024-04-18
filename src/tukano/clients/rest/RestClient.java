package tukano.clients.rest;
	import java.util.function.Supplier;
	import jakarta.ws.rs.ProcessingException;
	import jakarta.ws.rs.core.Response;
	import jakarta.ws.rs.core.Response.Status;
	import tukano.api.java.Result;
	import tukano.api.java.Result.ErrorCode;
	import static tukano.api.java.Result.ErrorCode.INTERNAL_ERROR;
	import static tukano.api.java.Result.ErrorCode.TIMEOUT;
	import utils.Sleep;;

public class RestClient {
	protected static final int MAX_RETRIES = 10;
    protected static final int RETRY_SLEEP = 1000;

    protected <T> Result<T> reTry(Supplier<Result<T>> func) {
    	for (int i = 0; i < MAX_RETRIES; i++)
    		try {
    			return func.get();
    		} catch (ProcessingException x) {
    			Sleep.ms(RETRY_SLEEP);
    		} catch (Exception x) {
    			x.printStackTrace();
    			return Result.error(INTERNAL_ERROR);
    		}
    	return Result.error(TIMEOUT);
    }

    protected <T> Result<T> toJavaResult(Response r, Class<T> entityType) {
    	try {
    		var status = r.getStatusInfo().toEnum();
    		if (status == Status.OK && r.hasEntity())
    			return Result.ok(r.readEntity(entityType));
    		else 
    			if( status == Status.NO_CONTENT) return Result.ok();
    		
    		return Result.error(getErrorCodeFrom(status.getStatusCode()));
    	} finally {
    		r.close();
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
