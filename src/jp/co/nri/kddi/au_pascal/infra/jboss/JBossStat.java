/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.co.nri.kddi.au_pascal.infra.jboss;

import java.util.*;
import java.io.*;
import java.text.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.*;
import javax.naming.*;

import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.jboss.varia.scheduler.Schedulable;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.RunAsIdentity;
import org.jboss.util.propertyeditor.PropertyEditors;



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
    protected List<JBossStatCommand> commands;
    
    protected MBeanServerConnection server = null;
    protected Map<String, String> config = null; 
    
    private String[] loginName = null;
    private String[] pass = null;
    
    //String configPath = null;
    String logPath = null;
    
    private boolean loginFlag;
    private boolean commandFlag;
    private boolean dataFlag;
    
    
    
    JBossStat() {
        DSArr = new ArrayList<Stat>();
        AttArr = new ArrayList<String>();
        AttTF = new ArrayList<String>();
        commandFlag = dataFlag = loginFlag =  false;
    }  
    
    protected static class JBossStatCommand {
      public final String colName;
      public final String objectName;
      public final String[] names;
      public JBossStatCommand(final String line){
        final String[] parts = line.split(" ");
        this.colName = parts[0];
        this.objectName = parts[1];
        this.names = Arrays.copyOfRange(parts, 2, parts.length);
      }
    }
   
    int perform() {
         int errorCode = 0;
        
         try {
             errorCode = getConfInfo();
             File file = new File(logPath); //ログ出力先
             FileWriter fw = new FileWriter(file,true);
             errorCode = runJBossCli();
             writeLog(DSArr, fw); 
             if (fw != null) {
                 fw.close();
             }
         }
         catch (IOException e) {
             System.out.println("書き込み不良");
             e.printStackTrace();
         }
         catch (Exception e) {
             e.printStackTrace();
         }
        
        
        return errorCode;
    }
    
    
    protected void connectServer() throws NamingException {
        if (debug) {
            System.out.println("name=" + loginName[1] + ", pass=" + pass[1]);
        }
        Hashtable props = new Hashtable(System.getProperties());
        props.put(Context.PROVIDER_URL, config.get("server"));
        InitialContext ctx = new InitialContext(props);
        Object obj = ctx.lookup("jmx/invoker/RMIAdaptor");
        ctx.close();
        if (!(obj instanceof RMIAdaptor)){
           throw new ClassCastException
              ("Object not of type: RMIAdaptorImpl, but: " +
              (obj == null ? "not found" : obj.getClass().getName()));
        }
        this.server = (MBeanServerConnection) obj;
    }
    
      
    protected AttributeList executeGetCommand(final String objName, final String[] names) throws Exception {
      final ObjectName objectName = new ObjectName(objName);
      AttributeList attrList = server.getAttributes(objectName, names);
      if (attrList.size() == 0) {
         throw new Exception("No matching attributes");
      } else if (attrList.size() != names.length) {
         throw new Exception("Not all specified attributes were found");
      }
      return attrList;
    }
    
    
    int loginServer(BufferedReader br) throws IOException, NamingException {
        String str = br.readLine();
        String name = null;
        String pass = null;
        
        if (str.equals("**LoginInfo**")) {
            loginFlag = true;
        }
        else {
            System.out.println("confファイルの様式がよろしくありません。");
            return 1;
        }
        
        if (config == null) {
            this.config = new HashMap<String, String>();
        }
        
        if (loginFlag) {
            str = br.readLine();
            this.loginName = (str.trim()).split("=");
            if (this.loginName[0].equals("name")) {
                name = this.loginName[1];
                this.config.put(this.loginName[0].trim(), name.trim());
            }
            else {
                System.out.println("confファイルの様式がよろしくありません。");
                return 1;
            }
            
            str = br.readLine();
            this.pass = (str.trim()).split("=");
            if (this.pass[0].equals("pass")) {
                pass = (str.trim()).split("=")[1];
                this.config.put(this.pass[0].trim(), pass.trim());
                
            }
            else {
                System.out.println("confファイルの様式がよろしくありません。");
                return 1;
            }
            
            if (config.get("server") == null) {
                config.put("server", "127.0.0.1");
            }
            
            if (debug) {
                System.out.println("name=" + this.config.get("name"));
                System.out.println("pass=" + this.config.get("pass"));
            }
            
            SecurityAssociation.setPrincipal(new SimplePrincipal(this.config.get("name")));
            SecurityAssociation.setCredential(this.config.get("pass"));
            
            if (debug) {
                System.out.println(Context.PROVIDER_URL);
                System.out.println(config.get("server"));
            }
            
            
            Hashtable props = new Hashtable(System.getProperties());
            props.put(Context.PROVIDER_URL, config.get("server"));
            InitialContext ctx = new InitialContext(props);
            Object obj = ctx.lookup("jmx/invoker/RMIAdaptor");
            ctx.close();
            if (!(obj instanceof RMIAdaptor)){
               throw new ClassCastException
                  ("Object not of type: RMIAdaptorImpl, but: " +
                  (obj == null ? "not found" : obj.getClass().getName()));
            }
            this.server = (MBeanServerConnection) obj;
        }
        else {
            System.out.println("サーバにログインできていません。");
            return 1;
        }
        
        return 0;
    }
    
    int getLogPath(BufferedReader br) {
        String str = null;
        try {
            str = br.readLine();
            if (str.equals("**logPath**".trim())) {
                
                //configPathの取得
                /*
                str = br.readLine();
                String[] tmp = null;
                tmp = str.split("=");
                if (tmp[0].trim().equals("configPath")) {
                    this.configPath = tmp[1].trim();
                }
                else {
                    System.out.println("書式が不正です。");
                    return 1;
                }
                */
                
                //ログファイル出力先の取得
                str = br.readLine();
                String[] tmp = str.split("=");
                if (tmp[0].trim().equals("logPath")) {
                    this.logPath = tmp[1].trim();
                    if (debug) {
                        System.out.println(tmp[0] + "=" + tmp[1]);
                    }
                }
                else {
                    System.out.println("書式が不正です。");
                    return 1;
                }
                
            }
            else {
                System.out.println("書式が不正です。");
                return 1;
            }
        } catch (IOException ex) {
            Logger.getLogger(JBossStat.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
    
    int getConfInfo() {
        int errorCode = 0;
        try {
            //File file = new File("C:\\Users\\k-ota\\Documents\\NetBeansProjects\\JBoss_Stat_Library\\src\\jp\\co\\nri\\kddi\\au_pascal\\infra\\jboss\\jbossstat.conf");
            File file = new File(".\\resources\\jbossstat.conf");
            FileReader filereader = new FileReader(file);
            BufferedReader br = new BufferedReader(filereader);
            
            errorCode = getLogPath(br);
            
            //サーバログイン
            loginServer(br);
            
            String str = br.readLine();
            if (str.equals("**Command**")) {
                commandFlag = true;
            }
            if (commandFlag) {
                str = br.readLine();
                commands.add(new JBossStatCommand(str.trim()));
            }
            else {
                System.out.println("confファイルの様式がよろしくありません。");
                return 1;
            }
            
            
            str = br.readLine();
            if (str.equals("**Data**")) {
                dataFlag = true;
            }
            else {
                System.out.println("confファイルの様式がよろしくありません。");
                return 1;
            }

            if (dataFlag) {
                String[] tmp = str.split("=", 0);
                String[] DSs = tmp[1].split(",", 0);

                if (debug) {
                    System.out.println("データソース群のsplitでいけた");
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

                //入力のクロージング
                if (br != null) {
                    br.close();
                }
                if (filereader != null) {
                    filereader.close();
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
            return 1;
        }
        catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        
        return 0;
    }
    
    Stat setAttribute(Stat DS, ArrayList<String> arrList) {
        for (String line : arrList) {
            String[] att = line.split("=");

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
        return -1;
    }
    
    int writeLog(List<Stat> DSArr, FileWriter fw) {
        try {
            //現在時刻の取得
            Date now = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd' 'HH':'mm':'ss");
            fw.append(sdf.format(now) + "\t");
            
            //現在リクエスト処理中のスレッド数の取得
            if (server == null) {
                connectServer();
            }
            for (final JBossStatCommand command : commands) {
                AttributeList attList = executeGetCommand(command.objectName, command.names);
                Iterator it = attList.iterator();
                
                while (it.hasNext()) {
                    Attribute att = (Attribute)it.next();
                    
                    fw.append(command.colName);
                    fw.append(".");
                    fw.append(att.getName());
                    fw.append("=");
                    fw.append((String) att.getValue());
                    fw.append("\t");
                }
            }
            
            fw.append("ACTIVE_THREAD.crrentThreadsBusy=" + DSArr.get(0).getActiveCount() + "\t");
            for (Stat ds : DSArr){
                for (int i = 0; i < AttArr.size(); i++) {
                    if (AttTF.get(i).equals("true")) {
                        fw.append(ds.getDSName() + "." + AttArr.get(i) + "=" + getAttribute(ds,AttArr.get(i)) + "\t");
                    }
                }
                fw.append("\t");
            }
            fw.append("\n");
        }
        catch (IOException e) {
            System.out.println("正常にログにかきこめませんでした。");
            e.printStackTrace();
            return -1;
        } 
        catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }
    
    int runJBossCli() {
        try {
            for (Stat DS : DSArr) {
                if (debug) {
                    System.out.println(DS.getDSName());
                }
                String runCommand = "C:\\Users\\k-ota\\Desktop\\jboss-eap-6.2\\bin\\jboss-cli.bat -c --commands=\"cd /subsystem=datasources/data-source=" + DS.getDSName() + "/statistics=pool/,ls\"";
                Process proc = Runtime.getRuntime().exec(runCommand);
                System.out.println("実行中");
                InputStream is = proc.getInputStream();
                proc.waitFor();
                System.out.println("実行終了");
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                ArrayList<String> tmp = new ArrayList<String>();
                String str = br.readLine();
                //CLI出力内容の読み込み
                while (str != null) {
                    tmp.add(str);
                    str = br.readLine();
                }
                DS = setAttribute(DS, tmp);
                //System.out.println(DS.getInUseCount());
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
        catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }
    
}
