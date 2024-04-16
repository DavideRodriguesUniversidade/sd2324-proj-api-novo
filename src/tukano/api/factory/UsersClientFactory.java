package tukano.api.factory;

import java.net.URI;

import tukano.api.java.Users;
import tukano.clients.rest.RestUsersClient;
import tukano.discovery.Discovery;

public class UsersClientFactory {

    public static Users getClient() {
        Discovery discovery = Discovery.getInstance();

        // Get discovered URIs for the Users service
        URI[] uris = discovery.knownUrisOf("users", 1);

        // Check if URIs array is empty
        if (uris == null || uris.length == 0) {
            return null;
        }

        // Use the first discovered URI for the Users service
        URI serverURI = uris[0];

        if (serverURI.toString().endsWith("rest")) {
            return new RestUsersClient(serverURI);
        } else {
            return null;
        }
    }
}
