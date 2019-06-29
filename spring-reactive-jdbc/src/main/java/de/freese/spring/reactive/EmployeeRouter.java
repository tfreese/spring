/**
 *
 */
package de.freese.spring.reactive;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * @author Thomas Freese
 */
@Configuration
public class EmployeeRouter
{
    /**
     * Erstellt ein neues {@link EmployeeRouter} Object.
     */
    public EmployeeRouter()
    {
        super();
    }

    /**
     * Die anderen REST-Methoden sind im {@link EmployeeRestController}.
     *
     * @param handler {@link EmployeeHandler}
     * @return {@link RouterFunction}
     */
    @Bean
    public RouterFunction<ServerResponse> route(final EmployeeHandler handler)
    {
        // @formatter:off
		return RouterFunctions
				.route(RequestPredicates.GET("/employee/fn/{fn}/ln/{ln}")
						.and(RequestPredicates.accept(MediaType.APPLICATION_JSON_UTF8)), handler::getEmployee)
                .andRoute(RequestPredicates.GET("/departments")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::getAllDepartments)
				.andRoute(RequestPredicates.GET("/employees")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::getAllEmployees)
//				.andRoute(RequestPredicates.PUT("/employee")
//						.and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::createNewEmployee)
				.andRoute(RequestPredicates.DELETE("/employee/id/{id}")
						.and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::deleteEmployee);
		// @formatter:on
    }
}
