package io2_nio.server.modle1;

public class ServerBoot {
    public static void main(String[] args) {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace(System.out);
            }
        }
        ServerReactor serverReactor = new ServerReactor(port);
        new Thread(serverReactor, "ServerReactor").start();
    }
}
