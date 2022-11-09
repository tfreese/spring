// Created: 04.05.2016
package de.freese.spring.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import de.freese.spring.hateoas.exception.GreetingException;
import de.freese.spring.hateoas.model.GreetingPOJO;
import de.freese.spring.hateoas.model.GreetingRepresentationModel;
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

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping(path = "/greeter", produces =
        {
                MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE
        })
public class GreetingController
{
    private static final String TEMPLATE = "Hello, %s!";

    @GetMapping
    public HttpEntity<GreetingRepresentationModel> greeting(@RequestParam(value = "name", required = false, defaultValue = "World") final String name)
    {
        String message = String.format(TEMPLATE, name);

        GreetingRepresentationModel greetingRepresentationModel = new GreetingRepresentationModel(message);
        greetingRepresentationModel.add(linkTo(methodOn(GreetingController.class).greeting(name)).withSelfRel());
        greetingRepresentationModel.add(linkTo(methodOn(GreetingController.class).greetingPath(name)).withRel("forPath"));
        greetingRepresentationModel.add(linkTo(methodOn(GreetingController.class).greetingPojo(name)).withRel("forPojo"));
        greetingRepresentationModel.add(linkTo(methodOn(GreetingController.class).greetingSimple(name)).withRel("forSimple"));
        // greetingRepresentationModel.add(Link.of(LocalDateTime.now().toString(), IanaLinkRelations.LAST));
        // greetingRepresentationModel.add(Link.of(new Date().toString(), Date.class.getName()));

        return ResponseEntity.ok(greetingRepresentationModel);
    }

    @GetMapping("/fail")
    public HttpEntity<EntityModel<GreetingPOJO>> greetingFail()
    {
        throw new GreetingException("failed greet");
    }

    @GetMapping("/path/{name}")
    public HttpEntity<EntityModel<GreetingPOJO>> greetingPath(@PathVariable(value = "name") final String name)
    {
        String message = String.format(TEMPLATE, name);

        GreetingPOJO pojo = new GreetingPOJO(message);

        EntityModel<GreetingPOJO> resource = EntityModel.of(pojo);
        resource.add(linkTo(methodOn(GreetingController.class).greetingPath(name)).withSelfRel());
        resource.add(linkTo(methodOn(GreetingController.class).greetingPojo(name)).withRel("forPojo"));
        resource.add(linkTo(methodOn(GreetingController.class).greetingSimple(name)).withRel("forSimple"));

        return ResponseEntity.ok(resource);
    }

    @GetMapping("/pojo")
    public HttpEntity<EntityModel<GreetingPOJO>> greetingPojo(@RequestParam(value = "name", required = false, defaultValue = "World") final String name)
    {
        String message = String.format(TEMPLATE, name);

        GreetingPOJO pojo = new GreetingPOJO(message);

        EntityModel<GreetingPOJO> resource = EntityModel.of(pojo);
        // GreetingResource resource = new GreetingResource(pojo);
        resource.add(linkTo(methodOn(GreetingController.class).greetingPojo(name)).withSelfRel());
        resource.add(linkTo(methodOn(GreetingController.class).greetingPath(name)).withRel("forPath"));
        resource.add(linkTo(methodOn(GreetingController.class).greetingSimple(name)).withRel("forSimple"));

        return ResponseEntity.ok(resource);
    }

    @GetMapping("/simple")
    public GreetingPOJO greetingSimple(@RequestParam(value = "name", required = false, defaultValue = "World") final String name)
    {
        String message = String.format(TEMPLATE, name);

        return new GreetingPOJO(message);
    }
}
