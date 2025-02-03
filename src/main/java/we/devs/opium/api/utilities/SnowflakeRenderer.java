package we.devs.opium.api.utilities;

import net.minecraft.client.gui.DrawContext;
import java.util.*;

public class SnowflakeRenderer {

    private static final Random RANDOM = new Random();
    private final List<Map<String, Object>> snowflakes = new ArrayList<>();
    private static final int INITIAL_SNOWFLAKE_COUNT = 100;

    public void initializeSnowflakes(int screenWidth, int screenHeight) {
        snowflakes.clear();
        for (int i = 0; i < INITIAL_SNOWFLAKE_COUNT; i++) {
            snowflakes.add(createSnowflake(screenWidth, screenHeight));
        }
    }

    private Map<String, Object> createSnowflake(int screenWidth, int screenHeight) {
        Map<String, Object> snowflake = new HashMap<>();
        snowflake.put("x", (float) RANDOM.nextInt(screenWidth));
        snowflake.put("y", (float) -RANDOM.nextInt(50));
        snowflake.put("speedY", 0.5F + RANDOM.nextFloat() * 2);
        snowflake.put("size", RANDOM.nextInt(3) + 2);
        snowflake.put("rotationAngle", RANDOM.nextFloat() * 360);
        snowflake.put("rotationSpeed", RANDOM.nextFloat() * 2 - 1);
        snowflake.put("opacity", 1.0F);
        return snowflake;
    }

    public void resizeSnowflakesIfNecessary(int screenWidth) {
        int targetSnowflakeCount = screenWidth / 10;
        if (snowflakes.size() < targetSnowflakeCount) {
            for (int i = snowflakes.size(); i < targetSnowflakeCount; i++) {
                snowflakes.add(createSnowflake(screenWidth, getScreenHeight()));
            }
        } else if (snowflakes.size() > targetSnowflakeCount) {
            snowflakes.subList(targetSnowflakeCount, snowflakes.size()).clear();
        }
    }

    private void updateSnowflake(Map<String, Object> snowflake, int screenWidth, int screenHeight) {
        float y = (float) snowflake.get("y") + (float) snowflake.get("speedY");
        if (y > screenHeight || (float) snowflake.get("opacity") <= 0) {
            snowflake.clear();
            snowflake.putAll(createSnowflake(screenWidth, screenHeight));
        } else {
            snowflake.put("y", y);
            snowflake.put("rotationAngle", (float) snowflake.get("rotationAngle") + (float) snowflake.get("rotationSpeed"));
        }
    }

    public void renderSnowflakes(DrawContext context, int screenWidth, int screenHeight) {
        for (Map<String, Object> snowflake : snowflakes) {
            updateSnowflake(snowflake, screenWidth, screenHeight);
            drawSnowflake(snowflake, context);
        }
    }

    private void drawSnowflake(Map<String, Object> snowflake, DrawContext context) {
        int alpha = (int) ((float) snowflake.get("opacity") * 0x80);
        int size = (int) snowflake.get("size");
        int color = (alpha << 24) | 0xFFFFFF;
        float centerX = (float) snowflake.get("x") + size / 2f;
        float centerY = (float) snowflake.get("y") + size / 2f;

        for (int i = 0; i < 4; i++) {
            double angle = Math.toRadians((float) snowflake.get("rotationAngle") + (i * 90));
            float armLength = size / 2f;
            float endX = centerX + (float) (armLength * Math.cos(angle));
            float endY = centerY + (float) (armLength * Math.sin(angle));
            context.fill((int) centerX, (int) centerY, (int) endX, (int) endY, color);
        }
    }

    private int getScreenHeight() {
        return net.minecraft.client.MinecraftClient.getInstance().getWindow().getScaledHeight();
    }
}
