package io2_nio.server.modle2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ReadHandler implements Runnable {

    private SocketChannel socketChannel;

    private SelectionKey selectionKey;

    public ReadHandler(SocketChannel socketChannel, SelectionKey selectionKey) {
        this.socketChannel = socketChannel;
        this.selectionKey = selectionKey;
    }

    @Override
    public void run() {
        try {
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            int readBytes = socketChannel.read(readBuffer);
            if (readBytes > 0) {
                /**
                 * 调用flip()之后，读/写指针position指到缓冲区头部，
                 * limit在读模式时，是之前写模式的position所在位置
                 */
                readBuffer.flip();
                byte[] bytes = new byte[readBuffer.remaining()];
                readBuffer.get(bytes);
                String body = new String(bytes, "UTF-8");

                System.out.println("客户端消息：" + body);

                doWrite(socketChannel, "服务器已经收到客户端消息：" + body);
            } else if (readBytes < 0) {
                // 对端链路关闭
                selectionKey.cancel();
                socketChannel.close();
            } else {
                // 读到0字节，不做处理
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
            selectionKey.cancel();
        }
    }

    private void doWrite(SocketChannel channel, String response) throws IOException {
        if (response != null && response.trim().length() > 0) {
            byte[] bytes = response.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            writeBuffer.flip();
            channel.write(writeBuffer);
        }
    }
}
