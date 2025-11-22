// Created: 15.10.2019
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
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Thomas Freese
 */
public class CipherJsonHttpMessageConverter extends AbstractHttpMessageConverter<Object> {
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final JsonMapper jsonMapper;

    public CipherJsonHttpMessageConverter(final JsonMapper jsonMapper) {
        super(MediaType.APPLICATION_JSON, new MediaType("application", "*+json", DEFAULT_CHARSET));

        this.jsonMapper = jsonMapper;

    }

    @Override
    protected Object readInternal(final Class<? extends Object> clazz, final HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return jsonMapper.readValue(decrypt(inputMessage.getBody()), clazz);
    }

    @Override
    protected boolean supports(final Class<?> clazz) {
        return true;
    }

    @Override
    protected void writeInternal(final Object t, final HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        outputMessage.getBody().write(encrypt(jsonMapper.writeValueAsBytes(t)));
    }

    private InputStream decrypt(final InputStream inputStream) {
        // Do your decryption here.
        return inputStream;
    }

    private byte[] encrypt(final byte[] bytesToEncrypt) {
        // Do your encryption here.
        return bytesToEncrypt;
    }
}
