package com.noahhusby.shmap;

import com.noahhusby.shmap.config.ConfigHandler;
import com.noahhusby.shmap.websocket.WebSocketHelper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.websocket.WsContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SledgehammerMap {
    private static Logger logger = LoggerFactory.getLogger(SledgehammerMap.class);
    private static WsContext wsContext;

    private static String title = "";
    private static String subtitle = "";

    private static double[] startingLocation = new double[]{0,0};
    private static long startingZoomLevel = 0;

    private static List<Waypoint> waypoints = new ArrayList<>();

    public static void main(String[] args) {
        ConfigHandler.getInstance();
        banner();

        Javalin app = Javalin.create(config -> {
            config.addStaticFiles("/public");
            config.showJavalinBanner = false;
        }).start(ConfigHandler.getInstance().host, ConfigHandler.getInstance().port);


        app.ws("/", wsHandler -> {
            wsHandler.onConnect(ctx -> {
                wsContext = ctx;
            });
            wsHandler.onMessage(ctx -> {
                handleMessage(ctx, ctx.message());
            });
        });

        app.get("/warp", ctx -> {
            JSONObject data = new JSONObject();
            String warp = ctx.queryParam("warp");
            String uuid = ctx.sessionAttribute("uuid");
            String key = ctx.sessionAttribute("key");
            if(!isAuthenticated(ctx)) return;
            data.put("uuid", uuid);
            data.put("key", key);
            data.put("warp", warp);
            wsContext.send(WebSocketHelper.generateAction("warp", data));
        });

        app.get("/session", ctx -> {
            ctx.sessionAttribute("uuid", ctx.queryParam("uuid"));
            ctx.sessionAttribute("key", ctx.queryParam("key"));
            ctx.html("<!DOCTYPE html> <html> <body> <script> window.location.replace(\"/\"); </script> </body> </html>");
        });

        app.get("/data", ctx -> {
            JSONObject data = new JSONObject();
            data.put("title", title);
            data.put("subtitle", subtitle);
            data.put("startingZoom", startingZoomLevel);
            data.put("startingLocation", startingLocation);
            JSONObject w = new JSONObject();
            w.put("type", "FeatureCollection");

            JSONArray features = new JSONArray();
            for(int x = 0; x < waypoints.size(); x++) {
                Waypoint wa = waypoints.get(x);
                wa.auth = isAuthenticated(ctx);
                features.add(wa.getJsonObject());
            }

            w.put("features", features);

            data.put("waypoints", w);
            ctx.json(data);
        });
    }

    public static void handleMessage(WsContext ws, String message) {
        try {
            JSONObject o = (JSONObject) new JSONParser().parse(message);
            if(o == null) {
                logger.warn("Unknown packet received from websockets!");
                return;
            } else {
                JSONObject data = (JSONObject) o.get("data");

                String auth = (String) data.get("auth");
                if(!auth.trim().equals(ConfigHandler.getInstance().auth)) return;

                String action = (String) o.get("action");
                if(action.equals("init")) {
                    title = (String) data.get("title");
                    subtitle = (String) data.get("subtitle");
                    double lat = (double) data.get("lat");
                    double lon = (double) data.get("lon");
                    startingLocation = new double[]{lat, lon};
                    startingZoomLevel = (long) data.get("zoomLevel");

                    JSONObject response = new JSONObject();
                    response.put("action", "init");
                    response.put("state", "success");
                    logger.info("Successfully received initialization data!");
                    ws.send(response);
                } else if(action.equals("warp_refresh")) {
                    JSONArray waypoints_data = (JSONArray) new JSONParser().parse((String) data.get("waypoints"));
                    List<Waypoint> temp = new ArrayList<>();
                    for(int x = 0; x < waypoints_data.size(); x++) {
                        JSONObject waypoint_data = (JSONObject) waypoints_data.get(x);

                        String name = (String) waypoint_data.get("name");
                        String info = (String) waypoint_data.get("info");
                        double lon_ = (double) waypoint_data.get("lon");
                        double lat_ = (double) waypoint_data.get("lat");

                        Waypoint waypoint = new Waypoint();
                        waypoint.location = new double[]{lon_, lat_};
                        waypoint.name = name;
                        waypoint.info = info;
                        temp.add(waypoint);
                    }

                    waypoints = temp;
                } else if(action.equals("alive")) {
                    data = new JSONObject();
                    data.put("state", "success");
                    ws.send(WebSocketHelper.generateAction("alive", data));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static boolean isAuthenticated(Context ctx) {
        String uuid = ctx.sessionAttribute("uuid");
        String key = ctx.sessionAttribute("key");
        if(uuid == null || key == null) {
            return false;
        }

        return key.length() == 36;
    }

    public static void banner() {
        System.out.println("  ____  _   _       __  __             \n" +
                " / ___|| | | |     |  \\/  | __ _ _ __  \n" +
                " \\___ \\| |_| |_____| |\\/| |/ _` | '_ \\ \n" +
                "  ___) |  _  |_____| |  | | (_| | |_) |\n" +
                " |____/|_| |_|     |_|  |_|\\__,_| .__/ \n" +
                "                                |_|    ");
        System.out.println("---------------------------------------------");
        System.out.println("Sledgehammer-Map v"+Constants.version);
        System.out.println("Listening on: "+"http://"+ConfigHandler.getInstance().host+":"+ConfigHandler.getInstance().port);
        System.out.println("---------------------------------------------\n");
    }
}
