package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class MultiplexerTimeServer implements Runnable {

    private Selector selector;  // 多路复用器

    private ServerSocketChannel servChannel;

    private volatile boolean stop;

    public MultiplexerTimeServer(int port) {
        try {
            selector = Selector.open();
            servChannel = ServerSocketChannel.open();                                                                   // 用来监听所有客户端的链接
            servChannel.configureBlocking(false);                                                                       // 把Channel设置为异步非阻塞
            /**
             * 与Selector一起使用时，Channel必须处于非阻塞模式下
             */
            servChannel.socket().bind(new InetSocketAddress(port), 1024);                                      // backlog: 请求链接队列的最大长度
            servChannel.register(selector, SelectionKey.OP_ACCEPT);                                                     // 将ServerSocketChannel注册到Selector，监听ACCEPT事件
            /**
             * 1 某个channel成功连接到另一个服务器称为“连接就绪”。
             * 2 一个server socket channel准备好接收新进入的连接称为“接收就绪”。
             * 3 一个有数据可读的通道可以说是“读就绪”。
             * 4 等待写数据的通道可以说是“写就绪”
             */
            System.out.println("The time server is start in port : " + port);
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
        while (!stop) {                                                                                                 // boolean默认值是0吧，所以没有初始化的地方，用的默认值
            try {
                int cNum = selector.select(1000);                                                              // 查找准备就绪的Channel,返回有多少个通道
                /**
                 * 如果 timeout为正，则select(long timeout)在等待有通道被选择时至多会阻塞timeout毫秒
                 * 如果timeout为零，则永远阻塞直到有至少一个通道准备就绪。
                 * timeout不能为负数
                 */
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectedKeys.iterator();

                SelectionKey key = null;
                while (it.hasNext()) {
                    key = it.next();
                    it.remove();
                    /**
                     * Selector不会自己从已选择键集中移除SelectionKey实例。必须在处理完通道时自己移除。
                     * 下次该通道变成就绪时，Selector会再次将其放入已选择键集中。
                     */
                    handleInput(key);
                }
            } catch (IOException e) {
                e.printStackTrace();
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
        if (key.isValid()) {                                                                                            // 判断key是否有效
            if (key.isAcceptable()) {                                                                                   // 判断servChannel是否有“接收就绪”的socketChannel
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                SocketChannel sc = ssc.accept();                                                                        // 获取这个socketChannel
                sc.configureBlocking(false);                                                                            // 设置为异步
                sc.register(selector, SelectionKey.OP_READ);                                                            // 把这个socketChannel注册到selector，并设置感兴趣的就绪事件
            }
            if (key.isReadable()) {                                                                                     // 判断key的channel是否“读就绪”
                SocketChannel sc = (SocketChannel) key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);                                                      // 为ByteBuffer分配容量(capacity)
                int readBytes = sc.read(readBuffer);
                if (readBytes > 0) {
                    readBuffer.flip();
                    /**
                     * 调用flip()之后，读/写指针position指到缓冲区头部，
                     * 并且设置了最多只能读出之前写入的数据长度(而不是整个缓存的容量大小)
                     */
                    byte[] bytes = new byte[readBuffer.remaining()];                                                    // 创建一个大小为Buffer内剩余内容的长度的Byte数组
                    readBuffer.get(bytes);                                                                              // 从readBuffer内取数据
                    String body = new String(bytes, "UTF-8");
                    System.out.println("The time server receive order : " + body);
                    String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body)
                        ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
                    doWrite(sc, currentTime);
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
