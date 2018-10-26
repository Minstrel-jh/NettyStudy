package io2_nio.server.modle2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class MultiServerReactor implements Runnable {

    private Selector selector;  // 多路复用器

    private ServerSocketChannel serverSocketChannel;

    private volatile boolean stop;

    public MultiServerReactor(int port) {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(port), 1024); // backlog: 请求链接队列的最大长度

            /**
             * 与Selector一起使用时，Channel必须处于非阻塞模式下
             * * 阻塞模式
             *      accept()方法等待连接。然后它接受一个连接，并返回到远程客户端的一个SocketChannel。
             *      在建立连接前你无法做任何操作。
             * * 非阻塞模式
             *      如果没有连接，accept()返回null。非阻塞模式一般和Selector结合使用。
             */
            serverSocketChannel.configureBlocking(false);

            /**
             * 将ServerSocketChannel注册到Selector，监听ACCEPT事件
             * 1 SelectionKey.OP_CONNECT    连接就绪
             * 2 SelectionKey.OP_ACCEPT     接收就绪
             * 3 SelectionKey.OP_READ       读就绪
             * 4 SelectionKey.OP_WRITE      写就绪
             */
            SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            selectionKey.attach(new Acceptor(selector, serverSocketChannel));
            System.out.println("服务器监听：OP_ACCEPT; port:" + port);
        } catch (IOException e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

    public void stop() {
        this.stop = true;
    }

    @Override
    public void run() {
        while (!stop) {
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
                        SelectionKey key = it.next();
                        /**
                         * Selector不会自己从已选择键集中移除SelectionKey实例。必须在处理完通道时自己移除。
                         * 下次该通道变成就绪时，Selector会再次将其放入已选择键集中。
                         */
                        it.remove();
                        dispatch(key);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace(System.out);
            }
        }

        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace(System.out);
            }
        }
    }

    private void dispatch(SelectionKey key) {
        Runnable r = (Runnable)(key.attachment());
        if (r != null){
            r.run();
        }
    }
}
