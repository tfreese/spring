package de.freese.spring.kryo;

import java.sql.Timestamp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * {@link Timestamp} wird nicht von Kryo unterstützt bzw. ist es ein Bug (DefaultSerializers.DateSerializer).<br>
 * Beim Serialisieren mit Kryo werden die Nanos nicht mitgenommen.<br>
 * Dieser Serializer serialisiert auch den Nanos-Teil eines java.sql.Timestamp.<br>
 *
 * @author Thomas Freese
 */
public class TimestampSerializer extends Serializer<Timestamp> {
    //    public TimestampSerializer()
    //    {
    //        super(true);
    //    }
    //
    //    @Override
    //    public Timestamp copy(final Kryo kryo, final Timestamp original)
    //    {
    //        if (original == null)
    //        {
    //            return null;
    //        }
    //
    //        Timestamp timestamp = new Timestamp(original.getTime());
    //        timestamp.setNanos(original.getNanos());
    //
    //        return timestamp;
    //    }

    @Override
    public Timestamp read(final Kryo kryo, final Input input, final Class<? extends Timestamp> type) {
        final long time = input.readLong(false);

        if (time == -1L) {
            return null;
        }

        final int nanos = input.readInt(false);

        final Timestamp timestamp = new Timestamp(time);
        timestamp.setNanos(nanos);

        return timestamp;
    }

    @Override
    public void write(final Kryo kryo, final Output output, final Timestamp obj) {
        if (obj == null) {
            output.writeLong(-1L, false);
        }
        else {
            output.writeLong(obj.getTime(), false);
            output.writeInt(obj.getNanos(), false);
        }
    }
}
