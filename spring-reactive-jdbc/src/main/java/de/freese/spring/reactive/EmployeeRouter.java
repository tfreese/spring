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
public class EmployeeRouter {
    /**
     * Die REST-Methode 'createNewEmployee' wird im {@link EmployeeRestController} behandelt.
     */
    @Bean
    public RouterFunction<ServerResponse> route(final EmployeeHandler handler) {
        return RouterFunctions
                .route(RequestPredicates.GET("/employee/ln/{ln}/fn/{fn}")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::getEmployee)
                .andRoute(RequestPredicates.GET("/departments")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::getAllDepartments)
                .andRoute(RequestPredicates.GET("/employees")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::getAllEmployees)
                .andRoute(RequestPredicates.DELETE("/employee/id/{id}")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::deleteEmployee)

                // Diese Route wird über den EmployeeRestController behandelt.
                // .andRoute(RequestPredicates.PUT("/employee")
                //      .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::createNewEmployee)
                ;
    }
}
