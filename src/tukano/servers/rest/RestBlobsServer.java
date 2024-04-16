package tukano.servers.rest;

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import tukano.discovery.Discovery;

public class RestBlobsServer {

    private static Logger Log = Logger.getLogger(RestBlobsServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static final int PORT = 5678;
    public static final String SERVICE = "blobs";
    
    public static void main(String[] args) {
    	startServer("172.18.0.5");
        startServer("172.18.0.6");
    }
    
    private static void startServer(String ip) {
        try {
            String serverURI = String.format("http://%s:5678/rest", ip);

            // Create and start the server
            ResourceConfig config = new ResourceConfig();
            config.register(RestBlobsResource.class);  // Assuming RestBlobsResource is your resource class

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
