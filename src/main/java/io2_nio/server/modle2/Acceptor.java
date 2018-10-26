package io2_nio.server.modle2;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Acceptor implements Runnable {

    private Selector selector;  // 多路复用器

    private ServerSocketChannel serverSocketChannel;

    public Acceptor(Selector selector, ServerSocketChannel serverSocketChannel) {
        this.selector = selector;
        this.serverSocketChannel = serverSocketChannel;
    }

    @Override
    public void run() {
        try {
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
            selectionKey.attach(new ReadHandler(socketChannel, selectionKey));
            System.out.println("服务器监听：OP_READ");
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}
