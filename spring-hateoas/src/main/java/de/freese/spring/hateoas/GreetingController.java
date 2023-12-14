// Created: 04.05.2016
package de.freese.spring.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.freese.spring.hateoas.exception.GreetingException;
import de.freese.spring.hateoas.model.GreetingPojo;
import de.freese.spring.hateoas.model.GreetingRepresentationModel;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping(path = "/greeter", produces = {MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE})
public class GreetingController {
    private static final String TEMPLATE = "Hello, %s!";

    @GetMapping
    public HttpEntity<GreetingRepresentationModel> greeting(@RequestParam(value = "name", required = false, defaultValue = "World") final String name) {
        final String message = String.format(TEMPLATE, name);

        final GreetingRepresentationModel greetingRepresentationModel = new GreetingRepresentationModel(message);
        greetingRepresentationModel.add(linkTo(methodOn(GreetingController.class).greeting(name)).withSelfRel());
        greetingRepresentationModel.add(linkTo(methodOn(GreetingController.class).greetingPath(name)).withRel("forPath"));
        greetingRepresentationModel.add(linkTo(methodOn(GreetingController.class).greetingPojo(name)).withRel("forPojo"));
        greetingRepresentationModel.add(linkTo(methodOn(GreetingController.class).greetingSimple(name)).withRel("forSimple"));
        // greetingRepresentationModel.add(Link.of(LocalDateTime.now().toString(), IanaLinkRelations.LAST));
        // greetingRepresentationModel.add(Link.of(new Date().toString(), Date.class.getName()));

        return ResponseEntity.ok(greetingRepresentationModel);
    }

    @GetMapping("/fail")
    public HttpEntity<EntityModel<GreetingPojo>> greetingFail() {
        throw new GreetingException("failed greet");
    }

    @GetMapping("/path/{name}")
    public HttpEntity<EntityModel<GreetingPojo>> greetingPath(@PathVariable(value = "name") final String name) {
        final String message = String.format(TEMPLATE, name);

        final GreetingPojo pojo = new GreetingPojo(message);

        final EntityModel<GreetingPojo> resource = EntityModel.of(pojo);
        resource.add(linkTo(methodOn(GreetingController.class).greetingPath(name)).withSelfRel());
        resource.add(linkTo(methodOn(GreetingController.class).greetingPojo(name)).withRel("forPojo"));
        resource.add(linkTo(methodOn(GreetingController.class).greetingSimple(name)).withRel("forSimple"));

        return ResponseEntity.ok(resource);
    }

    @GetMapping("/pojo")
    public HttpEntity<EntityModel<GreetingPojo>> greetingPojo(@RequestParam(value = "name", required = false, defaultValue = "World") final String name) {
        final String message = String.format(TEMPLATE, name);

        final GreetingPojo pojo = new GreetingPojo(message);

        final EntityModel<GreetingPojo> resource = EntityModel.of(pojo);
        // final GreetingResource resource = new GreetingResource(pojo);
        resource.add(linkTo(methodOn(GreetingController.class).greetingPojo(name)).withSelfRel());
        resource.add(linkTo(methodOn(GreetingController.class).greetingPath(name)).withRel("forPath"));
        resource.add(linkTo(methodOn(GreetingController.class).greetingSimple(name)).withRel("forSimple"));

        return ResponseEntity.ok(resource);
    }

    @GetMapping("/simple")
    public GreetingPojo greetingSimple(@RequestParam(value = "name", required = false, defaultValue = "World") final String name) {
        final String message = String.format(TEMPLATE, name);

        return new GreetingPojo(message);
    }
}
