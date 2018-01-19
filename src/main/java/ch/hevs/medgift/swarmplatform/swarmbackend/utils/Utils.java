package ch.hevs.medgift.swarmplatform.swarmbackend.utils;

import java.util.AbstractMap;
import java.util.Map;

public class Utils {

    public static String translateLabelToKeyValue(int label){
        String transformedLabel = null;

        // CPU
        if(label == 1)
            transformedLabel = "node.type == highcpu";

        // GPU
        if(label==2)
            transformedLabel = "node.type == highram";
        
        if(label==3)
            transformedLabel = "node.type == gpu";

        return transformedLabel;
    }
}
