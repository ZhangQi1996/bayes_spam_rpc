package com.zq.rpc;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TFastFramedTransport;
import org.apache.thrift.transport.TSocket;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Objects;

/**
 * 这个是rpc使用的示例代码
 * */
public class Cli {

    // 注意这个文件是gbk编码
    private static final String FILE_PATH = getFilePathByDefaultClassLoader("normal.txt");

    private static final String HOST = "localhost";

    private static final int PORT = 8899;

    private static String getFilePathByDefaultClassLoader(String fileName) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL url = Objects.requireNonNull(cl,
                String.format("By classloader %s, cannot find dest file.\n", cl))
                .getResource(fileName);

        if (null == url)
            return null;
        return url.getPath();
    }

    private static void close(Closeable closeable) throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }

    private static RpcResult query(String fileName) throws TException, IOException {
        TFastFramedTransport transport =
                new TFastFramedTransport(new TSocket(HOST, PORT), 600);
        TCompactProtocol protocol = new TCompactProtocol(transport);
        MailQueryService.Client client = new MailQueryService.Client(protocol);

        FileChannel fileChannel = null;
        RpcResult result = null;

        try {
            transport.open();

            fileChannel = new RandomAccessFile(FILE_PATH, "r").getChannel();
            long fileSize = fileChannel.size();

            ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);

            while (fileChannel.read(buffer) != 0) ; // 将文件内容写入buffer中

            buffer.flip();

            // RpcResult(status_code:0, clazz:0), status_code为0时表示服务正常，1时表示服务出错，
            // clazz为0表示正常邮件，为1表示垃圾邮件
            result = client.queryMailClass(buffer);
        } finally {
            close(fileChannel);
            close(transport);
        }

        return result;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(query(FILE_PATH).toString());
    }
}
