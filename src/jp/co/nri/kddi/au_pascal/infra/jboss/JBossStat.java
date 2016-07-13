/*
 * To change this license header,
 * choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.co.nri.kddi.au_pascal.infra.jboss;

import java.util.*;
import java.io.*;
import java.text.*;
import java.util.concurrent.TimeUnit;
import javax.management.*;
import javax.naming.*;
import java.lang.NullPointerException;




/**
 *
 * @author k-ota
 */
public class JBossStat {
    final static private boolean debug = true;
    //データソースの種類を格納
    private List<Stat> DSArr;
    private List<String> AttArr;
    private List<String> AttTF;
    //protected List<JBossStatCommand> commands;
    
    protected MBeanServerConnection server = null;
    protected Map<String, String> config = null; 
    
    private String[] loginName = null;
    private String[] pass = null;
    
    //String configPath = null;
    String logPath = null;
    String errorLogPath = null;
    String jbossCliPath = null;
    String commandFilePath = null;
    String currentThreadsBusyPath = null;
    
    private boolean loginFlag;
    private boolean commandFlag;
    private boolean dataFlag;
    
    
    
    JBossStat() {
        DSArr = new ArrayList<Stat>();
        AttArr = new ArrayList<String>();
        AttTF = new ArrayList<String>();
        commandFlag = dataFlag = loginFlag =  false;
    }  
    
  
   
    int perform(String confPath) throws IOException {
        int errorCode = 0;
        
         try {
             
             errorCode = getConfInfo(confPath);
             
             File errorFile = new File(this.errorLogPath);
             FileWriter errorfw = new FileWriter(errorFile,true);
             
             if (errorCode != 0) {
                if (debug) {
                    System.out.println("confファイルの読み込みに失敗しました");
                   
                }
                Date date = new Date();
                errorfw.append(date.toString() 
                        + ": confファイルの読み込みに失敗しました。"
                        +"confファイルの形式を見直してください。\n");
                errorfw.flush();
                return errorCode;
             }
             
             File file = new File(this.logPath); //ログ出力先
             FileWriter fw = new FileWriter(file,true);
             
             
             
           
            errorCode = runJBossCli(errorfw);
            if (debug) {
                System.out.println("runJBossCLI errorCode=" + errorCode);
            }
            if (errorCode != 0) {
                Date date = new Date();
                errorfw.append(date.toString() + 
                        ": jboss-cli実行時に問題が発生しました。\n");
                errorfw.flush();
                return 1;
            }
            writeLog(DSArr, fw, errorfw); 

             
            if (fw != null) {
                 fw.close();
            }
             if (errorfw != null) {
                 errorfw.close();
             }
         }
         catch (IOException e) {
             if (debug) {
                 e.printStackTrace();
             }
             File file = new File(this.errorLogPath);
             FileWriter errorfw = new FileWriter(file, true);
             Date date = new Date();
             errorfw.append(date.toString() + ": confファイルの形式が正しくありません。");
             errorfw.flush();
             return 1;
         }
         catch (NullPointerException e) {
             if (debug) {
                 e.printStackTrace();
             }
             File file = new File(this.errorLogPath);
             FileWriter errorfw = new FileWriter(file, true);
             Date date = new Date();
             errorfw.append(date.toString() + ": confファイルの形式が正しくありません。");
             errorfw.flush();
             return 1;
         }
         catch (Exception e) {
             File file = new File(this.errorLogPath);
             FileWriter errorfw = new FileWriter(file, true);
             Date date = new Date();
             errorfw.append(date.toString() + ": confファイルの形式が正しくありません。");
             errorfw.flush();
             return 1;
         }
         
        
        return errorCode;
    }
    
    int getCurrentThreadsBusy(FileWriter fw, FileWriter errorfw) {
        String num = null;
        if (debug) {
            System.out.println("⇒ getCurrentThreadsBusy");
        }
        try {
            String runCommand = this.currentThreadsBusyPath;
            if (debug) {
                System.out.println("runCommand = \n" + runCommand);
            }
            Process proc = Runtime.getRuntime().exec(runCommand);
            if (debug) {
                System.out.println("実行中 in getCurrentThreadsBusy");
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            boolean end = proc.waitFor(10, TimeUnit.SECONDS);
            if (debug) {
                System.out.println("end=" + end);
                if (end) {
                    System.out.println("正常終了 in getCurrentThreadsBusy");
                }
                else {
                    System.out.println("異常終了 in getCurrentThreadsBusy");
                    return 1;
                }
            }
            if (debug) {
                System.out.println("実行終了 in getCurrentThreadsBusy");
            }
                       
            String tmp = br.readLine();
            if (debug) {
                System.out.println(tmp);
            }
            while (tmp != null) {
                try {
                    num = new Integer(tmp).toString();
                    tmp = br.readLine();
                }
                catch (NumberFormatException e){
                    tmp = br.readLine();
                }
                catch (Exception e) {
                    tmp = br.readLine();
                }
            }
            
        }
        catch (IOException e) {
            e.printStackTrace();
            if (debug) {
                System.out.println("currentThreadsBusyの取得に失敗しました。");
            }
            return 1;
        }
        catch (Exception e) {
            e.printStackTrace();
            if (debug) {
                System.out.println("currentThreadsBusyの取得に失敗しました。");
            }
            return 1;
        }
        
        try {
            fw.append("ACTIVE_THREAD.currentThreadsBusy=" + num + "\t");
        }
        catch (IOException e) {
            return 1;
        }
        if (debug) {
            System.out.println("getCurrentThreadsBusy ⇒");
        }
        return 0;
    }
    
    
    int getPath(BufferedReader br, String confPath) throws IOException {
        String str = null;
        boolean format_OK = true;
        String logdir = confPath.trim().split("/jboss.conf")[0];
        
        if (debug) {
            System.out.println("func: getPath　⇒  start!");
        }
        try {
            str = br.readLine();
            if (str.trim().equals("**Path**")) {
                
                //ログファイル出力先の取得
                str = br.readLine();
                String[] tmp = str.split("=");
                if (tmp[0].trim().equals("logPath")) {
                    this.logPath = tmp[1].trim();
                }
                else {
                    System.out.println("書式が不正です＠logPath");
                    format_OK = false;
                }
                if (debug) {
                    System.out.println("logPath: " + this.logPath);
                }
                
                str = br.readLine();
                tmp = null;
                tmp = str.split("=");
                while (!tmp[0].trim().equals("errorLogPath")) {
                    format_OK = false;
                    tmp = null;
                    br.readLine();
                }
                if (tmp[0].trim().equals("errorLogPath")) {
                    this.errorLogPath = tmp[1].trim();
                    if (debug) {
                        System.out.println("errorPath = " + this.errorLogPath);
                    }
                }
                else {
                    System.out.println("書式が不正です＠errorLogPath");
                    format_OK = false;
                }
                if (debug) {
                    System.out.println("errorLogPath: " + this.errorLogPath);
                }
                
                File errorfile = new File(this.errorLogPath);
                FileWriter errorfw = new FileWriter(errorfile, true);
                
                //currentThreadsBusy取得用コマンドファイル
                str = br.readLine();
                tmp = null;
                tmp = str.split("=");
                if (tmp[0].trim().equals("commandFilePath")) {
                    this.commandFilePath = tmp[1].trim();
                }
                else {
                    System.out.println("書式が不正です＠commandFilePath");
                    format_OK = false;
                }
                if (debug) {
                    System.out.println("commandFilePath: " 
                            + this.commandFilePath);
                }
                
                //jboss-cliのパス
                str = br.readLine();
                tmp = null;
                tmp = str.split("=");
                if (tmp[0].trim().equals("jboss-cli")) {
                    this.jbossCliPath = tmp[1].trim();
                    if (debug) {
                        System.out.println(tmp[0] + "=" + tmp[1]);
                    }
                }
                else {
                    System.out.println("書式が不正です＠jboss-cli");
                    format_OK = false;
                }
                
                //アプリケーションホームパス
                str = br.readLine();
                tmp = null;
                tmp = str.split("=");
                if (tmp[0].trim().equals("currentThreadsBusyPath")) {
                    this.currentThreadsBusyPath = tmp[1].trim();
                }
                else {
                    System.out.println("書式が不正です＠currentThreadsBusyPath");
                    format_OK = false;
                }
                if (debug) {
                    System.out.println("currentThreadsBusyPath: " 
                            + this.currentThreadsBusyPath);
                }
                
            }
            else {
                System.out.println("書式が不正です＠Path");
                str = br.readLine();
                String[] tmp;
                tmp = null;
                tmp = str.split("=");
                while (!tmp[0].trim().equals("errorLogPath")) {
                    format_OK = false;
                    tmp = null;
                    if (debug) {
                        System.out.println("エラーパス探し");
                    }
                    br.readLine();
                }
                if (tmp[0].trim().equals("errorLogPath")) {
                    this.errorLogPath = tmp[1].trim();
                    if (debug) {
                        System.out.println("errorPath = " + this.errorLogPath);
                    }
                }
                format_OK = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            if (debug) {
                System.out.println("(　´∀｀)＜ぬるぽ１");
            }
            str = br.readLine();
            String[] tmp;
            tmp = null;
            tmp = str.split("=");
            while (!tmp[0].trim().equals("errorLogPath")) {
                format_OK = false;
                tmp = null;
                if (debug) {
                    System.out.println("エラーパス探し");
                }
                br.readLine();
            }
            if (tmp[0].trim().equals("errorLogPath")) {
                this.errorLogPath = tmp[1].trim();
                if (debug) {
                    System.out.println("errorPath = " + this.errorLogPath);
                }
            }
        }
        catch (Exception e) {
            if (debug) {
                System.out.println("(　´∀｀)＜ぬるぽ２");
            }
            str = br.readLine();
            String[] tmp;
            tmp = null;
            tmp = str.split("=");
            while (!tmp[0].trim().equals("errorLogPath")) {
                format_OK = false;
                tmp = null;
                if (debug) {
                    System.out.println("エラーパス探し");
                }
                br.readLine();
            }
            if (tmp[0].trim().equals("errorLogPath")) {
                this.errorLogPath = tmp[1].trim();
                if (debug) {
                    System.out.println("errorPath = " + this.errorLogPath);
                }
            }
        }
        if (debug) {
            System.out.println("func: getPath　⇒  end!");
        }
        if (format_OK == false) {
            return 1;
        }
        return 0;
    }
    
    
    
    int getData(BufferedReader br) throws IOException {
        
        try {
            String str = br.readLine();

            if (str.trim().equals("**Data**")) {
                dataFlag = true;
                if (debug) {
                    System.out.println("データチェック領域に突入。");
                }
            }
            else {
                if (debug) {
                    System.out.println("confファイルの様式がよろしくありません。");
                    Date date = new Date();
                    System.out.println(date.toString() + ": confファイルの様式がよろしくありません。");
                }
                return 1;
            }

            if (dataFlag) {
                str = br.readLine();
                if (debug) {
                    System.out.println("str = " + str);
                }
                String[] tmp = str.split("=", 0);
                if (debug) {
                    System.out.println("DS = " + tmp[1]);
                }
                String[] DSs = tmp[1].split(",", 0);

                if (debug) {
                    System.out.println("データソース群のsplitでいけた");
                    System.out.println("DS1=" + tmp[0]);
                    System.out.println("DS2=" + tmp[1]);
                }


                //データソース群の取得
                for (String s : DSs) {
                    Stat obj = new Stat();
                    obj.setDSName(s);
                    DSArr.add(obj);
                    if (debug) {
                        System.out.println(s);
                    }
                }

                if (debug) {
                    System.out.println("データソース群の取得まではOK");
                }

                str = br.readLine();
                //jbossstat.confの有効な属性のみを取得
                while (str != null) {
                    String[] word = str.split("=",0);
                    if (debug) {
                        System.out.println(str);
                    }

                    //記録する属性情報を取得
                    AttArr.add(word[0]);
                    AttTF.add(word[1]);
                    str = br.readLine();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            Date date = new Date();
            System.out.println(date.toString() + ": データソースの記載がありません");
            if (debug) {
                System.out.println("IOでクラッシュ");
            }
            return 1;
        }
        catch (Exception e) {
            e.printStackTrace();
            Date date = new Date();
            System.out.println(date.toString() + ": データソースの記載がありません");
            if (debug) {
                System.out.println("その他のエラーでクラッシュ");
                
            }
            return 1;
        }
        return 0;
    }
    
    int getConfInfo(String confPath) throws FileNotFoundException, IOException {
        int errorCode = 0;
        
        File file = new File(confPath); //絶対パスに直す
        FileReader filereader = new FileReader(file);
        BufferedReader br = new BufferedReader(filereader);
        
        try {
            errorCode = getPath(br, confPath);
            if (errorCode != 0) {
                return 1;
            }
        }
        catch (NullPointerException e) {
            if (debug) {
                System.out.println("(　´∀｀)＜ぬるぽ３");
            }
            String str = br.readLine();
            String[] tmp;
            tmp = null;
            tmp = str.split("=");
            while (!tmp[0].trim().equals("errorLogPath")) {
                tmp = null;
                if (debug) {
                    System.out.println("エラーパス探し");
                }
                br.readLine();
            }
            if (tmp[0].trim().equals("errorLogPath")) {
                this.errorLogPath = tmp[1].trim();
                if (debug) {
                    System.out.println("errorPath = " + this.errorLogPath);
                }
            }
            return 1;
        }
        catch (Exception e){
            if (debug) {
                System.out.println("(　´∀｀)＜ぬるぽ４");
            }
            String str = br.readLine();
            String[] tmp;
            tmp = null;
            tmp = str.split("=");
            while (!tmp[0].trim().equals("errorLogPath")) {
                tmp = null;
                if (debug) {
                    System.out.println("エラーパス探し");
                }
                br.readLine();
            }
            if (tmp[0].trim().equals("errorLogPath")) {
                this.errorLogPath = tmp[1].trim();
                if (debug) {
                    System.out.println("errorPath = " + this.errorLogPath);
                }
            }
        }
        
        
        
        
        if (errorCode != 0) {
            System.out.println("パスが取得できませんでした。");
            Date date = new Date();
            return 1;
        }
        
        errorCode = getData(br);
        if (errorCode != 0) {
            Date date = new Date();
            System.out.println(date.toString() 
                    + ": データ取得時に問題が発生しました。\n");
        }
            
        //入力のクロージング
        if (br != null) {
            br.close();
        }
        if (filereader != null) {
            filereader.close();
        }
        
        if (debug) {
            System.out.println("finish getConfInfo");
        }
        
        return 0;
    }
    
    Stat setAttribute(Stat DS, ArrayList<String> arrList) {
        for (String line : arrList) {
            String[] att = line.split("=");
            
            if (debug) {
                System.out.println("in setAttribute");
                if (att.length == 2) {
                    System.out.println("セットする値: " + att[0] + "=" + att[1]);
                }
            }

            if (att[0].equals("ActiveCount")) {
                DS.setActiveCount(Integer.parseInt(att[1]));
            }
            if (att[0].equals("AvailableCount")) {
                DS.setAvailableCount(Integer.parseInt(att[1]));
            }
            if (att[0].equals("AverageBlockingTime")) {
                DS.setAverageBlockingTime(Integer.parseInt(att[1]));
            }
            if (att[0].equals("AverageCreationTime")) {
                DS.setAverageCreationTime(Integer.parseInt(att[1]));
            }
            if (att[0].equals("CreatedCount")) {
                DS.setCreatedCount(Integer.parseInt(att[1]));
            }
            if (att[0].equals("DestroyedCount")) {
                DS.setDestroyedCount(Integer.parseInt(att[1]));
            }
            if (att[0].equals("InUseCount")) {
                DS.setInUseCount(Integer.parseInt(att[1]));
            }
            if (att[0].equals("MaxCreationTime")) {
                DS.setMaxCreationTime(Integer.parseInt(att[1]));
            }
            if (att[0].equals("MaxUsedCount")) {
                DS.setMaxUsedCount(Integer.parseInt(att[1]));
            }
            if (att[0].equals("MaxWaitCount")) {
                DS.setMaxWaitCount(Integer.parseInt(att[1]));
            }
            if (att[0].equals("MaxWaitTime")) {
                DS.setMaxWaitTime(Integer.parseInt(att[1]));
            }
            if (att[0].equals("TimedOut")) {
                DS.setTimedOut(Integer.parseInt(att[1]));
            }
            if (att[0].equals("TotalBlockingTime")) {
                DS.setTotalBlockingTime(Integer.parseInt(att[1]));
            }
            if (att[0].equals("TotalCreationTime")) {
                DS.setTotalCreationTime(Integer.parseInt(att[1]));
            }
        }
        
        return DS;
    }
    
    int getAttribute(Stat DS,String str) {
       
        if (str.trim().equals("ActiveCount")) {
            return DS.getActiveCount();
        }
        if (str.trim().equals("AvailableCount")) {
            return DS.getAvailableCount();
        }
        if (str.trim().equals("AverageBlockingTime")) {
            return DS.getAverageBlockingTime();
        }
        if (str.trim().equals("AverageCreationTime")) {
            return DS.getAverageCreationTime();
        }
        if (str.trim().equals("CreatedCount")) {
            return DS.getCreatedCount();
        }
        if (str.trim().equals("DestroyedCount")) {
            return DS.getDestroyedCount();
        }
        if (str.trim().equals("InUseCount")) {
            return DS.getInUseCount();
        }
        if (str.trim().equals("MaxCreationTime")) {
            return DS.getMaxCreationTime();
        }
        if (str.trim().equals("MaxUsedCount")) {
            return DS.getMaxUsedCount();
        }
        if (str.trim().equals("MaxWaitCount")) {
            return DS.getMaxWaitCount();
        }
        if (str.trim().equals("MaxWaitTime")) {
            return DS.getMaxWaitTime();
        }
        if (str.trim().equals("TimedOut")) {
            return DS.getTimedOut();
        }
        if (str.trim().equals("TotalBlockingTime")) {
            return DS.getTotalBlockingTime();
        }
        if (str.trim().equals("TotalCreationTime")) {
            return DS.getTotalCreationTime();
        }
        return 1;
    }
    
    int writeLog(List<Stat> DSArr, FileWriter fw, FileWriter errorfw) 
            throws IOException {
        try {
            //現在時刻の取得
            Date now = new Date();
            SimpleDateFormat sdf = 
                    new SimpleDateFormat("yyyy'-'MM'-'dd' 'HH':'mm':'ss");
            fw.append(sdf.format(now) + "\t");
            
            int errorCode = getCurrentThreadsBusy(fw,errorfw);
            if (errorCode != 0) {
                Date date = new Date();
                errorfw.append(date.toString() 
                        + ": currentThreadsBusyを取得できませんでした。\n");
                errorfw.flush();
                return 1;
            }
            
            for (Stat ds : DSArr){
                for (int i = 0; i < AttArr.size(); i++) {
                    if (AttTF.get(i).equals("true")) {
                        fw.append(ds.getDSName() + "." 
                                + AttArr.get(i) + "=" 
                                + getAttribute(ds,AttArr.get(i)) + "\t");
                    }
                }
            }
            fw.append("\n");
        }
        catch (IOException e) {
            Date date = new Date();
            System.out.println("正常にログにかきこめませんでした。");
            errorfw.append(date.toString() 
                    + ": 正常にログにかきこめませんでした。\n");
            errorfw.flush();
            e.printStackTrace();
            return 1;
        } 
        catch (Exception e) {
            e.printStackTrace();
            Date date = new Date();
            errorfw.append(date.toString() 
                    + ": 正常にログにかきこめませんでした。\n");
            errorfw.flush();
            return 1;
        }
        return 0;
    }
    
    int runJBossCli(FileWriter errorfw) throws IOException {
        try {
            for (Stat DS : DSArr) {
                if (debug) {
                    System.out.println(DS.getDSName());
                }
                
                String runCommand = this.jbossCliPath + " " + DS.getDSName();

                if (debug) {
                    System.out.println("runCommand = \n" + runCommand);
                }
                
                Process proc = Runtime.getRuntime().exec(runCommand);
                
                if (debug) {
                    System.out.println("実行中");
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                boolean end = proc.waitFor(60, TimeUnit.SECONDS);
                if (debug) {
                    System.out.println("runJBossCLI ⇒ end=" + end);
                    if (end) {
                        System.out.println("正常終了 in runJBossCLI");
                    }
                    else {
                        System.out.println("異常終了 in runJBossCLI");
                        return 1;
                    }
                }
                
                ArrayList<String> tmp = new ArrayList<String>();
                String line = br.readLine();
                if (debug) {
                    System.out.println("str = \n" + line);
                }
                //CLI出力内容の読み込み
                if (debug) {
                    System.out.println("CLIの読み込み開始");
                }
                while (line != null) {
                    if (debug) {
                        System.out.println("line = \n" + line);
                    }
                    tmp.add(line);
                    line = br.readLine();
                }
                if (debug) {
                    System.out.println("CLIの読み込み終了");
                }
                DS = setAttribute(DS, tmp);
                //System.out.println(DS.getInUseCount());
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            Date date = new Date();
            errorfw.append(date.toString() 
                    + ": 正常にJboss-cliが動作しませんでした。\n");
            errorfw.flush();
            return 1;
        }
        catch (Exception e) {
            e.printStackTrace();
            Date date = new Date();
            errorfw.append(date.toString() 
                    + ": 正常にJboss-cliが動作しませんでした。\n");
            errorfw.flush();
            return 1;
        }
        return 0;
    }
    
}
