package ServerStuff;

import Calcs.BCrypt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class User {

    private String name;

    private String password;

    private String email;

//    private List<Pokemon> pok;
//    private List<Pokemon> bank;
//    private GymBadge badge;
//    private List<Item> items;
//    private Pokedex pokedex;
//    private List<City> citys;
//    private List<Skin> skins;
//    private GymBadge badge;

    private static final List<User> allUser = new ArrayList<>();

    public User(String name, String password, String email) {
        this.name = name;
        this.email = email;
        String salt = BCrypt.gensalt(6);
        this.password = BCrypt.hashpw(password, salt);
    }

    private static boolean exists(String name) {
        try {
            ResultSet r = Database.get("select * from User where name='" + name + "' or email='" + name + "';");
            return r != null && r.first();
        } catch (SQLException ignored) {
            throw new IllegalArgumentException("Couln't connect to Database");
        }
    }

    public static int isCorrect(String username, String password) {
        try {
            ResultSet r = Database.get("select * from User where name='" + username + "' OR email='" + username + "'");
            if (r == null || !r.first()) {
                return 1;
            } else if (!BCrypt.checkpw(password, r.getString(3))) {
                return 2;
            }
            return 0;
        } catch (SQLException ignored) {
            return 7;
        }
    }

    public static void main(String[] args) {
        Database.init();
//        System.out.println(User.add("Name2", "a", "hi1@gmail.c"));
//        User.add("Name", "Password", "h@g.m");
//        User.add("Name2", "Password", "h2@g.m");
//        User.add("name", "Password", "hu.ber@gmail.com");
//        System.out.println(isCorrect("name", "Password"));
//        System.out.println(encrypt("HalloWelt123"));
//        String hashpwd = BCrypt.hashpw(encrypt("HalloWelt123"), BCrypt.gensalt());
//        System.out.println(hashpwd);
//        System.out.println(BCrypt.hashpw(hashpwd,BCrypt.gensalt(11)));
//        String salt = BCrypt.gensalt(11);
//        System.out.println(salt);
//        System.out.println(BCrypt.hashpw("abc", salt));
//        System.out.println(BCrypt.hashpw("abc", salt));
//        String pwd = "Ht;isc\u4234hlerSoft95-!3G";
//        for (int i = 0; i < 106_000; i++) {
//            storePwd("Swag", pwd);
//            String log = getLogin();
//            if (log == null || !log.split(";", 2)[1].equals(pwd)) {
//                System.out.println(log);
//            }
//        }
//        User.add("Swag", "PasswordMyNow", "myemail@gmail.com");
        System.out.println(getDataDir());
        delLoginData();
        System.out.println(getLogin());
    }

    public static void delLoginData() {
        try {
            Files.writeString(Path.of(User.getDataDir() + "User.creds"), "");
        } catch (IOException ignored) {
        }
    }

    public static String getLogin() {
        try {
            List<String> l = Files.readAllLines(Path.of(getDataDir() + "User.creds"));
            if (l.get(0).length() > 3)
                return l.get(0) + ";" + (decrypt(intArrToString(l.get(2)), Integer.parseInt(l.get(1))));
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String intArrToString(String s) {
        ArrayList<Integer> ints = new ArrayList<>();
        Matcher m = Pattern.compile("(-?[0-9]+)[,\\]]").matcher(s);
        while (m.find()) ints.add(Integer.parseInt(m.group(1)));
        StringBuilder res = new StringBuilder();
        for (Integer anInt : ints) res.append((char) (int) anInt);
        return res.toString();
    }

    public static void storePwd(String name, String pwd) {
        int r = (int) (Math.random() * 1_133_574_420) + 666;//353569;//
        pwd = encrypt(pwd, r);
        int[] ints = new int[pwd.length()];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = pwd.charAt(i);
        }
        try {
            new File(String.valueOf(Path.of(getDataDir()).toAbsolutePath())).mkdirs();
            FileOutputStream out = new FileOutputStream(getDataDir() + "User.creds");
            out.write((name + "\n").getBytes());
            out.write((r + "\n").getBytes());
            out.write((Arrays.toString(ints)).getBytes());
            out.close();
        } catch (Exception ignored) {
        }
    }

    private static String encrypt(String s, int seed) {
        int[] ints = new int[(s.length() + 1) / 2];
        Random r = new Random(seed);
        for (int i = 0; i < s.length(); i += 2) {
            int c1 = s.charAt(i);
            int c2 = (i + 1 < s.length()) ? s.charAt(i + 1) : 0;
            for (int j = 0; j < 16; j++) {
                ints[i / 2] <<= 1;
                ints[i / 2] |= ((c1 >> j) & 1);
                ints[i / 2] <<= 1;
                ints[i / 2] |= ((c2 >> j) & 1);
            }
            ints[i / 2] ^= r.nextInt();
        }

        StringBuilder res = new StringBuilder();
        for (int anInt : ints) {
            int c1 = ((anInt & 0xFFFF_0000) >> 16);
            int c2 = ((anInt & 0x0000_FFFF));
            res.append((char) c1);
            res.append((char) c2);
        }
        return res.toString();
    }

    private static String decrypt(String s, int seed) {
        int[] ints = new int[(s.length() + 1) / 2];
        Random r = new Random(seed);
        for (int i = 0; i < s.length(); i += 2) {
            int c1 = s.charAt(i);
            int c2 = i + 1 < s.length() ? s.charAt(i + 1) : 0;
            ints[i / 2] = ((0xFFFF_0000) & (c1 << 16));
            ints[i / 2] |= ((c2 & 0x0000_FFFF));
            ints[i / 2] ^= r.nextInt();
        }

        StringBuilder res = new StringBuilder();
        for (int i : ints) {
            int c1 = 0;
            int c2 = 0;
            for (int j = 0; j < 16; j++) {
                c1 >>= 1;
                c1 |= (((i >> (31 - j * 2)) & 1) << 15);
                c2 >>= 1;
                c2 |= (((i >> (30 - j * 2)) & 1) << 15);
            }
//            System.out.println(": " + String.format("%32s", Integer.toBinaryString(c1)).replaceAll(" ", "0"));
//            System.out.println(": " + String.format("%32s", Integer.toBinaryString(c2)).replaceAll(" ", "0"));
            res.append((char) c1);
            if (c2 != 0) res.append((char) c2);
        }
//        System.out.println("res:");
        return res.toString();
    }

    public static String add(String name, String password, String email) {
        if (password.length() < 8) return "Password too short";
        User cur = new User(name, password, email);
        String s = Database.execute("insert into User (name, password, email) value ('" + cur.name + "' ,'" + cur.password + "','" + cur.email + "');");
        if (s == null) {
            allUser.add(cur);
        }
        return s;
    }

    public static String getDataDir() {
        String workingDirectory;
        String OS = (System.getProperty("os.name")).toUpperCase();
        if (OS.contains("WIN")) workingDirectory = System.getenv("AppData");
        else if (OS.contains("MAC"))
            workingDirectory = System.getProperty("user.home") + "/Library/Application Support";
        else workingDirectory = System.getProperty("user.home");
        //TODO . am Anfang wegen Linux
        return (workingDirectory.endsWith("/") || workingDirectory.endsWith("\\") ? workingDirectory : workingDirectory + "/") + "ZwickelstorferPokemon/";
    }

    public static int delete(String name, String pwd) {
        int error = isCorrect(name, pwd);
        if (error == 0) {
            String s = Database.execute("delete from User where name='" + name + "' OR email='" + pwd + "';");
            return s == null ? error : 7;
        }
        return error;
    }
}
