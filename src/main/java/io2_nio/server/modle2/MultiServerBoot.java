package io2_nio.server.modle2;

public class MultiServerBoot {
    public static void main(String[] args) {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace(System.out);
            }
        }
        MultiServerReactor multiServerReactor = new MultiServerReactor(port);
        new Thread(multiServerReactor, "MultiServerReactor").start();
    }
}
