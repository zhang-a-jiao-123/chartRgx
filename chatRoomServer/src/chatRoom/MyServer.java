package chatRoom;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class MyServer {
    public static void main(String[] args) {
        ArrayList<String> list = new ArrayList<>();//用来存储用户名
        HashMap<String, Socket> hm = new HashMap<>();//创建双列集合，用来保存用户名和它对应的管道
        Properties pName = new Properties();//配置文件,存储用户名、密码
        try {
            ServerSocket socket = new ServerSocket(9999);
            System.out.println("服务器已开启......");
            int i=1;
            while(true){
                //监听客户端
                Socket sk = socket.accept();
                System.out.println("第"+(i++)+"个客户端已经连接");

                byte[] bytes = new byte[1024];
                int len = sk.getInputStream().read(bytes);
                String s = new String(bytes, 0, len);
                if("register".equals(s)){
                    //开启注册用户的线程
                    new RegisterThread(sk,hm,list,pName).start();
                }else if("login".equals(s)){
                    //开启用户登录的线程
                    new LoginThread(sk,hm,list,pName).start();
                }
            }
        }catch (SocketException e){
            //客户端下线后，会抛出一个SocketException异常,但是会在控制台打印异常信息，
            //所以我们捕获一下，做空处理
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}

