package com.gk.apps.java.firebaseadmin;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException;
import com.google.firebase.remoteconfig.Template;

import java.io.InputStream;

public class Main {

    /**
     *
     * Command Syntax: command arg1 arg2 ...
     * Commands:
     *      -copy-remote-conf sourceProjectId targetProjectId
     *
     * @param args script arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            logError("No args passed! Aborting...");
        } else {
            Main main = new Main();
            logVerbose("arg list:\n");
            for (String a : args) {
                logVerbose(a);
            }

            String command = args[0];
            switch (command) {
                case "-copy-remote-conf":
                    if (args.length == 3) {
                        main.copyRemoteConf(args[1], args[2]);
                    }
                    break;

                // add more commands here.

                default:
                    logError("Unknown command: " + command);
                    break;
            }
        }
    }

    private static void logVerbose(String msg) {
        System.out.println("" + msg);
    }

    private static void logError(String msg) {
        System.err.println("" + msg);
    }

    private static void logException(Exception e) {
        e.printStackTrace();
    }

    private void copyRemoteConf(String sourceProjectId, String targetProjectId) {
        logVerbose("Copying remote conf from " + sourceProjectId + " to " + targetProjectId + " ...");
        initProject(sourceProjectId);

        Template sourceTemplate = getRemoteConfTemplate(sourceProjectId);
        if (sourceTemplate != null && sourceTemplate.getVersion() != null) {
            initProject(targetProjectId);
            FirebaseApp targetApp = FirebaseApp.getInstance(targetProjectId);
            FirebaseRemoteConfig targetRemoteConf = FirebaseRemoteConfig.getInstance(targetApp);

            try {
                Template targetTemplate = targetRemoteConf.getTemplate();

                logVerbose("Source template eTag: " + sourceTemplate.getETag());
                logVerbose("Target template eTag: " + targetTemplate.getETag());

                logVerbose("Updating target template from source template...");
                targetTemplate.setConditions(sourceTemplate.getConditions());
                targetTemplate.setParameterGroups(sourceTemplate.getParameterGroups());
                targetTemplate.setParameters(sourceTemplate.getParameters());
                targetTemplate.setVersion(sourceTemplate.getVersion());

                logVerbose("Publishing target template...");
                targetRemoteConf.publishTemplate(targetTemplate);

            } catch (FirebaseRemoteConfigException e) {
                logException(e);
            }

        } else {
            logError("Source project has no template !");
        }
    }

    private void initProject(String projectId) {
        logVerbose("Initializing project " + projectId + " ...");
        try {
            String resourcePath = "/json/" + projectId + ".json";
            logVerbose("Loading resource path:" + resourcePath + " ...");
            InputStream stream = getClass().getResourceAsStream(resourcePath);
            if (stream != null) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();

                FirebaseApp app = FirebaseApp.initializeApp(options, projectId);
                logVerbose(app.getName() + " successfully initialized!\n");

            } else {
                logError("Resource " + resourcePath + " not found!");
            }
        } catch (Exception e) {
            logError(e.toString());
        }
    }

    private Template getRemoteConfTemplate(String projectId){
        try {
            FirebaseApp app = FirebaseApp.getInstance(projectId);
            FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance(app);
            return remoteConfig.getTemplate();
        } catch (Exception e) {
            logError(e.toString());
        }

        return null;
    }

}
