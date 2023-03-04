package com.jaeger.findviewbyme.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jaeger
 * 15/11/25
 */
public class ViewPart {

    private boolean selected = true;
    private String type;
    private String typeFull;
    private String id;
    private String name;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        String[] packages = type.split("\\.");
        if (packages.length > 1) {
            this.typeFull = type;
            this.type = packages[packages.length - 1];
        } else {
            this.typeFull = null;
            this.type = type;
        }
    }

    public String getTypeFull() {
        return typeFull;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
//        generateName(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void resetName() {
        generateName(id);
    }

    public void addPrefixForName(String prefix) {
        if (prefix != null && !prefix.isEmpty()) {
            generateName(prefix + "_" + id);
        }
    }

    private void generateName(String id) {
        Pattern pattern = Pattern.compile("_([a-zA-Z])");
        Matcher matcher = pattern.matcher(id);

        char[] chars = id.toCharArray();
        while (matcher.find()) {
            int index = matcher.start(1);
            chars[index] = Character.toUpperCase(chars[index]);
        }
        String name = String.copyValueOf(chars);

        name = name.replaceAll("_", "");

        setName(name);
    }
}

