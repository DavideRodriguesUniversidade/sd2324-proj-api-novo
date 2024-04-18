package tukano.clients.rest;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import tukano.api.java.Result;
import tukano.api.java.Users;
import jakarta.ws.rs.client.Client;
import tukano.api.rest.RestBlobs;
import tukano.api.rest.RestShorts;
import tukano.api.rest.RestUsers;
import tukano.api.java.Result.ErrorCode;
import tukano.api.java.Blobs;

import java.net.URI;
import java.util.logging.Logger;

public class RestBlobsClient extends RestClient implements Blobs {
    private static Logger Log = Logger.getLogger(RestUsersClient.class.getName());
    
    protected static final int READ_TIMEOUT = 2000;
	protected static final int CONNECT_TIMEOUT = 2000;

	protected static final int MAX_RETRIES = 6;
	protected static final int RETRY_SLEEP = 2000;
    
    final URI serverURI;
    final Client client;
    final ClientConfig config;

    final WebTarget target;

    public RestBlobsClient( URI serverURI ) {
        this.serverURI = serverURI;
        this.config = new ClientConfig();
        
        config.property( ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property( ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

        this.client = ClientBuilder.newClient(config);

        target = client.target( serverURI ).path(RestBlobs.PATH);
    }

    private Result<Void> clt_upload(String blobId, byte[] bytes) {
        return super.toJavaResult(
                target.path( blobId )
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .post(Entity.json(bytes)), null);
    }

    private Result<byte[]> clt_download(String blobId) {
        return super.toJavaResult(
                target.path( blobId )
                        .request()
                        .accept(MediaType.APPLICATION_JSON)
                        .get(), byte[].class);
    }
    
    private Result<Void> clt_deleteBlob(String blobId) {
        String path = blobId+"/delete";
        return super.toJavaResult(
                target.path(path)
                        .request()
                        .delete(), null);
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        return super.reTry( () -> clt_upload(blobId, bytes));
    }

    @Override
    public Result<byte[]> download(String blobId) {
        return super.reTry( () -> clt_download(blobId));
    }
    
    @Override
    public Result<Void> deleteBlob(String blobId) {
        return super.reTry( () -> clt_deleteBlob(blobId));
    }    
    
   /* @Override
    public Result<Void> downloadToSink(String blobId, Consumer<byte[]> sink) {
        Response r = target.path(blobId)
                .request()
                .get();

        var status = r.getStatus();
        if (status != Status.OK.getStatusCode()) {
            return Result.error(getErrorCodeFrom(status));
        } else {
            byte[] bytes = r.readEntity(byte[].class);
            sink.accept(bytes);
            return Result.ok(null);
        }
    }*/

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
