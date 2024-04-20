package tukano.api.clients;

import java.net.URI;

import tukano.api.java.Shorts;
import tukano.clients.rest.RestShortsClient;
import tukano.discovery.Discovery;
import tukano.servers.rest.RestShortsServer;

public class ShortsClient {

    public static Shorts getClient() {
    	Discovery discovery = Discovery.getInstance();
        var res = discovery.knownUrisOf(RestShortsServer.SERVICE, 1);
        var port = res[0].getPort();
        var hostname = res[0].getHost();

        String serverURI = String.format(RestShortsServer.SERVER_URI_FMT, hostname, port);
        return new RestShortsClient(URI.create(serverURI));
    }
}
