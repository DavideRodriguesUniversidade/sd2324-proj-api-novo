package tukano.api.clients;

import java.net.URI;

import tukano.api.java.Users;
import tukano.clients.rest.RestUsersClient;
import tukano.discovery.Discovery;
import tukano.servers.rest.RestUsersServer;

public class UsersClient {

	 public static RestUsersClient getClient() {
	        Discovery discovery = Discovery.getInstance();
	        var res = discovery.knownUrisOf(RestUsersServer.SERVICE, 1);
	        var port = res[0].getPort();
	        var hostname = res[0].getHost();

	        String serverURI = String.format(RestUsersServer.SERVER_URI_FMT, hostname, port);
	        return new RestUsersClient(URI.create(serverURI));
	    }
}
