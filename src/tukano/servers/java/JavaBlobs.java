package tukano.servers.java;

import java.util.function.Consumer;
import java.util.logging.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.java.Blobs;

public class JavaBlobs implements Blobs {

    private static Logger Log = Logger.getLogger(JavaBlobs.class.getName());

    private boolean blobExistsInFileSystem(String blobId) {
        // Supondo que os blobs estejam armazenados em algum diretório específico
        Path blobFilePath = Paths.get("", blobId);
        return Files.exists(blobFilePath);
    }
    
    
   /* public Result<Void> upload(String blobId, byte[] bytes) {
        Shorts shortsClient = ShortsClientFactory.getClientShorts();

        try {
            Result<Short> verifiers = shortsClient.getShortByBlobId(blobId);
            Log.info("chegou ao upload");
            Log.info("Verifiers value: " + verifiers.value());            

            if (!verifiers.isOK()) {
                return Result.error(Result.ErrorCode.FORBIDDEN);
            }

            if (blobExistsInFileSystem(blobId)) {
                byte[] fileBytes;
                try {
                    fileBytes = Files.readAllBytes(Paths.get("", blobId));
                } catch (IOException e) {
                    // Handle IOException, e.g., log the error
                    return Result.error(Result.ErrorCode.INTERNAL_ERROR);
                }

                if (!Arrays.equals(fileBytes, bytes)) {
                    return Result.error(Result.ErrorCode.CONFLICT);
                }

                return Result.ok();
            }

            // Se o blobId não existe no sistema de arquivos, cria um novo blob
            createNewBlob(blobId, bytes);
            return Result.ok(); // Upload bem-sucedido
        } catch (Exception e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }*/
    
    private Result<Void> createNewBlob(String blobId, byte[] bytes) {

        try {
            Path path = Paths.get("", blobId);

            if (Files.notExists(path)) {
                Files.createFile(path);
            }
            Files.write(path, bytes);
            return Result.ok(null);
        } catch (IOException e) {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<byte[]> download(String blobId) {
        Log.info("download : blobId = " + blobId);

        // Define the directory where blobs are stored
        String storageDirectory = "./blobs/";

        try {
            // Define the path for the blob file
            Path blobPath = Paths.get(storageDirectory, blobId);

            // Check if blobId exists
            if (Files.exists(blobPath)) {
                // Read the blob content
                byte[] blobBytes = Files.readAllBytes(blobPath);

                // Return the blob content
                return Result.ok(blobBytes);
            } else {
                // If blobId does not exist, return NOT_FOUND
                return Result.error(ErrorCode.NOT_FOUND);
            }

        } catch (Exception e) {
            Log.severe("Error downloading blob: " + e.getMessage());
            e.printStackTrace();
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> downloadToSink(String blobId, Consumer<byte[]> sink) {
        Log.info("downloadToSink : blobId = " + blobId);

        // Define the directory where blobs are stored
        String storageDirectory = "./blobs/";

        try {
            // Define the path for the blob file
            Path blobPath = Paths.get(storageDirectory, blobId);

            // Check if blobId exists
            if (Files.exists(blobPath)) {
                // Read and stream the blob content in chunks
                byte[] buffer = new byte[4096]; // 4KB buffer
                int bytesRead;
                try (var inputStream = Files.newInputStream(blobPath)) {
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        byte[] chunk = new byte[bytesRead];
                        System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                        sink.accept(chunk); // Pass the chunk to the sink
                    }
                }
                return Result.ok();
            } else {
                // If blobId does not exist, return NOT_FOUND
                return Result.error(ErrorCode.NOT_FOUND);
            }

        } catch (Exception e) {
            Log.severe("Error downloading blob to sink: " + e.getMessage());
            e.printStackTrace();
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    // Helper method to generate a unique blob ID
    private String generateUniqueBlobId() {
        return java.util.UUID.randomUUID().toString();
    }


	@Override
	public Result<Void> upload(String blobId, byte[] bytes) {
		// TODO Auto-generated method stub
		return null;
	}
}
