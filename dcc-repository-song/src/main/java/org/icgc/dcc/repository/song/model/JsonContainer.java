package org.icgc.dcc.repository.song.model;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class JsonContainer {
   private final JsonNode json;
   JsonContainer(JsonNode json) {
       assert json.isObject();
       this.json=json;
   }

    String get(String key) {
        return json.at("/"+key).asText();
    }

    long getLong(String key) {
        return json.at("/" + key).asLong(0);
    }

    JsonNode from(String key) {
        return json.at("/"+key);
    }

    boolean getBoolean(String key) { return json.at("/"+key).asBoolean();}

    public JsonNode getInfo() {
       return from("info");
    }

    @Override
    public String toString() {
        return json.toString();
    }
}
