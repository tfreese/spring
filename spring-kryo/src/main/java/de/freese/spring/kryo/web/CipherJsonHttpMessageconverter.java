/**
 * Created: 15.10.2019
 */

package de.freese.spring.kryo.web;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Thomas Freese
 */
public class CipherJsonHttpMessageconverter extends AbstractHttpMessageConverter<Object>
{
    /**
     *
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     *
     */
    private final ObjectMapper objectMapper;

    /**
     * Erstellt ein neues {@link CipherJsonHttpMessageconverter} Object.
     *
     * @param objectMapper {@link ObjectMapper}
     */
    public CipherJsonHttpMessageconverter(final ObjectMapper objectMapper)
    {
        super(MediaType.APPLICATION_JSON, new MediaType("application", "*+json", DEFAULT_CHARSET));

        this.objectMapper = objectMapper;

    }

    /**
     * @param inputStream {@link InputStream}
     * @return {@link InputStream}
     */
    private InputStream decrypt(final InputStream inputStream)
    {
        // do your decryption here
        return inputStream;
    }

    /**
     * @param bytesToEncrypt byte[]
     * @return byte[]
     */
    private byte[] encrypt(final byte[] bytesToEncrypt)
    {
        // do your encryption here
        return bytesToEncrypt;
    }

    /**
     * @see org.springframework.http.converter.AbstractHttpMessageConverter#readInternal(java.lang.Class, org.springframework.http.HttpInputMessage)
     */
    @SuppressWarnings("resource")
    @Override
    protected Object readInternal(final Class<? extends Object> clazz, final HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException
    {
        return this.objectMapper.readValue(decrypt(inputMessage.getBody()), clazz);
    }

    /**
     * @see org.springframework.http.converter.AbstractHttpMessageConverter#supports(java.lang.Class)
     */
    @Override
    protected boolean supports(final Class<?> clazz)
    {
        return true;
    }

    /**
     * @see org.springframework.http.converter.AbstractHttpMessageConverter#writeInternal(java.lang.Object, org.springframework.http.HttpOutputMessage)
     */
    @SuppressWarnings("resource")
    @Override
    protected void writeInternal(final Object t, final HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException
    {
        outputMessage.getBody().write(encrypt(this.objectMapper.writeValueAsBytes(t)));
    }
}
