package ch.hevs.medgift.swarmplatform.swarmbackend.utils;

import java.util.AbstractMap;
import java.util.Map;

public class Utils {

    public static String translateLabelToKeyValue(String label){
        String transformedLabel = null;

        // CPU
        if(label.equalsIgnoreCase("highcpu"))
            transformedLabel = "node.labels.cpu == high";

        // GPU
        if(label.equalsIgnoreCase("highgpu"))
            transformedLabel = "node.labels.gpu == high";

        return transformedLabel;
    }
}
