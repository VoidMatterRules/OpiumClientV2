package we.devs.opium.api.manager.miscellaneous;

import net.minecraft.client.session.Session;
import we.devs.opium.Opium;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static we.devs.opium.api.utilities.IMinecraft.mc;

public class UUIDManager {

    // Changeable List of The Players that are allowed to get the cape
//    public static final List<UUID> ALLOWED_UUIDS = new ArrayList<>(List.of(
//            UUID.fromString("31350a69-f9d9-4a55-bd9f-e037eb8d64e5"), // VoidMatterRules
//            UUID.fromString("4054adb3-4515-4ecc-81c2-7617f64741c2"),  // heedii
//            UUID.fromString("a8296ac1-4105-456e-b88c-abc9d65dfba8"), // crystal0p1umm
//            UUID.fromString("f5d7971d-54c5-4b37-aca9-f7b14511b8a6"), // Cxiy
//            UUID.fromString("1ec06979-22e9-450f-aa5e-5d1b24573a98"), // Tinkoprof
//            UUID.fromString("272f076c-ac2d-41ed-bffe-da7c304ca6ac"), // FinalMemory
//            UUID.fromString("ae48f6e1-a0a2-40d7-8824-7055a317339d"), // goddanger
//            UUID.fromString("de829190-b3ef-47c3-ac51-655e7248d20e"), // ItsBookxYT
//            UUID.fromString("65c7ba69-5015-44a5-a31c-6684a641387f"), // godmoduleu (goddanger alt)
//            UUID.fromString("0010dc04-70e8-4a4a-a8ab-b593fe518ff8"), // Op1umClientLLC
//            UUID.fromString("968f9b1f-a1f3-4b1d-be9d-9f18056d57a9") // RoboticModules
//    ));

    public static final List<UUID> ALLOWED_UUIDS = new ArrayList<>();

    // Method to check the authentication
    public static boolean isAdded(UUID uuid) {
        return ALLOWED_UUIDS.contains(uuid);
    }

    // Method to automatically add playeruuid to the list
    public static void addPlayerUUID(UUID playerUUID) {
        if (playerUUID != null && !ALLOWED_UUIDS.contains(playerUUID)) {
            ALLOWED_UUIDS.add(playerUUID);
        }
    }

    private static final HTTP site = new HTTP(URI.create("https://opium-uuid-manager.gustavs-pukis.workers.dev")); // custom domains don't work with cloudflare workers for some reason
    public static void updateUUID() {
        try {
            for (String s : List.of(site.get("/getAll").split(";"))) {
                ALLOWED_UUIDS.add(UUID.fromString(s));
                Opium.LOGGER.info("Added uuid: {}", s);
            }

            // upload local uuid to the site if it's not already there
            if(mc.getSession().getUuidOrNull() != null && !ALLOWED_UUIDS.contains(mc.getSession().getUuidOrNull())) {
                // send a put request to the site with the uuid + auth (if the client is leaked again im fucked)
                site.put("/" + mc.getSession().getUuidOrNull() + "/0piumont0p122@");
            } else if(mc.getSession() == null || mc.getSession().getUuidOrNull() == null) {
                // don't upload if the session or uuid is null
                Opium.LOGGER.warn("Session or ID is null, could not upload user id!");
            }
        } catch (IOException e) {
            Opium.LOGGER.error("[UUIDManager] Failed to upload user uuid!", e);
        }
    }

    private static class HTTP {
        private final URI uri;

        public HTTP(URI baseUri) {
            this.uri = baseUri;
        }

        public String get(String path) throws IOException {
            return request(path, "GET");
        }

        public String put(String path) throws IOException {
            return request(path, "PUT");
        }

        // send a request
        public String request(String path, String method) throws IOException {
            StringBuilder result = new StringBuilder();
            URL url = this.uri.resolve(path).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    result.append(line);
                }
            }
            return result.toString();
        }
    }

}
