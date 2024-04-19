package tukano.servers.java;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import tukano.api.*;
import tukano.api.clients.ShortsClient;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.java.Shorts;
import tukano.api.java.Blobs;

public class JavaBlobs implements Blobs {

    private static Logger Log = Logger.getLogger(JavaBlobs.class.getName());
    
    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        Log.info("upload : blobId = " + blobId);

        Shorts shortsClient = ShortsClient.getClient();
        
        // Retrieve verifier details via GET request
        Result<List<Verifier>> verifierResult = shortsClient.verify(blobId);

        if (!verifierResult.isOK()) {
            Log.info("BlobId is not valid.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        Verifier verifier = verifierResult.value().get(0);

        // Validate the verifier
        if (!verifier.getVerifier().equals(blobId)) {
            Log.info("Verifier does not match.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        // Save the blob to the local file system
        try {
            Path path = Paths.get("", blobId);

            if (Files.notExists(path)) {
                Files.createFile(path);
                Files.write(path, bytes);
                Log.info("Blob uploaded successfully.");
            } else {
                // If blob exists but bytes don't match, return CONFLICT
                byte[] existingBytes = Files.readAllBytes(path);
                if (!Arrays.equals(existingBytes, bytes)) {
                    Log.info("Blob content does not match.");
                    return Result.error(ErrorCode.CONFLICT);
                }
                Log.info("Blob already exists.");
            }
        } catch (IOException e) {
            Log.severe("Error saving or reading blob: " + e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }

        return Result.ok(null);
    }
    
    @Override
    public Result<byte[]> download(String blobId) {
        Log.info("download : blobId = " + blobId);

        // Check if the blob exists
        Path path = Paths.get("", blobId);
        if (Files.notExists(path)) {
            Log.info("Blob not found.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        try {
            byte[] bytes = Files.readAllBytes(path);
            Log.info("Blob downloaded successfully.");
            return Result.ok(bytes);
        } catch (IOException e) {
            Log.severe("Error reading blob: " + e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }
    
    public Result<Void> deleteBlob(String blobId) {
        Log.info("deleteBlob : blobId = " + blobId);
        
        try {
            Path blobPath = Paths.get("", blobId);
            Files.deleteIfExists(blobPath);
            Log.info("Blob deleted successfully.");
            return Result.ok(null);
        } catch (IOException e) {
            Log.severe("Error deleting blob: " + e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

}
