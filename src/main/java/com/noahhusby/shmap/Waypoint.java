package com.noahhusby.shmap;

import org.json.simple.JSONObject;

public class Waypoint {
    public String name = "";
    public String info = "";
    public double[] location;
    public boolean auth = false;

    public Waypoint() {}

    public JSONObject getJsonObject() {
        JSONObject o = new JSONObject();
        o.put("type", "Feature");

        JSONObject geometry = new JSONObject();
        geometry.put("type", "Point");
        geometry.put("coordinates", location);

        o.put("geometry", geometry);

        JSONObject properties = new JSONObject();
        properties.put("name", name);
        properties.put("info", info);
        properties.put("auth", auth);

        o.put("properties", properties);
        return o;
    }
}
