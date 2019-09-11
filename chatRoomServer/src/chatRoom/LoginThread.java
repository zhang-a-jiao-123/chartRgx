package chatRoom;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class LoginThread extends Thread {
    Socket sk;
    HashMap<String, Socket> hm;
    ArrayList<String> list;
    Properties pName;
    private String username;

    public LoginThread(Socket sk, HashMap<String, Socket> hm, ArrayList<String> list, Properties pName) {
        this.sk=sk;
        this.hm=hm;
        this.list=list;
        this.pName=pName;
    }

    @Override
    public void run() {
        try {
            InputStream in = sk.getInputStream();
            OutputStream out = sk.getOutputStream();
            while(true){
                byte[] bytes = new byte[1024];
                int len = in.read(bytes);
                String nameWord = new String(bytes, 0, len);
                String[] nameWordSplit = nameWord.split("===");
                this.username=nameWordSplit[0];
                String password = nameWordSplit[1];

                File file = new File("users.properties");
                if(!file.exists()){
                    file.exists();
                }
                pName.load(new FileReader(file));
                String property = pName.getProperty(username, "null");
                if(password.equals(property)){
                    hm.put(username,sk);
                    list.add(username);
                    out.write("yes".getBytes());
                    break;
                }else{
                    out.write("no".getBytes());
                }
            }
            //登录成功开启服务端的读取线程
            new serverThread(hm,username,sk,list).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
