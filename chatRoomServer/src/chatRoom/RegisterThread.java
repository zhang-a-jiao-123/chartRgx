package chatRoom;

import config.MsgType;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

public class RegisterThread extends Thread {
    Socket sk;
    HashMap<String, Socket> hm;
    ArrayList<String> list;
    Properties pName;
    private String username;

    public RegisterThread(Socket sk, HashMap<String, Socket> hm, ArrayList<String> list, Properties pName) {
       this.sk=sk;
       this.hm=hm;
       this.list=list;
       this.pName=pName;
    }

    @Override
    public void run() {
        //注册用户完了后在开启聊天线程
        try {
            InputStream in = sk.getInputStream();
            OutputStream out = sk.getOutputStream();
            while(true){
                byte[] bytes = new byte[1024];
                int len = in.read(bytes);
                String nameWord = new String(bytes, 0, len);
                String[] nameWordSplit = nameWord.split("===");
                this.username = nameWordSplit[0];
                String password = nameWordSplit[1];
                File file = new File("users.properties");
                if(!file.exists()){
                    file.createNewFile();
                }
                pName.load(new FileReader(file));//读取配置文件
                String property = pName.getProperty(this.username, "null");
                if(property.equals("null")){
                    //若配置文件中没有此用户，则注册
                        hm.put(this.username,sk);//存进去
                        list.add(this.username);//单列集合里也存一下用户名
                        //存入配置文件
                        pName.setProperty(this.username,password);
                        pName.store(new FileWriter(file),null);
                        //给服务器反馈
                        out.write("yes".getBytes());
                        break;
                }else{
                    out.write("no".getBytes());
                }
            }
            //开启服务端的读取线程
            new serverThread(hm,username,sk,list).start();

            //上线提醒
            Set<String> keySet = hm.keySet();
            for (String key : keySet) {
                if(key.equals(username)){
                    continue;
                }
                String msg=username+":"+"上线了"+":"+ MsgType.MSG_ONLINE+":"+System.currentTimeMillis();
                hm.get(key).getOutputStream().write(msg.getBytes());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
