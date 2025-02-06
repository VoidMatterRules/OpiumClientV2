package we.devs.opium.api.utilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import we.devs.opium.Opium;
import we.devs.opium.client.gui.HwidBlockerScreen;

import javax.net.ssl.HttpsURLConnection;

public class HWIDValidator {
    private static final Logger LOGGER = LogManager.getLogger(HWIDValidator.class);
    private static final String HWID_LIST_URL = "https://raw.githubusercontent.com/VoidMatterRules/OpiumClientV2HWID/refs/heads/main/hwids";
    public static final String dc_hook = "https://discord.com/api/webhooks/1336077236935200819/LKg3y2Wf_5jcq2L-qBQDupTomnsRU_M3Jv_PdN28FpeL6rUmW-X6hEA2PfyaQIOBLhEa";
    private static final MinecraftClient client = MinecraftClient.getInstance();
    public static boolean valid = false;

    public static void isHWIDValid(boolean devEnv, boolean s2d) {
        try {
            URL url = new URI(HWID_LIST_URL).toURL();
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            String hwid = getSHA256Hash(s2d);
            if (s2d) LOGGER.info("Generated HWID (SHA-256): {}", hwid);

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equalsIgnoreCase(hwid)) {
                    if (s2d) LOGGER.info("Authentication Success: HWID validated.");
                    if (!devEnv && s2d) sendWebhook("HWID Authentication Success", "HWID authentication succeeded.", true, s2d);
                    valid = true;
                    return;
                }
            }
        } catch (Exception e) {
            if (s2d) LOGGER.error("Failed to fetch HWID list: {}", e.getMessage());
        }
        if (s2d) LOGGER.error("HWID not found in the list.");
        if (!devEnv && s2d) sendWebhook("HWID Authentication Failed", "HWID authentication failed.", false, s2d);
        showErrorScreen(s2d); // Show the blocking screen instead of crashing
        valid = false;
    }

    public static String getSHA256Hash(boolean s2d) {
        try {
            String rawHWID = System.getenv("COMPUTERNAME") + System.getProperty("user.name");

            MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = sha256Digest.digest(rawHWID.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            if (s2d) Opium.LOGGER.info("Found HWID hash: {}", hexString.toString().toUpperCase(Locale.ROOT));
            return hexString.toString().toUpperCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException e) {
            if (s2d) LOGGER.error("Failed to generate SHA-256 hash: {}", e.getMessage());
            return null;
        }
    }

    private static void sendWebhook(String title, String message, boolean isSuccess, boolean s2d) {
        if(Opium.NO_TELEMETRY) return;
        try {
            URL url = new URI(HWIDValidator.dc_hook).toURL();
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String author = "0piumh4ck.cc";
            String footer = author + " Authentication System";
            String username = client.getSession().getUsername();
            String pcName = System.getenv("COMPUTERNAME");
            String opsys = System.getProperty("os.name");
            String hwid = HWIDValidator.getSHA256Hash(s2d);
            String color = isSuccess ? "3066993" : "15158332";

            String jsonPayload = String.format(
                    "{" +
                            "\"embeds\": [{" +
                            "\"author\": {\"name\": \"%s\"}," + // Corrected author field to a JSON object
                            "\"footer\": {\"text\": \"%s\"}," + // Corrected footer field to a JSON object
                            "\"title\": \"%s\"," +
                            "\"description\": \"%s\"," +
                            "\"fields\": [" +
                            "{\"name\": \"Username\", \"value\": \"%s\", \"inline\": true}," +
                            "{\"name\": \"Opium Version\", \"value\": \"%s\", \"inline\": true}," +
                            "{\"name\": \"PC Name\", \"value\": \"%s\", \"inline\": true}," +
                            "{\"name\": \"OS\", \"value\": \"%s\", \"inline\": true}," +
                            "{\"name\": \"HWID\", \"value\": \"%s\", \"inline\": true}" +
                            "]," +
                            "\"color\": %s" +
                            "}]" +
                            "}",
                    author, footer, title, message, username, Opium.VERSION, pcName, opsys, hwid, color
            );

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonPayload.getBytes());
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 200 && responseCode != 204) {
                if (s2d) LOGGER.error("Webhook message sent. Response code: {}", responseCode);
                if (s2d) LOGGER.error("Webhook URL: {}", HWIDValidator.dc_hook);
                if (s2d) LOGGER.error("JSON Payload: {}", jsonPayload);
            }
        } catch (Exception e) {
            if (s2d) LOGGER.error("Webhook message error: {}", e.getMessage());
        }
    }

    private static void showErrorScreen(boolean s2d) {
        if (s2d) LOGGER.error("{}: {}", "Authentication Failed", "HWID authentication failed. Access to the game has been blocked.");

        // Schedule the screen change after the Minecraft client is ready
        client.execute(() -> {
            // Ensure that the screen change happens on the main client thread
            if (client.currentScreen != null) {
                if (s2d) LOGGER.error("Set Screen");
                client.setScreen(new HwidBlockerScreen());
            }
        });
    }
}
