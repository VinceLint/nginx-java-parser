package com.github.odiszapc.nginxparser;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class BaseTest {
    @Test
    public void base() throws Exception{
        NgxConfig conf = NgxConfig.read("E:\\projects\\nginx-java-parser\\src\\main\\resources\\nginx\\nginx.conf");
        NgxParam workers = conf.findParam("worker_processes");       // Ex.1
        System.out.println(workers.getValue());
        NgxParam listen = conf.findParam("http", "server", "listen"); // Ex.2
        System.out.println(listen.getValue());
        List<NgxEntry> test = conf.findAll(NgxConfig.BLOCK, "rtmp");
        test.forEach(block -> {
            System.out.println(((NgxBlock)block).getName());
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
    public void dump() throws  Exception {
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
    public void testReadLocation() throws Exception{
        NgxConfig config = NgxConfig.read("E:\\projects\\nginx-java-parser\\src\\main\\resources\\nginx\\test-remove-location.conf");
        NgxBlock server = config.findBlock("server");
        List<NgxEntry> ngxBlockList = server.findAll(NgxConfig.BLOCK, "location");
        for (NgxEntry ngxEntry:ngxBlockList) {
            System.out.println(((NgxBlock)ngxEntry).getName() + ((NgxBlock)ngxEntry).getValue());
            if (((NgxBlock)ngxEntry).getValue().equals("/helloWorld")) {
                server.remove(ngxEntry);
            }
        }
        server.findAll(NgxConfig.BLOCK, "location").forEach(ngxEntry -> {
            System.out.println(((NgxBlock)ngxEntry).getName() + ((NgxBlock)ngxEntry).getValue());
        });
    }

}
