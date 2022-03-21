package JsonParser;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONParser {

    public static Map<String, JSONValue> read(Path path) {
        try {
            return read(path.toUri().toURL());
        } catch (IOException ignored) {
            throw new IllegalArgumentException("File does not exist!");
        }
    }

    public static Map<String, JSONValue> read(URL url) {
        Map<String, JSONValue> m = new HashMap<>();
        BufferedReader in;
        try {
            File f = new File(String.valueOf(Paths.get(url.toURI())));
            in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            while (true) {
                String s = in.readLine();
                if (s == null) break;
                s = s.trim();
                boolean isBreak = false;
                if (s.startsWith("//")) {
                    continue;
                }
                for (int i = 0; i < s.length(); i++) {
                    if (s.charAt(i) == '{') {
                        m = getJSONValue(s.substring(i + 1), in);
                    }
                    if (s.charAt(i) == '}') isBreak = true;
                }
                if (isBreak) break;
            }
        } catch (IOException | URISyntaxException e) {
            throw new IllegalArgumentException("File does not exist");
        }
        return m;
    }

    private static Map<String, JSONValue> getJSONValue(String s, BufferedReader in) throws IOException {
        Map<String, JSONValue> m = new HashMap<>();
        Mode mode = Mode.end;
        StringBuilder curKey = new StringBuilder();
        StringBuilder douStrVal = new StringBuilder();
        boolean isSlash = false;
        while (true) {
            boolean isBreak = false;
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if (ch == '}') {
                    isBreak = true;
                    break;
                } else {
                    switch (mode) {
                        case end:
                            if (ch == '\"') {
                                curKey = new StringBuilder();
                                mode = Mode.key;
                            }
                            break;
                        case key:
                            if (isSlash) {
                                curKey.append(ch);
                                isSlash = false;
                            } else if (ch == '\\') {
                                isSlash = true;
                            } else if (ch != '\"') {
                                curKey.append(ch);
                            } else {
                                mode = Mode.afterKey;
                            }
                            break;
                        case afterKey:
                            if (ch == ':') {
                                mode = Mode.beforeVal;
                            }
                            break;
                        case beforeVal:
                            if (Character.isDigit(ch)) {
                                douStrVal = new StringBuilder("" + ch);
                                mode = Mode.intValue;
                            } else if (ch == '\"') {
                                douStrVal = new StringBuilder();
                                mode = Mode.strValue;
                            } else if (ch == '{') {
                                m.put(curKey.toString(), new JSONValue(getJSONValue(s.substring(i + 1), in)));
                                curKey = new StringBuilder();
                                mode = Mode.end;
                            } else if (ch == '[') {
                                m.put(curKey.toString(), new JSONValue(getList(s.substring(i + 1), in)));
                                curKey = new StringBuilder();
                                mode = Mode.end;
                            }

                            break;
                        case intValue:
                            if (ch == ' ' || ch == ',') {
                                mode = Mode.end;
                                m.put(curKey.toString(), new JSONValue(getDoubleVal(douStrVal.toString())));
                                curKey = new StringBuilder();
                                douStrVal = new StringBuilder();
                                break;
                            }
                            douStrVal.append(ch);
                            break;
                        case strValue:
                            if (isSlash) {
                                douStrVal.append(ch);
                                isSlash = false;
                            } else if (ch == '\\') {
                                isSlash = true;
                            } else if (ch != '\"') {
                                douStrVal.append(ch);
                            } else {
                                m.put(curKey.toString(), new JSONValue(douStrVal.toString()));
                                curKey = new StringBuilder();
                                douStrVal = new StringBuilder();
                                mode = Mode.end;
                            }
                            break;
                    }
                }
            }
            if (isBreak) break;
            do {
                s = in.readLine();
            } while (s.trim().startsWith("//"));
        }
        if (!curKey.toString().isBlank()) {
            if (mode == Mode.intValue)
                m.put(curKey.toString(), new JSONValue(Double.parseDouble(douStrVal.toString())));
            if (mode == Mode.strValue) m.put(curKey.toString(), new JSONValue(douStrVal.toString()));
        }
        return m;
    }

    private static List<JSONValue> getList(String s, BufferedReader in) throws IOException {
        List<JSONValue> m = new ArrayList<>();
        Mode mode = Mode.end;
        StringBuilder douStrVal = new StringBuilder();
        boolean isSlash = false;
        while (true) {
            boolean isBreak = false;

            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if (ch == ']') {
                    isBreak = true;
                    break;
                } else {
                    switch (mode) {
                        case end:
                            if (Character.isDigit(ch)) {
                                douStrVal = new StringBuilder("" + ch);
                                mode = Mode.intValue;
                            } else if (ch == '\"') {
                                douStrVal = new StringBuilder();
                                mode = Mode.strValue;
                            } else if (ch == '{') {
                                m.add(new JSONValue(getJSONValue(s.substring(i + 1), in)));
                                mode = Mode.end;
                            } else if (ch == '[') {
                                m.add(new JSONValue(getList(s.substring(i + 1), in)));
                                mode = Mode.end;
                            }
                            break;
                        case intValue:
                            if (ch == ' ' || ch == ',') {
                                mode = Mode.end;
                                m.add(new JSONValue(getDoubleVal(douStrVal.toString())));
                                douStrVal = new StringBuilder();
                                break;
                            }
                            douStrVal.append(ch);
                            break;
                        case strValue:
                            if (isSlash) {
                                douStrVal.append(ch);
                                isSlash = false;
                            } else if (ch == '\\') {
                                isSlash = true;
                            } else if (ch != '\"') {
                                douStrVal.append(ch);
                            } else {
                                m.add(new JSONValue(douStrVal.toString()));
                                douStrVal = new StringBuilder();
                                mode = Mode.end;
                            }
                            break;
                    }
                }
            }
            if (isBreak) break;
            do {
                s = in.readLine();
            } while (s.trim().startsWith("//"));
        }

        if (mode != Mode.end) {
            if (mode == Mode.intValue) m.add(new JSONValue(Double.parseDouble(douStrVal.toString())));
            if (mode == Mode.strValue) m.add(new JSONValue(douStrVal.toString()));
        }
        return m;
    }

    private static Double getDoubleVal(String d) {
        d = deleteChars(d, '_');
        int index = d.indexOf('x');
        int indexB = d.indexOf('b');
        if (indexB == 1) return toDezDouble(d.substring(2), 2);
        if (index != -1) {
            if (Integer.parseInt(d.substring(0, index)) == 0) return toDezDouble(d.substring(index + 1), 16);
            return toDezDouble(d.substring(index + 1), Integer.parseInt(d.substring(0, index)));
        }
        return Double.parseDouble(d);
    }

    private static Double toDezDouble(String s, int radix) {

        int num;
        long dec = 0;

        int index = s.indexOf('.');

        for (int i = 0; i < s.length(); i++) {
            if (i == index) break;
            num = getValue(s.charAt(i));
            dec = ((dec * radix) + num);
        }
        double doub = 0;
        if (index >= 0) {
            for (int i = 0; i < s.length() - index - 1; i++) {
                num = getValue(s.charAt(s.length() - i - 1));
                System.out.println(num);
                doub = ((doub / radix) + num);
            }
            doub /= radix;
        }
        System.out.println(doub);
        return dec + doub;
    }

    public static int getValue(char digit) {
        digit = Character.toLowerCase(digit);
        if (Character.isDigit(digit)) {
            return digit - '0';
        } else if (digit >= 'a' && digit <= 'z') {
            return digit - 'a' + 10;
        }
        return -1;
    }

    public static void main(String[] args) {
        Map<String, JSONValue> m = read(Paths.get("./res/z/test1.json"));
        for (Map.Entry<String, JSONValue> e : m.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
    }

    private static String deleteChars(String s, char... chars) {
        StringBuilder res = new StringBuilder();
        for (char ch : s.toCharArray()) {
            boolean isOk = true;
            for (char c : chars) {
                if (c == ch) {
                    isOk = false;
                    break;
                }
            }
            if (isOk) res.append(ch);
        }
        return res.toString();
    }

    enum Mode {
        key,
        afterKey,
        beforeVal,
        intValue,
        strValue,
        end
    }
}
