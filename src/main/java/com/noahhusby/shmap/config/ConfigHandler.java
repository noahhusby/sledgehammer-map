package com.noahhusby.shmap.config;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;

public class ConfigHandler {
    private static ConfigHandler mInstance = null;

    public static ConfigHandler getInstance() {
        if(mInstance == null) mInstance = new ConfigHandler();
        return mInstance;
    }

    public String host;
    public int port;
    public String auth;

    private ConfigHandler() {
        createConfig();
        getFieldsFromConfig();
    }

    private void getFieldsFromConfig() {
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader(new File(System.getProperty("user.dir"), "config.json"))) {
            JSONObject config = (JSONObject) jsonParser.parse(reader);
            host = (String) config.getOrDefault("host", "127.0.0.1");
            port = Integer.parseInt((String) config.getOrDefault("port", "7000"));
            auth = (String) config.getOrDefault("auth", "");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void createConfig() {
        File file = new File(System.getProperty("user.dir"), "config.json");

        if (!file.exists()) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.json")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
