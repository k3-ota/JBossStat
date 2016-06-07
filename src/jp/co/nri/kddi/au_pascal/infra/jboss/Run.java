/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.co.nri.kddi.au_pascal.infra.jboss;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

/**
 *
 * @author k-ota
 */
public class Run {
    public static void main(String[] args)  throws InterruptedException, IOException {
        System.out.println("test start!");
        JBossStat obj = new JBossStat();
        
        int errorCode = obj.perform(args[0]);
        System.out.println("test end!!");
        if (errorCode == 0) {
            System.out.println("正常にログがとれました。");
        }
        else {
            System.out.println("異常終了が発生しました。");
        }
    }
    
}
