package JsonParser;

import java.util.ArrayList;
import java.util.Map;

public class JSONValue {

    private Object obj;
    private Type type;

    public JSONValue(Object obj) {
        this.obj = obj;
        setType();
    }

    private void setType() {
        if (obj instanceof String) type = Type.str;
        if (obj instanceof Number) type = Type.dou;
        if (obj instanceof Map) type = Type.map;
        if (obj instanceof ArrayList) type = Type.lis;
    }

    public Object getObj() {
        return obj;
    }

    public Type getType() {
        return type;
    }

    public double getDouble() {
        return (double) obj;
    }

    public String getStr() {
        return (String) obj;
    }

    public ArrayList<JSONValue> getList() {
        return (ArrayList<JSONValue>) obj;
    }

    public Map<String, JSONValue> getMap() {
        return (Map<String, JSONValue>) obj;
    }

    public boolean isString() {
        return type == Type.str;
    }

    public boolean isDouble() {
        return type == Type.dou;
    }

    public boolean isMap() {
        return type == Type.map;
    }

    public boolean isList() {
        return type == Type.lis;
    }

    @Override
    public String toString() {
        return switch (type) {
            case str -> getStr();
            case dou -> "" + getDouble();
            case map -> "" + getMap();
            case lis -> "" + getList();
        };
    }

    enum Type {
        str,
        dou,
        map,
        lis
    }
}
