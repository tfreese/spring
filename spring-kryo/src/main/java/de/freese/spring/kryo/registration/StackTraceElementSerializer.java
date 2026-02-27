// Created: 27.02.2026
package de.freese.spring.kryo.registration;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Durch unterschiedlichen JVMs im Client und Server könnte die Serialisierung der StackTraceElement Objekte nicht zuverlässig funktionieren.<br>
 *
 * @author Thomas Freese
 */
class StackTraceElementSerializer extends Serializer<StackTraceElement> {
    @Override
    public java.lang.StackTraceElement read(final Kryo kryo, final Input input, final Class<? extends java.lang.StackTraceElement> type) {
        final String cls = input.readString();

        if (cls == null) {
            return null;
        }

        final String method = input.readString();
        final String file = input.readString();
        final int line = input.readInt();

        return new java.lang.StackTraceElement(cls, method, file, line);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final java.lang.StackTraceElement obj) {
        if (obj == null) {
            output.writeString(null);
        }
        else {
            output.writeString(obj.getClassName());
            output.writeString(obj.getMethodName());
            output.writeString(obj.getFileName());
            output.writeInt(obj.getLineNumber());
        }
    }
}
