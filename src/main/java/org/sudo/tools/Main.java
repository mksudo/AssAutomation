package org.sudo.tools;

import org.opencv.core.Core;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.logging.LogManager;

public class Main {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(Arrays.toString(args));

        if (args.length > 0 && args[0].equals("--debug")) {
            LogManager.getLogManager().readConfiguration(new FileInputStream("logging.properties"));
        }

        new GUIProvider();
    }
}