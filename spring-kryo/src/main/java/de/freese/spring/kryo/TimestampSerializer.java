package de.freese.spring.kryo;

import java.sql.Timestamp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * java.sql.Timestamp wird nicht von Kryo unterstützt bzw. ist es ein Bug (DefaultSerializers.DateSerializer).<br>
 * Beim Serialisieren mit Kryo werden die Nanos nicht mitgenommen.br>
 * Dieser Serializer serialisiert auch den Nanos-Teil eines java.sql.Timestamp.<br>
 *
 * @author Thomas Freese
 */
public class TimestampSerializer extends Serializer<Timestamp>
{
    @Override
    public Timestamp read(Kryo kryo, Input input, Class<? extends Timestamp> type)
    {
        long time = input.readLong();
        int nanos = input.readInt();

        Timestamp timestamp = new Timestamp(time);
        timestamp.setNanos(nanos);

        return timestamp;
    }

    @Override
    public void write(Kryo kryo, Output output, Timestamp obj)
    {
        output.writeLong(obj.getTime());
        output.writeInt(obj.getNanos());
    }
}
