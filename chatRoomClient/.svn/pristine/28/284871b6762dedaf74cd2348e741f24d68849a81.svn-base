package chatRoom;

import config.ChatTxt;
import config.MsgType;
import utils.InputAndOutputUtil;
import utils.TimeUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

public class clientThread  extends Thread{
    InputStream in;

    public boolean isSave() {
        return isSave;
    }

    public void setSave(boolean save) {
        isSave = save;
    }

    public boolean isColse() {
        return isColse;
    }

    public void setColse(boolean colse) {
        isColse = colse;
    }

    private volatile boolean isSave=true;
    private volatile boolean isColse=false;
    public clientThread(InputStream in) {
        this.in=in;
    }

    @Override
    public void run() {
        //读取服务器转发过来的消息
        try {
            while(true){
                byte[] bytes = new byte[1024*10];
                int len = in.read(bytes);
                String msg = new String(bytes, 0, len).trim();
                //转发格式：发送者：消息内容：消息类型：时间
                String[] msgs = msg.split(":");
                String sender = msgs[0];
                String msgContent = msgs[1];
                int msgType = Integer.parseInt(msgs[2]);
                long date = Long.parseLong(msgs[3]);
                String time = TimeUtil.changeMils2Date(date, "yyyy-MM-dd HH:mm:ss");
                if(msgType== MsgType.MSG_PRIVATE){
                    System.out.println(time);
                    System.out.println(sender+"给你发来了消息："+msgContent);
                    String chattxt=time+": "+sender+"给你发来消息: "+msg;
                    ChatTxt.store(sender,chattxt);//收到sender的消息,所以存为发给我消息的人为名的txt
                }else if(msgType==MsgType.MSG_PUBLIC){
                    System.out.println(time);
                    System.out.println(sender+"给大家发来了消息："+msgContent);
                    String chattxt=time+": "+sender+"给大家发来消息: "+msg;
                    ChatTxt.store("publicTxt",chattxt);
                }else if(msgType==MsgType.MSG_ONLINELIST){
                    System.out.println(time);
                    System.out.println(msgContent);
                }else if(msgType==MsgType.MSG_EXIT){
                    System.out.println(time);
                    System.out.println(sender+":"+msgContent);
                }else if(msgType==MsgType.MSG_ONLINE){
                    System.out.println(time);
                    System.out.println(sender+":"+msgContent);
                }else if(msgType==MsgType.MSG_SENDFILE){
                    System.out.println(time);
                    String[] fileInfo = msgContent.split("#");
                    String fileName = fileInfo[0];
                    long fileLength = Long.parseLong(fileInfo[1]);
                    System.out.println(sender+"给你发来一个文件，文件名："+fileName+"大小是"+fileLength/1024.0+"kb");
                    System.out.println("你是否接收y/n");
                    while (isSave()){
                        if(isColse()){
                            break;
                        }
                    }
                    if(isColse){
                        //不保存
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] cathchBytes = new byte[1024 * 8];
                        int cathchlen=0;
                        while(true){
                            int lens = in.read(cathchBytes);
                            bos.write(cathchBytes,0,lens);
                            cathchlen+=lens;
                            if(cathchlen==fileLength){
                                break;
                            }
                        }
                        System.out.println("您已拒收该文件!");
                    }else{
                        //保存
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] cathchBytes = new byte[1024 * 8];
                        int cathchlen=0;
                        while(true){
                            int lens = in.read(cathchBytes);
                            bos.write(cathchBytes,0,lens);
                            cathchlen+=lens;
                            if(cathchlen==fileLength){
                                break;
                            }
                        }
                        byte[] fileBytes = bos.toByteArray();//把文件字节数组取出来
                        boolean b = InputAndOutputUtil.writeFile("E:\\" + fileName, fileBytes);
                        if(b){
                            System.out.println("文件保存成功！在"+"E:\\"+fileName);
                        }else{
                            System.out.println("文件保存失败");
                        }
                    }
                    //重置
                    isSave=true;
                    isColse=false;
                }else if(msgType==MsgType.MSG_PRIVATETXT){
                    String chatStr = ChatTxt.load(sender);
                    System.out.println(chatStr);
                }else if(msgType==MsgType.MSG_PUBLICTXT){
                    String chatStr = ChatTxt.load("publicTxt");
                    System.out.println(chatStr);
                }
            }
        } catch (SocketException e){
            //客户端下线后，会抛出一个SocketException异常,但是会在控制台打印异常信息，
            //所以我们捕获一下，做空处理
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
