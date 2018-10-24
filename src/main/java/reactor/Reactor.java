package reactor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class Reactor implements Runnable {

    public final Selector selector;
    public final ServerSocketChannel serverSocketChannel;

    public Reactor(int port) throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();

        InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost(), port);
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(inetSocketAddress);

        SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);



    }

    @Override
    public void run() {

    }
}
