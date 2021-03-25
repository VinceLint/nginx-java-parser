package com.github.odiszapc.nginxparser;

import org.junit.Test;

/**
 * @author VinceLin
 * @date 2021/3/21 13:43
 **/
public class TestReg {
    @Test
    public void testLazy() {
        String reg = "<.++>";
        String str = "<html>";
        System.out.println(str.matches(reg));
    }

    @Test
    public void duzhan() {
        String reg = "a(b|c)+bc";
        String text = "abbc";
        System.out.println(text.matches(reg));
    }
}
