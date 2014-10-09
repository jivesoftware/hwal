package com.jivesoftware.os.hwal.permit;

import com.jivesoftware.os.rcvs.marshall.api.TypeMarshaller;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class PermitMarshaller implements TypeMarshaller<Permit> {

    private final Charset utf_8 = Charset.forName("UTF-8");

    @Override
    public Permit fromBytes(byte[] bytes) throws Exception {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long issued = bb.getLong();
        long expires = bb.getLong();

        int id = bb.getInt();

        int length = bb.getInt();
        byte[] ownerBytes = new byte[length];
        bb.get(ownerBytes);

        length = bb.getInt();
        byte[] tenantBytes = new byte[length];
        bb.get(tenantBytes);

        length = bb.getInt();
        byte[] poolBytes = new byte[length];
        bb.get(poolBytes);

        return new Permit(issued, expires, id, new String(ownerBytes, utf_8), new String(tenantBytes, utf_8), new String(poolBytes, utf_8));
    }

    @Override
    public byte[] toBytes(Permit permit) throws Exception {
        if (permit == null) {
            return null;
        }
        byte[] ownerBytes = permit.owner.getBytes(utf_8);
        byte[] tenantBytes = permit.tenantId.getBytes(utf_8);
        byte[] poolBytes = permit.pool.getBytes(utf_8);
        ByteBuffer buffer = ByteBuffer.allocate(8 + 8 + 4 + 4 + ownerBytes.length + 4 + tenantBytes.length + 4 + poolBytes.length);

        buffer.putLong(permit.issuedAtTimeInMillis);
        buffer.putLong(permit.expiresInNMillis);

        buffer.putInt(permit.id);

        buffer.putInt(ownerBytes.length);
        buffer.put(ownerBytes);

        buffer.putInt(tenantBytes.length);
        buffer.put(tenantBytes);

        buffer.putInt(poolBytes.length);
        buffer.put(poolBytes);
        return buffer.array();
    }

    @Override
    public Permit fromLexBytes(byte[] bytes) throws Exception {
        return fromBytes(bytes);
    }

    @Override
    public byte[] toLexBytes(Permit permit) throws Exception {
        return toBytes(permit);
    }
}
