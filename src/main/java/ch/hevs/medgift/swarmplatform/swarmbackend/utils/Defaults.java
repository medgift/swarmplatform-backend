package ch.hevs.medgift.swarmplatform.swarmbackend.utils;

public class Defaults {

	/*
	 * REST endpoint related 
	 */
    public static final int NB_REPLICAS = 1;
    public static final String DATASET_MOUNT_TARGET = "/dataset";
    
    
    /*
     * Websocket related
     */
    public static final String SOCKET_ENDPOINT = "/socket";
    public static final String LOGS_BROKER = "/logs";
    public static final String LOGS_BROKER_APP_DEST_PREFIX = "/dockerservice";
    public static final String LOGS_MESSAGE_MAPPING_PREFIX = "/log";
}
