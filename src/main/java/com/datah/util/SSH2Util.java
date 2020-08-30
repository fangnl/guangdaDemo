package com.datah.util;

import com.jcraft.jsch.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import java.io.*;


public class SSH2Util {
    static Logger logger = LogUtil.getLogger();
    private String host;

    private String user;

    private String password;

    private int port;

    private Session session;

    /**
     * 创建一个连接
     *
     * @param host     地址
     * @param user     用户名
     * @param password 密码
     * @param port     ssh2端口
     */
    public SSH2Util(String host, String user, String password, int port) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.port = port;
        logger.info("用户{},连接主机{},端口号{}", user, host, port);

    }

    private void initialSession() throws Exception {
        if (session == null) {
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, port);
            session.setUserInfo(new UserInfo() {

                public String getPassphrase() {
                    return null;
                }

                public String getPassword() {
                    return null;
                }

                public boolean promptPassword(String arg0) {
                    return false;
                }

                public boolean promptPassphrase(String arg0) {
                    return false;
                }

                public boolean promptYesNo(String arg0) {
                    return true;
                }

                public void showMessage(String arg0) {
                }

            });
            session.setPassword(password);
            session.connect();
        }
    }

    /**
     * 关闭连接
     */
    public void close() throws Exception {
        if (session != null && session.isConnected()) {
            logger.info("关闭连接{}", session.getUserName());
            session.disconnect();
            session = null;
        }


    }

    /**
     * @param shellFile  shell 本地文件
     * @param remotePath shell 远程文件存储位置
     * @throws Exception
     */
    public void putFile(String shellFile, String remotePath) {
        try {
            this.initialSession();
            Channel channelSftp = session.openChannel("sftp");
            channelSftp.connect();
            ChannelSftp c = (ChannelSftp) channelSftp;

            //创建目录
            try {
                c.rm(remotePath);
                logger.info("文件{}已经存在，删除文件", remotePath);
            } catch (Exception e) {
                logger.info("删除文件{}失败", remotePath);
                //System.out.println("文件不存在");
            }

            try {
                c.mkdir(remotePath.substring(0, (remotePath.lastIndexOf("/"))));
                logger.info("创建文件夹{}", remotePath.substring(0, (remotePath.lastIndexOf("/"))));
            } catch (Exception e) {
                //System.out.println("目录已经存在");
            }

            //写入目标文件
            ByteArrayInputStream inputStream = new ByteArrayInputStream(shellFile.getBytes());
            c.put(inputStream, remotePath);
            inputStream.close();
            channelSftp.disconnect();
        } catch (Exception e) {
            try {
                close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }

        }
    }


    // command 命令
    public void runCommand(String command) throws Exception {

        this.initialSession();
        InputStream in = null;
        InputStream err = null;
        int time = 0;
        boolean flagIn = false;
        boolean flagErr = false;

        //执行命令
        ChannelExec channel = (ChannelExec) session.openChannel("exec");

        channel.setCommand(command);

        channel.setInputStream(null);

        channel.setErrStream(null);

        //输入流信息
        in = channel.getInputStream();
        //error信息
        err = channel.getErrStream();
        channel.connect();
        logger.info("执行脚本{}", command);
        BufferedReader inReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        BufferedReader errReader = new BufferedReader(new InputStreamReader(err, "UTF-8"));
        String sin = null;
        while (true) {
            try {
                sin = inReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                logger.error(e.getMessage());
            }
            if (StringUtils.isNotEmpty(sin) || sin != null) {
                logger.info(sin);
            } else {
                flagIn = true;
                break;
            }
        }
        String serr = null;
        while (true) {
            try {
                serr = errReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                logger.error(e.getMessage());
            }
            if (StringUtils.isNotEmpty(serr) || serr != null) {
                logger.info(serr);
            } else {
                flagErr = true;
                break;
            }
        }


        while (true) {
            if (channel.isClosed() || (flagErr && flagIn == true)) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ee) {
            }
            if (time > 180) {
                break;
            }
            time++;
        }

        in.close();
        err.close();
        inReader.close();
        errReader.close();
        channel.disconnect();
        session.disconnect();
        //输出的结果
        logger.info("关闭linux channel连接");
    }


}
