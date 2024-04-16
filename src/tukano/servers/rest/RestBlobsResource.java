package tukano.servers.rest;

import java.util.function.Consumer;

import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import tukano.api.java.Result;
import tukano.api.java.Blobs;
import tukano.servers.java.JavaBlobs;

@Singleton
public class RestBlobsResource {

    final Blobs impl;

    public RestBlobsResource() {
        this.impl = new JavaBlobs();
    }

    
    public Result<Void> upload(String blobId, byte[] bytes) {
        return resultOrThrow(impl.upload(blobId, bytes));
    }

    
    public Result<byte[]> download(String blobId) {
        return resultOrThrow(impl.download(blobId));
    }

    
    public Result<Void> downloadToSink(String blobId, Consumer<byte[]> sink) {
        return resultOrThrow(impl.downloadToSink(blobId, sink));
    }

    protected <T> Result<T> resultOrThrow(Result<T> result) {
        if (result.isOK())
            return result;
        else
            throw new WebApplicationException(statusCodeFrom(result));
    }

    private static Status statusCodeFrom(Result<?> result) {
        return switch (result.error()) {
            case CONFLICT -> Status.CONFLICT;
            case NOT_FOUND -> Status.NOT_FOUND;
            case FORBIDDEN -> Status.FORBIDDEN;
            case BAD_REQUEST -> Status.BAD_REQUEST;
            case INTERNAL_ERROR -> Status.INTERNAL_SERVER_ERROR;
            case NOT_IMPLEMENTED -> Status.NOT_IMPLEMENTED;
            case OK -> result.value() == null ? Status.NO_CONTENT : Status.OK;
            default -> Status.INTERNAL_SERVER_ERROR;
        };
    }

}
