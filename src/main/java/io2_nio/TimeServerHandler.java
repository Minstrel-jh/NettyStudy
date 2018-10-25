package io2_nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TimeServerHandler implements Runnable {

    private Selector selector;  // 多路复用器

    private ServerSocketChannel serverSocketChannel;

    private volatile boolean stop;

    public TimeServerHandler(int port) {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();

            /**
             * 与Selector一起使用时，Channel必须处于非阻塞模式下
             * * 阻塞模式
             *      accept()方法等待连接。然后它接受一个连接，并返回到远程客户端的一个SocketChannel。
             *      在建立连接前你无法做任何操作。
             * * 非阻塞模式
             *      如果没有连接，accept()返回null。非阻塞模式一般和Selector结合使用。
             */
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(port), 1024); // backlog: 请求链接队列的最大长度

            /**
             * 将ServerSocketChannel注册到Selector，监听ACCEPT事件
             * 1 SelectionKey.OP_CONNECT    连接就绪
             * 2 SelectionKey.OP_ACCEPT     接收就绪
             * 3 SelectionKey.OP_READ       读就绪
             * 4 SelectionKey.OP_WRITE      写就绪
             */
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务器监听：OP_ACCEPT; port:" + port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void stop() {
        this.stop = true;
    }

    @Override
    public void run() {
        while (!stop) {
            SelectionKey key = null;
            try {
                /**
                 * 选择通道
                 * selector.select();       阻塞到至少有一个通道在你注册的事件上就绪了。
                 * selector.select(1000);   和select()一样，除了最长会阻塞timeout毫秒(参数)。
                 * selector.selectNow();    不会阻塞，不管什么通道就绪都立刻返回，如果无通道就绪，则立即返回零
                 */
                System.out.println("select() 阻塞...");
                int keysNum = selector.select();
                if (keysNum != 0) {
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> it = selectedKeys.iterator();

                    while (it.hasNext()) {
                        key = it.next();
                        /**
                         * Selector不会自己从已选择键集中移除SelectionKey实例。必须在处理完通道时自己移除。
                         * 下次该通道变成就绪时，Selector会再次将其放入已选择键集中。
                         */
                        it.remove();
                        handleInput(key);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (key != null) {
                    key.cancel();
                }
            }
        }

        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            if (key.isAcceptable()) {
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel sc = ssc.accept();
                sc.configureBlocking(false);
                sc.register(selector, SelectionKey.OP_READ);
                System.out.println("服务器监听：OP_READ");
            }
            if (key.isReadable()) {
                SocketChannel sc = (SocketChannel) key.channel();
                /**
                 * Buffer的3个重要属性：
                 * 1 capacity   容量
                 * 2 position   位置
                 * 3 limit      上限
                 */
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = sc.read(readBuffer);
                if (readBytes >= 0) {
                    /**
                     * 调用flip()之后，读/写指针position指到缓冲区头部，
                     * limit在读模式时，是之前写模式的position所在位置
                     */
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes, "UTF-8");

                    System.out.println("client msg:" + body);

                    doWrite(sc, "服务器已经收到客户端消息");
                } else if (readBytes < 0) {
                    // 对端链路关闭
                    key.cancel();
                    sc.close();
                } else {
                    // 读到0字节，不做处理
                }
            }
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
