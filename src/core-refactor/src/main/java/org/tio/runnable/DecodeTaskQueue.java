package org.tio.runnable;

import lombok.extern.slf4j.Slf4j;
import org.tio.common.CoreConstant;
import org.tio.common.packet.AbstractPacket;
import org.tio.common.ChannelContext;
import org.tio.common.packet.SuperPacket;
import org.tio.util.CheckSumUtil;

import java.nio.ByteBuffer;

/**
 * Copyright (c) for darkidiot
 * Date:2017/8/20
 * Author: <a href="darkidiot@icloud.com">darkidiot</a>
 * Desc:  t-io解码器
 */
@Slf4j
public class DecodeTaskQueue extends AbstractTaskQueue<ByteBuffer> {

    private ChannelContext context;

    public DecodeTaskQueue(ChannelContext context) {
        this.context = context;
    }

    @Override
    public void runTask(ByteBuffer byteBuffer) throws InterruptedException {
        AbstractPacket packet = new AbstractPacket();

        while (byteBuffer.remaining() > 1) {
            short magic = byteBuffer.getShort();
            if (SuperPacket.magic == magic) {
                break;
            }
        }

        while (!byteBuffer.hasRemaining()) {
            byteBuffer = mergeByteBuffer(byteBuffer);
        }
        byte packetType = byteBuffer.get();
        packet.setPacketType(packetType);

        while (!byteBuffer.hasRemaining()) {
            byteBuffer = mergeByteBuffer(byteBuffer);
        }
        byte reserved = byteBuffer.get();
        packet.setReserved(reserved);

        while (!byteBuffer.hasRemaining()) {
            byteBuffer = mergeByteBuffer(byteBuffer);
        }
        byte optLen = byteBuffer.get();
        packet.setOptionalLength(optLen);

        while (!(byteBuffer.remaining() > 2)) {
            byteBuffer = mergeByteBuffer(byteBuffer);
        }
        short bodyLen = byteBuffer.get();
        packet.setBodyLength(bodyLen);

        while (!byteBuffer.hasRemaining()) {
            byteBuffer = mergeByteBuffer(byteBuffer);
        }
        byte checkSum = byteBuffer.get();
        packet.setOptionalLength(checkSum);

        while (!(byteBuffer.remaining() > optLen)) {
            byteBuffer = mergeByteBuffer(byteBuffer);
        }
        byte[] optData = new byte[optLen];
        byteBuffer.get(optData, 0, optLen);
        packet.setOptional(optData);

        while (!(byteBuffer.remaining() > 2)) {
            byteBuffer = mergeByteBuffer(byteBuffer);
        }
        short packetSeq = byteBuffer.getShort();
        packet.setPacketSeq(packetSeq);

        while (!(byteBuffer.remaining() > bodyLen - 2)) {
            byteBuffer = mergeByteBuffer(byteBuffer);
        }
        byte[] bodyData = new byte[bodyLen - 2];
        byteBuffer.get(bodyData, 0, bodyLen - 2);
        packet.setBody(bodyData);

        if (byteBuffer.hasRemaining()) {
            boolean flag = msgQueue.offerFirst(byteBuffer.slice());
            if (!flag) {
                log.warn("put the left ByteBuffer into msgQueue failed.");
            }
        }
        if (context.isUse_checksum()) {
            byte[][] bytes = {
                    packet.header(), optData, new byte[]{(byte) (packetSeq >> 8), (byte) packetSeq}, bodyData
            };
            if (CheckSumUtil.judgeCheckSum(bytes)) {
                context.getHandlerRunnable().addMsg(packet);
            } else {
                log.warn("validate checkSum failed, and the packet[{}] will be discard.", packet.toString());
            }
        }
    }

    private ByteBuffer mergeByteBuffer(ByteBuffer byteBuffer) throws InterruptedException {
        int remaining = byteBuffer.remaining();
        ByteBuffer nextBuffer = msgQueue.take();
        int capacity = remaining + nextBuffer.capacity();
        ByteBuffer newByteBuffer = ByteBuffer.allocateDirect(capacity);
        newByteBuffer.put(byteBuffer.slice());
        newByteBuffer.put(nextBuffer);
        newByteBuffer.flip();
        return newByteBuffer;
    }
}