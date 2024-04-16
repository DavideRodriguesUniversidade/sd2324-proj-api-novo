package tukano.api.factory;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tukano.api.java.Shorts;
import tukano.api.java.Users;
import tukano.clients.rest.RestUsersClient;
import tukano.discovery.Discovery;
import tukano.shorts.rest.RestShortsClient;

public class UsersShortsFactory {

    private static final String SHORTS_SERVICE_NAME = "shorts";

    private static final Map<String, Shorts> clientCache = new ConcurrentHashMap<>();

    public static Shorts getClient() {
        Discovery discovery = Discovery.getInstance();
        URI[] uris = discovery.knownUrisOf(SHORTS_SERVICE_NAME, 1);
        URI serverURI = uris[0];
        String serverURIString = serverURI.toString();

        Shorts client = clientCache.get(serverURIString);

        if (client == null) {
            if (serverURIString.endsWith("rest")) {
                client = new RestShortsClient(serverURI);
                
                clientCache.put(serverURIString, client);
            }
        }

        return client;
    }
}
