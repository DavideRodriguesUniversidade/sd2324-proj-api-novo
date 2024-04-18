package tukano.api.clients;

import java.net.URI;

import tukano.api.java.Blobs;
import tukano.clients.rest.RestBlobsClient;
import tukano.clients.rest.RestShortsClient;
import tukano.discovery.Discovery;
import tukano.servers.rest.RestBlobsServer;
import tukano.servers.rest.RestShortsServer;

public class BlobsClient {

    public static Blobs getClient() {
    	Discovery discovery = Discovery.getInstance();
        var res = discovery.knownUrisOf(RestBlobsServer.SERVICE, 1);
        var port = res[0].getPort();
        var hostname = res[0].getHost();

        String serverURI = String.format(RestBlobsServer.SERVER_URI_FMT, hostname, port);
        return new RestBlobsClient(URI.create(serverURI));
    }
}
