package tukano.servers.rest;


import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.rest.RestBlobs;
import tukano.servers.java.JavaBlobs;
import jakarta.inject.Singleton;

@Singleton
public class RestBlobsResource implements RestBlobs {
    final Blobs impl;

    public RestBlobsResource() {
        this.impl = new JavaBlobs();
    }

    @Override
    public void upload(String blobId, byte[] bytes){
     resultOrThrow(impl.upload(blobId,bytes));
    }

    @Override
    public byte[] download(String blobId){
        return resultOrThrow(impl.download(blobId));
    }

    protected <T> T resultOrThrow(Result<T> result) {
        if (result.isOK())
            return result.value();
        else
            throw new WebApplicationException(statusCodeFrom(result));
    }

    /**
     * Translates a Result<T> to a HTTP Status code
     */
    private static Response.Status statusCodeFrom(Result<?> result) {
        return switch (result.error()) {
            case CONFLICT -> Response.Status.CONFLICT;
            case NOT_FOUND -> Response.Status.NOT_FOUND;
            case FORBIDDEN -> Response.Status.FORBIDDEN;
            case BAD_REQUEST -> Response.Status.BAD_REQUEST;
            case INTERNAL_ERROR -> Response.Status.INTERNAL_SERVER_ERROR;
            case NOT_IMPLEMENTED -> Response.Status.NOT_IMPLEMENTED;
            case OK -> result.value() == null ? Response.Status.NO_CONTENT : Response.Status.OK;
            default -> Response.Status.INTERNAL_SERVER_ERROR;
        };
    }

}