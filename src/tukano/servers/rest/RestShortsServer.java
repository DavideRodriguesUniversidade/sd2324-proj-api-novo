package tukano.servers.rest;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import tukano.discovery.Discovery;

public class RestShortsServer {

    private static Logger Log = Logger.getLogger(RestShortsServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static final int PORT = 4567;
    public static final String SERVICE = "shorts";
    private static final String SERVER_URI_FMT = "http://172.18.0.4:4567/rest";

    public static void main(String[] args) {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_URI_FMT, ip, PORT);

            // Create and start the server
            ResourceConfig config = new ResourceConfig();
            config.register(RestShortsResource.class); 

            JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config);

            Log.info(String.format("%s Server ready @ %s\n", SERVICE, serverURI));

            // Announce the service after starting the server
            try {
                Discovery.getInstance().announce(SERVICE, serverURI);
                Log.info(String.format("Service %s announced: %s\n", SERVICE, serverURI));
            } catch (Exception ex) {
                Log.severe(String.format("Service %s not announced: %s", SERVICE, serverURI));
                ex.printStackTrace();
            }

        } catch (Exception e) {
            Log.severe(e.getMessage());
            e.printStackTrace();
        }
    }
}
