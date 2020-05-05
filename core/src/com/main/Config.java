package com.main;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Config {
    public static final Map<String, Integer> objectCost;
    public static final Map<String, Integer> objectReward;
    public static final Map<String, Float> width;
    public static final Map<String, Float> height;
    public static final Map<String, Integer> damage;
    public static final Map<String, Float> range;
    public static final Map<String, Float> reloadTime;
    public static final Map<String, Integer> hp;
    public static final Map<String, Float> speed;
    public static final Map<String, String> representativeTexture;
    public static final Map<String, String> fullTexture;
    public static final Map<String, Boolean[][]> mapGrid;

    public static final int startingCoins = 5000;
    public static final float refreshRate = 2f;

    static boolean isUnit(String type) {
        return type == "firstUnit";
    }

    static {
        Map<String, Integer> tempObjectCost = new HashMap<>();
        Map<String, Integer> tempObjectReward = new HashMap<>();
        Map<String, Float> tempWidth = new HashMap<>();
        Map<String, Float> tempHeight = new HashMap<>();
        Map<String, Integer> tempDamage = new HashMap<>();
        Map<String, Float> tempRange = new HashMap<>();
        Map<String, Float> tempReloadTime = new HashMap<>();
        Map<String, Integer> tempHp = new HashMap<>();
        Map<String, Float> tempSpeed = new HashMap<>();
        Map<String, String> tempRepresentativeTexture = new HashMap<>();
        Map<String, String> tempFullTexture = new HashMap<>();
        Map<String, Boolean[][]> tempMapGrid = new HashMap<>();

        String type = "firstUnit";
        tempObjectCost.put(type, 300);
        tempObjectReward.put(type, 100);
        tempWidth.put(type, 64f);
        tempHeight.put(type, 64f);
        tempDamage.put(type, 5);
        tempRange.put(type, 300.0f);
        tempReloadTime.put(type, 1.0f);
        tempHp.put(type, 100);
        tempSpeed.put(type, 100.0f);
        tempRepresentativeTexture.put(type, "units/firstUnit/firstUnit00.png");
        tempFullTexture.put(type, "units/firstUnit/firstUnit");

        type = "firstTower";
        tempObjectCost.put(type, 400);
        tempObjectReward.put(type, 200);
        tempWidth.put(type, 64f);
        tempHeight.put(type, 64f);
        tempDamage.put(type, 10);
        tempRange.put(type, 300.0f);
        tempReloadTime.put(type, 2.0f);
        tempHp.put(type, 200);
        tempRepresentativeTexture.put(type, "towers/firstTower/firstTower0.png");
        tempFullTexture.put(type, "towers/firstTower/firstTower");

        type = "missile";
        tempSpeed.put(type, 300f);
        tempWidth.put(type, 8f);
        tempHeight.put(type, 8f);
        tempRepresentativeTexture.put(type, "missile.png");

        type = "map0";
        tempRepresentativeTexture.put(type, "maps/map0.png");
        Boolean[][] grid = {
                {false, false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false},
                {false, false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false},
                {false, false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false},
                {false, false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false},
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
                {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false},
                {false, false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false},
                {false, false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false},
                {false, false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false},
                {false, false, false, false, false, false, false, false, false, true, true, false, false, false, false, false, false, false, false, false}
        };
        tempMapGrid.put(type, grid);

        objectCost = Collections.unmodifiableMap(tempObjectCost);
        objectReward = Collections.unmodifiableMap(tempObjectReward);
        width = Collections.unmodifiableMap(tempWidth);
        height = Collections.unmodifiableMap(tempHeight);
        damage = Collections.unmodifiableMap(tempDamage);
        range = Collections.unmodifiableMap(tempRange);
        reloadTime = Collections.unmodifiableMap(tempReloadTime);
        hp = Collections.unmodifiableMap(tempHp);
        speed = Collections.unmodifiableMap(tempSpeed);
        representativeTexture = Collections.unmodifiableMap(tempRepresentativeTexture);
        fullTexture = Collections.unmodifiableMap(tempFullTexture);
        mapGrid = Collections.unmodifiableMap(tempMapGrid);
    }
}
