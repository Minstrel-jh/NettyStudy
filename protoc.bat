set path=.\src\main\java
set src=.\src\main\java\netty\serialization\protoBuf

protoc.exe --java_out=%path% %src%\proto\SubscribeReq.proto

protoc.exe --java_out=%path% %src%\proto\SubscribeResp.proto