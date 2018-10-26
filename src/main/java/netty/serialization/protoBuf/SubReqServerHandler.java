package netty.serialization.protoBuf;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import netty.serialization.protoBuf.proto.SubscribeReqProto;
import netty.serialization.protoBuf.proto.SubscribeRespProto;

public class SubReqServerHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SubscribeReqProto.SubscribeReq req = (SubscribeReqProto.SubscribeReq) msg;
        if ("JiangHan".equalsIgnoreCase(req.getUserName())) {
            System.out.println("Server accept client subscribe req : [" + req + "]");
            ctx.writeAndFlush(resp(req.getSubReqID()));
        }
    }

    private static SubscribeRespProto.SubscribeResp resp(int subReqID) {
        SubscribeRespProto.SubscribeResp.Builder builder = SubscribeRespProto.SubscribeResp.newBuilder();
        builder.setSubReqID(subReqID);
        builder.setRespCode(0);
        builder.setDesc("Server return");

        return builder.build();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace(System.out);
        ctx.close();
    }
}
