package we.devs.opium.api.manager.miscellaneous;

import com.google.gson.*;
import we.devs.opium.Opium;
import we.devs.opium.api.manager.element.Element;
import we.devs.opium.api.manager.friend.Friend;
import we.devs.opium.api.manager.module.Module;
import we.devs.opium.client.values.Value;
import we.devs.opium.client.values.impl.*;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public class ConfigManager {
    public static final String CONFIG_DIRECTORY = "Opium/Configs/";


    public void load(String name) {
        try {
            this.loadConfig(name);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void delete(String name) {
        try {
            Files.deleteIfExists(Paths.get(CONFIG_DIRECTORY, name + ".json"));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public ArrayList<Object> getAvailableConfigs() {
        ArrayList<Object> configNames = new ArrayList<>();

        // Define the directory where configs are stored
        File configDirectory = new File(CONFIG_DIRECTORY);

        // Check if the directory exists and is a directory
        if (configDirectory.exists() && configDirectory.isDirectory()) {
            // Get all files in the directory
            File[] files = configDirectory.listFiles((dir, name) -> name.endsWith(".json"));

            if (files != null) {
                // Loop through all files and add their names (without the ".json" extension)
                for (File file : files) {
                    String fileName = file.getName();
                    // Remove the ".json" extension
                    String configName = fileName.substring(0, fileName.lastIndexOf("."));
                    configNames.add(configName);
                }
            }
        } else {
            Opium.LOGGER.error("Config directory does not exist: " + CONFIG_DIRECTORY);
        }

        return configNames;
    }

    boolean saving = false;
    public void save(String name) {
        if(saving) return;
        saving = true;
        try {
            Path path = Path.of(CONFIG_DIRECTORY);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                Opium.LOGGER.atInfo().log("Created Opium directory");
            }
            this.saveConfig(name);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        saving = false;
    }


    private Optional<Module> findModuleByName(String moduleName) {
        // Directly call stream() on the List
        return Opium.MODULE_MANAGER.getModules()
                .stream()
                .filter(module -> module.getName().equalsIgnoreCase(moduleName))
                .findFirst();
    }

    public void saveConfig(String configName) throws IOException {
        if (configName == null || configName.isEmpty()) {
            throw new IllegalArgumentException("Config name cannot be null or empty!");
        }

        // Ensure the config directory exists
        Files.createDirectories(Paths.get(CONFIG_DIRECTORY));

        // Construct the file path
        Path configFilePath = Paths.get(CONFIG_DIRECTORY, configName + ".json");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject rootJson = new JsonObject();

        // Save Modules
        JsonObject modulesJson = new JsonObject();
        for (Module module : Opium.MODULE_MANAGER.getModules()) {
            JsonObject moduleJson = new JsonObject();
            JsonObject valueJson = new JsonObject();

            moduleJson.addProperty("Name", module.getName());
            moduleJson.addProperty("Status", module.isToggled());
            this.saveValues(valueJson, module.getValues());  // Using your existing saveValues
            moduleJson.add("Values", valueJson);

            modulesJson.add(module.getName(), moduleJson);
        }
        rootJson.add("Modules", modulesJson);

        // Save Elements
        JsonObject elementsJson = new JsonObject();
        for (Element element : Opium.ELEMENT_MANAGER.getElements()) {
            JsonObject elementJson = new JsonObject();
            JsonObject valueJson = new JsonObject();
            JsonObject positionJson = new JsonObject();

            elementJson.addProperty("Name", element.getName());
            elementJson.addProperty("Status", element.isToggled());
            this.saveValues(valueJson, element.getValues());  // Using your existing saveValues

            positionJson.addProperty("X", element.frame.getX());
            positionJson.addProperty("Y", element.frame.getY());
            elementJson.add("Values", valueJson);
            elementJson.add("Positions", positionJson);

            elementsJson.add(element.getName(), elementJson);
        }
        rootJson.add("Elements", elementsJson);

        // Save Client Data
        JsonObject clientJson = new JsonObject();
        clientJson.addProperty("Prefix", Opium.COMMAND_MANAGER.getPrefix());

        JsonArray friendArray = new JsonArray();
        for (Friend friend : Opium.FRIEND_MANAGER.getFriends()) {
            friendArray.add(friend.getName());
        }
        clientJson.add("Friends", friendArray);

        rootJson.add("Client", clientJson);

        // Write to the file
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(configFilePath), StandardCharsets.UTF_8)) {
            writer.write(gson.toJson(rootJson));
        }

        Opium.LOGGER.info("Config saved as: " + configName);
    }


    public void loadConfig(String configName) throws IOException {
        if (configName == null || configName.isEmpty()) {
            throw new IllegalArgumentException("Config name cannot be null or empty!");
        }

        Path configFilePath = Paths.get(CONFIG_DIRECTORY, configName + ".json");

        if (!Files.exists(configFilePath)) {
            Opium.LOGGER.error("Config not found: " + configName);
            return;
        }

        JsonObject rootJson;
        try (InputStream stream = Files.newInputStream(configFilePath);
             InputStreamReader reader = new InputStreamReader(stream)) {
            rootJson = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IllegalStateException | JsonSyntaxException exception) {
            exception.printStackTrace();
            Opium.LOGGER.error("Failed to load config: " + configName);
            return;
        }

        // Load Modules
        if (rootJson.has("Modules")) {
            JsonObject modulesJson = rootJson.getAsJsonObject("Modules");
            for (Map.Entry<String, JsonElement> entry : modulesJson.entrySet()) {
                String moduleName = entry.getKey();
                JsonObject moduleJson = entry.getValue().getAsJsonObject();

                Optional<Module> optionalModule = findModuleByName(moduleName);
                if (optionalModule.isPresent()) {
                    Module module = optionalModule.get();
                    if (moduleJson.has("Status")) {
                        boolean status = moduleJson.get("Status").getAsBoolean();
                        if (status) {
                            module.enable(false);
                        } else {
                            module.disable(false);
                        }
                    }
                    if (moduleJson.has("Values")) {
                        JsonObject valueJson = moduleJson.getAsJsonObject("Values");
                        this.loadValues(valueJson, module.getValues());  // Using your existing loadValues
                    }
                }
            }
        }

        // Load Elements
        if (rootJson.has("Elements")) {
            JsonObject elementsJson = rootJson.getAsJsonObject("Elements");
            for (Map.Entry<String, JsonElement> entry : elementsJson.entrySet()) {
                String elementName = entry.getKey();
                JsonObject elementJson = entry.getValue().getAsJsonObject();

                Optional<Element> optionalElement = findElementByName(elementName);
                if (optionalElement.isPresent()) {
                    Element element = optionalElement.get();
                    if (elementJson.has("Status")) {
                        boolean status = elementJson.get("Status").getAsBoolean();
                        if (status) {
                            element.enable(false);
                        } else {
                            element.disable(false);
                        }
                    }
                    if (elementJson.has("Values")) {
                        JsonObject valueJson = elementJson.getAsJsonObject("Values");
                        this.loadValues(valueJson, element.getValues());  // Using your existing loadValues
                    }
                    if (elementJson.has("Positions")) {
                        JsonObject positionJson = elementJson.getAsJsonObject("Positions");
                        if (positionJson.has("X") && positionJson.has("Y")) {
                            element.frame.setX(positionJson.get("X").getAsFloat());
                            element.frame.setY(positionJson.get("Y").getAsFloat());
                        }
                    }
                }
            }
        }

        // Load Client Data
        if (rootJson.has("Client")) {
            JsonObject clientJson = rootJson.getAsJsonObject("Client");

            // Load Prefix
            if (clientJson.has("Prefix")) {
                Opium.COMMAND_MANAGER.setPrefix(clientJson.get("Prefix").getAsString());
            }

            // Load Friends
            if (clientJson.has("Friends")) {
                JsonArray friendArray = clientJson.getAsJsonArray("Friends");
                friendArray.forEach(friend -> Opium.FRIEND_MANAGER.addFriend(friend.getAsString()));
            }
        }

        Opium.LOGGER.info("Config loaded: " + configName);
    }



    private Optional<Element> findElementByName(String elementName) {
        // Directly use the stream() method if getElements() returns a List or ArrayList
        return Opium.ELEMENT_MANAGER.getElements()
                .stream()
                .filter(element -> element.getName().equalsIgnoreCase(elementName))
                .findFirst();
    }


    private void loadValues(JsonObject valueJson, ArrayList<Value> values) {
        for (Value value : values) {
            JsonElement dataObject = valueJson.get(value.getName());
            if (dataObject == null || !dataObject.isJsonPrimitive()) continue;
            if (value instanceof ValueBoolean) {
                ((ValueBoolean) value).setValue(dataObject.getAsBoolean());
                continue;
            }
            if (value instanceof ValueNumber) {
                if (((ValueNumber) value).getType() == 1) {
                    ((ValueNumber) value).setValue(dataObject.getAsInt());
                    continue;
                }
                if (((ValueNumber) value).getType() == 2) {
                    ((ValueNumber) value).setValue(dataObject.getAsDouble());
                    continue;
                }
                if (((ValueNumber) value).getType() != 3) continue;
                ((ValueNumber) value).setValue(Float.valueOf(dataObject.getAsFloat()));
                continue;
            }
            if (value instanceof ValueEnum) {
                ((ValueEnum) value).setValue(((ValueEnum) value).getEnum(dataObject.getAsString()));
                continue;
            }
            if (value instanceof ValueString) {
                ((ValueString) value).setValue(dataObject.getAsString());
                continue;
            }
            if (value instanceof ValueColor) {
                ((ValueColor) value).setValue(new Color(dataObject.getAsInt()));
                if (valueJson.get(value.getName() + "-Rainbow") != null) {
                    ((ValueColor) value).setRainbow(valueJson.get(value.getName() + "-Rainbow").getAsBoolean());
                }
                if (valueJson.get(value.getName() + "-Alpha") != null) {
                    ((ValueColor) value).setValue(new Color(((ValueColor) value).getValue().getRed(), ((ValueColor) value).getValue().getGreen(), ((ValueColor) value).getValue().getBlue(), valueJson.get(value.getName() + "-Alpha").getAsInt()));
                }
                if (valueJson.get(value.getName() + "-Sync") == null) continue;
                ((ValueColor) value).setSync(valueJson.get(value.getName() + "-Sync").getAsBoolean());
                continue;
            }
            if (!(value instanceof ValueBind)) continue;
            ((ValueBind) value).setValue(dataObject.getAsInt());
        }
    }

    private void saveValues(JsonObject valueJson, ArrayList<Value> values) {
        for (Value value : values) {
            if (value instanceof ValueBoolean) {
                valueJson.add(value.getName(), new JsonPrimitive(((ValueBoolean) value).getValue()));
                continue;
            }
            if (value instanceof ValueNumber) {
                valueJson.add(value.getName(), new JsonPrimitive(((ValueNumber) value).getValue()));
                continue;
            }
            if (value instanceof ValueEnum) {
                valueJson.add(value.getName(), new JsonPrimitive(((ValueEnum) value).getValue().name()));
                continue;
            }
            if (value instanceof ValueString) {
                valueJson.add(value.getName(), new JsonPrimitive(((ValueString) value).getValue()));
                continue;
            }
            if (value instanceof ValueColor) {
                valueJson.add(value.getName(), new JsonPrimitive(((ValueColor) value).getValue().getRGB()));
                valueJson.add(value.getName() + "-Alpha", new JsonPrimitive(((ValueColor) value).getValue().getAlpha()));
                valueJson.add(value.getName() + "-Rainbow", new JsonPrimitive(((ValueColor) value).isRainbow()));
                valueJson.add(value.getName() + "-Sync", new JsonPrimitive(((ValueColor) value).isSync()));
                continue;
            }
            if (!(value instanceof ValueBind)) continue;
            valueJson.add(value.getName(), new JsonPrimitive(((ValueBind) value).getValue()));
        }
    }

}
