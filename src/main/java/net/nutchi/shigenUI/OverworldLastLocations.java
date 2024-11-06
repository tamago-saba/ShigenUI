package net.nutchi.shigenUI;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class OverworldLastLocations {
    private final Logger logger;
    private final File parentFolder;

    private final Gson gson = new Gson();
    private final Map<UUID, Coordinate> playerCoordinates = new HashMap<>();

    public Optional<Coordinate> getCoordinate(UUID player) {
        return Optional.ofNullable(playerCoordinates.get(player));
    }

    public void setCoordinate(UUID player, Coordinate coordinate) {
        playerCoordinates.put(player, coordinate);
    }

    public void clearCoordinate(UUID player) {
        playerCoordinates.remove(player);
    }

    public void load() {
        if (getJsonFile().exists()) {
            try (Reader reader = new FileReader(getJsonFile())) {
                playerCoordinates.clear();

                Type type = new TypeToken<Map<UUID, Coordinate>>() {
                }.getType();
                playerCoordinates.putAll(gson.fromJson(reader, type));
            } catch (IOException e) {
                logger.warning("Failed to load location history");
            }
        }
    }

    public void save() {
        parentFolder.mkdir();

        try (Writer writer = new FileWriter(getJsonFile())) {
            gson.toJson(playerCoordinates, writer);
        } catch (IOException e) {
            logger.warning("Failed to save location history");
        }
    }

    private File getJsonFile() {
        return new File(parentFolder, "overworld-last-locations.json");
    }
}
