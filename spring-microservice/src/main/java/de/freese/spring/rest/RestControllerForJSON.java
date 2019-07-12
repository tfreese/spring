/**
 * Created: 28.04.2018
 */

package de.freese.spring.rest;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Annotation für einen {@link RestController} der nur {@link MediaType#APPLICATION_JSON_UTF8} produziert.
 *
 * @author Thomas Freese
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public @interface RestControllerForJSON
{
    /**
     * @return String[]
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "consumes")
    String[] consumes() default {};

    // /**
    // * @return String[]
    // */
    // @AliasFor(annotation = RequestMapping.class, attribute = "headers")
    // String[] headers() default {};

    // /**
    // * @return {@link RequestMethod}[]
    // */
    // @AliasFor(annotation = RequestMapping.class, attribute = "method")
    // RequestMethod[] method() default {};

    // /**
    // * @return String
    // */
    // @AliasFor(annotation = RequestMapping.class, attribute = "name")
    // String name() default "";

    // /**
    // * @return String[]
    // */
    // @AliasFor(annotation = RequestMapping.class, attribute = "params")
    // String[] params() default {};

    /**
     * @return String[]
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "path")
    String[] path() default {};

    // /**
    // * @return String[]
    // */
    // @AliasFor(annotation = RequestMapping.class, attribute = "produces")
    // String[] produces() default {};

    /**
     * @return String[]
     */
    @AliasFor(annotation = RequestMapping.class, attribute = "value")
    String[] value() default {};
}
