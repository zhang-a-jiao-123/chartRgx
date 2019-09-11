package chatRoom;

import config.ChatTxt;
import config.MsgType;
import utils.InputAndOutputUtil;
import utils.InputUtil;
import utils.TimeUtil;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class MyClient {
    private static InputStream in;
    private static OutputStream out;
    public static void main(String[] args) {
        try {
            Socket sk = new Socket("192.168.11.22", 9999);
            //获取通道中的输入输出流
            in = sk.getInputStream();
            out = sk.getOutputStream();
            System.out.println("请选择 1.注册 2.登陆");
            int i = InputUtil.inputIntType(new Scanner(System.in));
            switch (i) {
                case 1:
                    out.write("register".getBytes());
                    userRegister();
                    break;
                case 2:
                    out.write("login".getBytes());
                    userLogin();
                    break;
            }
            //开启客户端读取子线程
            clientThread th = new clientThread(in);
            th.start();
            //提供菜单选择
            boolean flag = true;
            while (flag) {
                System.out.println("请选择：1.私聊 2.公聊 3.在线列表 4.下线 5.隐身/上线 6.在线发送文件 7.查询聊天记录");
                Scanner sc = new Scanner(System.in);
                String num = sc.nextLine();
                switch (num) {
                    case "1"://私聊
                        privateTalk();
                        break;
                    case "2"://公聊
                        publicTalk();
                        break;
                    case "3"://获取在线列表
                        getOnlineList();
                        break;
                    case "4"://下线
                        //客户端：给服务端发送下线指令；关闭客户端的Socket；还得停掉客户端读取消息的线程
                        exitTalk();
                        flag = false;
                        break;
                    //服务端：关闭集合中下线者的管道；移除集合中下线者的名字；下线提醒
                    case "5"://隐身、上线状态的切换
                        switchStatus();
                        break;
                    case "6"://在线发送文件
                        sendFile();
                        break;
                    case "7"://查询聊天记录
                        lookTxt();
                        break;
                    case "y":
                        th.setSave(false);
                        break;
                    case "n":
                        th.setColse(true);
                        break;
                }
            }
            //下线：关闭客户端的Socket,停掉客户端读取消息的线程
            th.stop();
            sk.close();
        } catch (SocketException e){
            //客户端下线后，会抛出一个SocketException异常,但是会在控制台打印异常信息，
            //所以我们捕获一下，做空处理
        } catch (IOException e) {
            e.printStackTrace();
        }catch (ArrayIndexOutOfBoundsException e){
            System.out.println("您输入格式不正确,请重新输入");
        }
    }

    private static void userLogin() throws IOException {
        //登录
        while(true){
            Scanner sc = new Scanner(System.in);
            System.out.println("请输入登录用户名");
            String username = sc.nextLine();
            System.out.println("请输入登录密码");
            String password = sc.nextLine();
            //将用户名和密码提交给服务器
            String nameWord=username+"==="+password;
            out.write(nameWord.getBytes());
            //读取服务器的反馈
            byte[] bytes = new byte[1024];
            int len = in.read(bytes);
            String fk = new String(bytes, 0, len);
            if ("yes".equals(fk)) {
                System.out.println("恭喜您登陆成功!");
                break;
            } else {
                System.out.println("用户名或密码输入错误，请重新输入：");
            }
        }
    }

    private static void userRegister() throws IOException {
        //注册
        while(true){
            Scanner sc = new Scanner(System.in);
            System.out.println("请输入注册用户名");
            String username = sc.nextLine();
            System.out.println("请输入注册密码");
            String password = sc.nextLine();
            //将用户名和密码提交服务器
            String nameWord=username+"==="+password;
            out.write(nameWord.getBytes());
            //读取服务器对用户名的反馈
            byte[] bytes = new byte[1024];
            int len = in.read(bytes);
            String fk = new String(bytes, 0, len);
            if ("yes".equals(fk)) {
                System.out.println("恭喜您注册成功!");
                break;
            } else {
                System.out.println("用户名已存在，请重新注册");
            }
        }
    }

    private static void lookTxt() throws IOException {
        System.out.println("输入y查看私聊记录，输入n查看公聊记录.(输入-q退出)");
        Scanner scanner = new Scanner(System.in);
        String way = scanner.next();
        if("y".equalsIgnoreCase(way)){
            System.out.println("请输入要查看的聊天记录的用户名.(输入-q退出)");
            String receiver = scanner.next();
            if(receiver.equalsIgnoreCase("-q")){
                return;
            }
            String msg=receiver+":"+"null"+":"+MsgType.MSG_PRIVATETXT;
            out.write(msg.getBytes());
        }else if("n".equalsIgnoreCase(way)){
            String msg="null"+":"+"null"+":"+MsgType.MSG_PUBLICTXT;
            out.write(msg.getBytes());
        }else if(way.equalsIgnoreCase("-q")){
            return;
        }else{
            System.out.println("输入错误!");
        }
    }

    private static void sendFile() throws IOException {
        //发送文件
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入目标用户");
        String receiver = sc.nextLine();
        System.out.println("请输入文件路径");
        String path = sc.nextLine();
        //封装文件
        File file = new File(path);
        if(file.exists()){
            String msg=receiver+":"+file.getName()+"#"+file.length()+":"+MsgType.MSG_SENDFILE;
            byte[] msgBytes = msg.getBytes();
            byte[] emptyBytes = new byte[1024 * 10 - msgBytes.length];
            byte[] fileBytes = InputAndOutputUtil.readFile(path);
            //把三个小的字节数组合并成一个大的字节数组
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(msgBytes);
            bos.write(emptyBytes);
            bos.write(fileBytes);
            //取出大的字节数组，把大的字节数组发给服务器
            byte[] allBytes = bos.toByteArray();
            out.write(allBytes);
            System.out.println("文件已发送");
        }else{
            System.out.println("文件不存在");
        }
    }

    private static void switchStatus() throws IOException {
        String msg="null"+":"+"null"+":"+MsgType.MSG_SWITCHSTSTUS;
        out.write(msg.getBytes());
    }

    private static void exitTalk() throws IOException {
        String msg="null"+":"+"null"+":"+MsgType.MSG_EXIT;
        out.write(msg.getBytes());
    }

    private static void getOnlineList() throws IOException {
        String msg="null"+":"+"null"+":"+MsgType.MSG_ONLINELIST;
        out.write(msg.getBytes());
    }
    private static void publicTalk() throws IOException {
        while(true){
            Scanner sc = new Scanner(System.in);
            System.out.println("你已进入公聊模式请输入消息内容.(输入-q退出)");
            String msg = sc.nextLine();
            if("-q".equals(msg)){
                break;
            }
            String chattxt= TimeUtil.changeMils2Date(System.currentTimeMillis(),"yyyy-MM-dd HH:mm:ss")+":"+"我给大家发消息"+msg;
            ChatTxt.store("publicTxt",chattxt);
            msg="null"+":"+msg+":"+MsgType.MSG_PUBLIC;
            out.write(msg.getBytes());
        }
    }

    private static void privateTalk() throws IOException {
            Scanner sc = new Scanner(System.in);
            //发送消息
            //约定的格式：  接收者：消息内容：消息类型
            System.out.println("你已进入私聊模式,请输入要私聊的用户");
            String user = sc.nextLine();
            while(true){
                System.out.println("请输入消息内容.(输入-q退出)");
                String msg = sc.nextLine();
                if("-q".equals(msg)){
                    break;
                }
                String chattxt=TimeUtil.changeMils2Date(System.currentTimeMillis(),"yyyy-MM-dd HH:mm:ss")+":"+"我发消息"+msg;
                ChatTxt.store(user, chattxt);//我要给user发消息,所以是以接收者命名的txt
                msg=user+":"+msg+":"+MsgType.MSG_PRIVATE;
                out.write(msg.getBytes());
            }
    }
}
