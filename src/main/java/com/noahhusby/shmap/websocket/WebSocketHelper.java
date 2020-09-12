package com.noahhusby.shmap.websocket;

import org.json.simple.JSONObject;

public class WebSocketHelper {
    public static JSONObject generateAction(String action, JSONObject data) {
        JSONObject o = new JSONObject();
        o.put("action", action);
        o.put("data", data);
        return o;
    }
}
