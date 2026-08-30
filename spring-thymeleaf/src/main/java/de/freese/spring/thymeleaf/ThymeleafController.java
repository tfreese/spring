package de.freese.spring.thymeleaf;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Controller;

import de.freese.spring.thymeleaf.exception.ThymeleafControllerExceptionHandler;

/**
 * Extra-Annotation für ThymeleafController zum Vereinfachen des ExceptionHandlings ({@link ThymeleafControllerExceptionHandler}).
 *
 * @author Thomas Freese
 * @since 05.09.2018
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Controller
public @interface ThymeleafController {

}
