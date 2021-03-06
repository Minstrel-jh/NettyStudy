package io1_bio.server.model2;

import io1_bio.server.handler.HandlerThreadPool;
import io1_bio.server.handler.TimeServerHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 伪异步模型
 */
public class TimeServer {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {}
        }
        ServerSocket server = null;

        try {
            server = new ServerSocket(port);
            System.out.println("The time server is start in port:" + port);
            Socket socket = null;
            HandlerThreadPool handlerThreadPool
                = new HandlerThreadPool(50, 10000); // 创建I/O线程池

            while (true) {
                socket = server.accept(); // 此处会阻塞
                handlerThreadPool.execute(new TimeServerHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        } finally {
            if (server != null) {
                System.out.println("The time server close");
                server.close();
                server = null;
            }
        }
    }
}
