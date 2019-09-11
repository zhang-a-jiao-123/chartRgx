package chatRoom;

import config.MsgType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class serverThread extends Thread{
    HashMap<String, Socket> hm;
    String username;
    Socket sk;
    ArrayList<String> list;
    boolean isHidden=true;//true代表在线，false代表隐身
    public serverThread(HashMap<String, Socket> hm, String username, Socket sk, ArrayList<String> list) {
        this.hm=hm;
        this.username=username;
        this.sk=sk;
        this.list=list;
    }

    @Override
    public void run() {
        try {
            InputStream in = sk.getInputStream();
            OutputStream out = sk.getOutputStream();
            while(true){
                //服务器接收消息
                byte[] bytes = new byte[1024*10];
                int len = in.read(bytes);
                String msg = new String(bytes, 0, len).trim();
                //客户端发来的消息格式：接收者：消息内容：消息类型
                //截取客户端发来的消息，根据消息类型做不同的处理
                String[] msgs = msg.split(":");
                String receiver = msgs[0];
                String msgContent = msgs[1];
                int msgType = Integer.parseInt(msgs[2]);
                if(msgType==MsgType.MSG_PRIVATE){
                    //私聊拿出接收者的管道输出流，把消息重新组拼一下写个他
                    //转发格式：发送者：消息内容：消息类型：时间
                    String zfMsg=username+":"+msgContent+":"+MsgType.MSG_PRIVATE+":"+System.currentTimeMillis();
                    if(hm.get(receiver)!=null){
                        hm.get(receiver).getOutputStream().write(zfMsg.getBytes());
                    }
                }else if(msgType==MsgType.MSG_PUBLIC){
                    //公聊：取出所有人的管道，发给每一个人
                    Set<String> keySet = hm.keySet();
                    for (String key : keySet) {
                        if(key.equals(username)){
                            continue;//排除自己
                        }
                        String zfMsg=username+":"+msgContent+":"+MsgType.MSG_PUBLIC+":"+System.currentTimeMillis();
                        hm.get(key).getOutputStream().write(zfMsg.getBytes());
                    }
                }else if(msgType==MsgType.MSG_ONLINELIST){
                    //给客户端返回在线列表
                    StringBuffer sb = new StringBuffer();
                    int i=1;
                    for (String key : list) {
                        if(key.equals(username)){
                            continue;
                        }
                        sb.append((i++)).append(".").append(key).append("\n");
                    }
                    String zfMsg=username+":"+sb.toString()+":"+MsgType.MSG_ONLINELIST+":"+System.currentTimeMillis();
                    hm.get(username).getOutputStream().write(zfMsg.getBytes());
                }else if (msgType==MsgType.MSG_EXIT){
                    //给其他人发送下线提醒
                    Set<String> keySet = hm.keySet();
                    for (String key : keySet) {
                        if(key.equals(username)){
                            continue;
                        }
                        String zfMsg=username+":"+"下线了"+":"+MsgType.MSG_EXIT+":"+System.currentTimeMillis();
                        hm.get(key).getOutputStream().write(zfMsg.getBytes());
                    }
                    break;
                }else if(msgType==MsgType.MSG_SWITCHSTSTUS){
                    if(isHidden){
                        //在线——隐身
                        list.remove(username);
                    }else{
                        //隐身——在线
                        list.add(username);
                        Set<String> keySet = hm.keySet();
                        for (String key : keySet) {
                            if(key.equals(username)){
                                continue;
                            }
                            String zfMsg=username+":"+"又上线了"+":"+MsgType.MSG_ONLINE+":"+System.currentTimeMillis();
                            hm.get(key).getOutputStream().write(zfMsg.getBytes());
                        }
                    }
                    isHidden=!isHidden;//更改开关状态
                }else if(msgType==MsgType.MSG_SENDFILE){
                    //服务端处理文件
                    String[] fileInfo = msgContent.split("#");
                    String fileName = fileInfo[0];
                    long fileLength = Long.parseLong(fileInfo[1]);
                    //拼接要转发回去的信息
                    String zfMsg=username+":"+msgContent+":"+MsgType.MSG_SENDFILE+":"+System.currentTimeMillis();
                    byte[] msgBytes = zfMsg.getBytes();
                    byte[] emptyBytes = new byte[1024 * 10 - msgBytes.length];
                    //服务端读取文件信息
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] cathchBytes = new byte[1024 * 8];
                    int cathchLen=0;
                    while(true){
                        int lens = in.read(cathchBytes);
                        bos.write(cathchBytes,0,lens);
                        cathchLen+=lens;
                        if(cathchLen==fileLength){
                            break;
                        }
                    }
                    byte[] fileBytes = bos.toByteArray();//把文件字节数组读取出来
                    bos.reset();//重置
                    bos.write(msgBytes);
                    bos.write(emptyBytes);
                    bos.write(fileBytes);
                    byte[] allBytes = bos.toByteArray();//取出大的字节数组
                    hm.get(receiver).getOutputStream().write(allBytes);//转发回去
                }else if(msgType==MsgType.MSG_PRIVATETXT){
                    //私聊记录
                    String zfMsg=receiver+":"+"这是你和"+receiver+"的聊天记录"+":"+MsgType.MSG_PRIVATETXT+":"+System.currentTimeMillis();
                    hm.get(username).getOutputStream().write(zfMsg.getBytes());
                }else if(msgType==MsgType.MSG_PUBLICTXT){
                    //公聊记录
                    String zfMsg=username+":"+"这是和大家的聊天记录"+":"+MsgType.MSG_PUBLICTXT+":"+System.currentTimeMillis();
                    hm.get(username).getOutputStream().write(zfMsg.getBytes());
                }
            }
            //关闭集合中下线者的管道
            //移除管道中下线者的名字
            hm.get(username).close();
            hm.remove(username);
            list.remove(username);
        } catch(SocketException e){
            //客户端下线后，会抛出一个SocketException异常,但是会在控制台打印异常信息，
            //所以我们捕获一下，做空处理
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e){
            System.out.println(username+"输入的消息格式错误,服务器断开了与他的连接");
        }
    }
}
