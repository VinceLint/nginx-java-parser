package com.github.odiszapc.nginxparser;

import org.junit.Test;

import javax.sound.midi.Soundbank;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class BaseTest {
    @Test
    public void base() throws Exception {
        NgxConfig conf = NgxConfig.read("/Users/vincelin/Projects/nginx-java-parser/src/main/resources/nginx/nginx.conf");
        NgxParam workers = conf.findParam("worker_processes");       // Ex.1
        System.out.println(workers.getValue());
        NgxParam listen = conf.findParam("http", "server", "listen"); // Ex.2
        System.out.println(listen.getValue());
        List<NgxEntry> test = conf.findAll(NgxConfig.BLOCK, "rtmp");
        test.forEach(block -> {
            System.out.println(((NgxBlock) block).getName());
            System.out.println(((NgxBlock) block).getTokens());
        });
        List<NgxEntry> rtmpServers = conf.findAll(NgxConfig.BLOCK, "rtmp", "server"); // Ex.3
        for (NgxEntry entry : rtmpServers) {
            System.out.println(((NgxBlock) entry).getName());
            System.out.println(((NgxBlock) entry).findParam("application", "live"));
        }
        System.out.println(conf.findParam("listen"));
    }


    @Test
    public void dump() throws Exception {
        NgxConfig conf = NgxConfig.read("E:\\projects\\nginx-java-parser\\src\\main\\resources\\nginx\\nginx.conf");
// ...
        NgxDumper dumper = new NgxDumper(conf);
        dumper.dump(System.out);
        System.out.println(conf.getValues());

        NgxConfig config = new NgxConfig();
    }

    @Test
    public void write() throws Exception {
        try {
            NgxConfig ngxConfig = NgxConfig.read("E:\\projects\\nginx-java-parser\\src\\main\\resources\\nginx\\test-write.conf");
            NgxBlock serverBlock = ngxConfig.findBlock("server");


            NgxBlock locationBlock = new NgxBlock();
            locationBlock.addValue("location");
            locationBlock.addValue("/");
            NgxParam proxyPass = new NgxParam();
            proxyPass.addValue(String.format("proxy_pass %s", ""));
            locationBlock.addEntry(proxyPass);
            serverBlock.addEntry(locationBlock);


            NgxBlock locationBlock2 = new NgxBlock();
            locationBlock2.addValue("location");
            locationBlock2.addValue("/helloWorld");
            serverBlock.addEntry(locationBlock2);
            String content = new NgxDumper(ngxConfig).dump();
            System.out.println(content);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("write nginx.conf to file catch IOException!");
        }
    }

    @Test
    public void testReadLocation() throws Exception {
        NgxConfig config = NgxConfig.read("/Users/vincelin/Projects/nginx-java-parser/src/main/resources/nginx/test-remove-location.conf");
        NgxBlock server = config.findBlock("server");
        List<NgxEntry> ngxBlockList = server.findAll(NgxConfig.BLOCK, "location");
        for (NgxEntry ngxEntry : ngxBlockList) {
            System.out.println(((NgxBlock) ngxEntry).getName() + ((NgxBlock) ngxEntry).getValue());
            System.out.println(((NgxBlock) ngxEntry).findParam("proxy_pass").getValue());
            if (((NgxBlock) ngxEntry).getValue().equals("/helloWorld")) {
                server.remove(ngxEntry);
            }
        }
        server.findAll(NgxConfig.BLOCK, "location").forEach(ngxEntry -> {
            System.out.println(((NgxBlock) ngxEntry).getName() + ((NgxBlock) ngxEntry).getValue());
        });
    }

    @Test
    public void testAddEmptyParam() {
        NgxConfig config = new NgxConfig();
        NgxBlock location = new NgxBlock();
        // sftp type + " " + context
        location.addValue("location /heelo");
        config.addEntry(location);

        NgxParam ngxParam1 = new NgxParam();
        ngxParam1.addValue(String.format("proxy_pass %s", ""));
        location.addEntry(ngxParam1);

        String content = new NgxDumper(config).dump();
        System.out.println(content);
    }

    @Test
    public void buildServerParam() {
        Map<String, String> params = new HashMap<String, String>(3) {{
            put("proxy_pass", "http://127.0.01");
            put("proxy_download_rate", "10kb");
        }};
        System.out.println(parseServerConfig(params));
    }

    public static String parseServerConfig(Map<String, String> params) {
        NgxConfig config = new NgxConfig();
        NgxBlock server = new NgxBlock();
        server.addValue("server");
        config.addEntry(server);
        addParam(server, params);
        String content = new NgxDumper(config).dump();
        return content;
    }

    private static void addParam(NgxBlock block, Map<String, String> params) {
        params.forEach((key, value) -> {
            addParam(block, key, value);
        });
    }

    private static void addParam(NgxBlock block, String key, String value) {
        NgxParam param = new NgxParam();
        param.addValue(String.format(key + " %s", value));
        block.addEntry(param);
    }

    @Test
    public void readList() throws Exception {
        NgxConfig config = NgxConfig.read("/Users/vincelin/Projects/nginx-java-parser/src/main/resources/nginx/readList.conf");
        NgxBlock server = config.findBlock("server");
        List<NgxEntry> ngxBlockList = server.findAll(NgxConfig.BLOCK, "location");
        for (NgxEntry ngxEntry : ngxBlockList) {
            List<NgxEntry> params = (List<NgxEntry>) ((NgxBlock) ngxEntry).getEntries();
            Map<String, StringJoiner> map = new HashMap<>();
            Node node = new Node();
            for (NgxEntry param : params) {
                String key = ((NgxParam) param).getName();
                String value = ((NgxParam) param).getValue();
                System.out.println(key);
                System.out.println(value);
                if (map.get(key) == null) {
                    map.put(key, new StringJoiner(";"));
                }
                map.get(key).add(value);
            }
            mapToObject(map, node);
            System.out.println(node.server);
        }
    }

    private class Node {
        String server;
    }


    private <O> O mapToObject(Map<String, StringJoiner> map, O obj) {
        try {
            Field[] declaredFields = obj.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                int mod = field.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                    continue;
                }
                field.setAccessible(true);
                if (map.get(field.getName()) == null) {
                    continue;
                }
                field.set(obj, map.get(field.getName()).toString());
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
