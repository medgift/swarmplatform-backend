package ch.hevs.medgift.swarmplatform.swarmbackend.utils;

import java.util.AbstractMap;
import java.util.Map;

public class Utils {

    public static String translateLabelToKeyValue(int label){
        String transformedLabel = null;
        
        switch(label){
	        case 1 : transformedLabel = "node.labels.type == default";
	        	break;
	        case 2 : transformedLabel = "node.labels.type == highcpu";
	        	break;
	        case 3 : transformedLabel = "node.labels.type == highram";
	        	break;
	        case 4 : transformedLabel = "node.labels.type == gpu";
	        	break;
	        default : transformedLabel = "node.labels.type == default";
        }

        return transformedLabel;
    }
}

