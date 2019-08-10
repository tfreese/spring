// Erzeugt: 04.05.2016
package de.freese.spring.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import java.time.LocalDateTime;
import java.util.Date;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
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
import de.freese.spring.hateoas.model.GreetingPOJO;
import de.freese.spring.hateoas.model.GreetingRepresentationModel;

/**
 * https://spring.io/guides/tutorials/bookmarks/
 *
 * @author Thomas Freese
 */
@RestController
// produces -> Unterstützung von POJOs
// produces = {MediaType.APPLICATION_JSON_VALUE, "application/hal+json"}
@RequestMapping(path = "/greeter", produces =
{
        MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE
})
public class GreetingController
{
    /**
     *
     */
    private static final String TEMPLATE = "Hello, %s!";

    /**
     * Erzeugt eine neue Instanz von {@link GreetingController}
     */
    public GreetingController()
    {
        super();
    }

    /**
     * Ergebnis: {"_links":{"self":{"href":"http://localhost:9000/greeter/?name=World"}},"greeting":"Hello, World!"}
     *
     * @param name String
     * @return {@link ResponseEntity}
     */
    @GetMapping
    // @RequestMapping("")
    // @JsonRequestMapping("")
    public HttpEntity<GreetingRepresentationModel> greeting(@RequestParam(value = "name", required = false, defaultValue = "World") final String name)
    {
        String message = String.format(TEMPLATE, name);

        GreetingRepresentationModel greetingRepresentationModel = new GreetingRepresentationModel(message);
        greetingRepresentationModel.add(linkTo(methodOn(GreetingController.class).greeting(name)).withSelfRel());
        greetingRepresentationModel.add(linkTo(methodOn(GreetingController.class).greetingPATH(name)).withRel("forPath"));
        greetingRepresentationModel.add(linkTo(methodOn(GreetingController.class).greetingPOJO(name)).withRel("forPojo"));
        greetingRepresentationModel.add(linkTo(methodOn(GreetingController.class).greetingSimple(name)).withRel("forSimple"));
        greetingRepresentationModel.add(new Link(LocalDateTime.now().toString(), IanaLinkRelations.LAST));
        greetingRepresentationModel.add(new Link(new Date().toString(), Date.class.getName()));

        // return new ResponseEntity<>(greetingResource, HttpStatus.OK);
        return ResponseEntity.ok(greetingRepresentationModel);
    }

    /**
     * @return {@link ResponseEntity}
     */
    @GetMapping("/fail")
    public HttpEntity<EntityModel<GreetingPOJO>> greetingFail()
    {
        throw new GreetingException("failed greet");
    }

    /**
     * Ergebnis: {"greeting":"Hello, test!","_links":{"self":{"href":"http://localhost:9000/greeter/path/test"}}}
     *
     * @param name String
     * @return {@link ResponseEntity}
     */
    @GetMapping("/path/{name}")
    public HttpEntity<EntityModel<GreetingPOJO>> greetingPATH(@PathVariable(value = "name") final String name)
    {
        String message = String.format(TEMPLATE, name);

        GreetingPOJO pojo = new GreetingPOJO(message);

        EntityModel<GreetingPOJO> resource = new EntityModel<>(pojo);
        resource.add(linkTo(methodOn(GreetingController.class).greetingPATH(name)).withSelfRel());
        resource.add(linkTo(methodOn(GreetingController.class).greetingPOJO(name)).withRel("forPojo"));
        resource.add(linkTo(methodOn(GreetingController.class).greetingSimple(name)).withRel("forSimple"));
        resource.add(new Link(LocalDateTime.now().toString(), IanaLinkRelations.LAST));

        return ResponseEntity.ok(resource);
    }

    /**
     * Ergebnis: {"greeting":"Hello, World!","_links":{"self":{"href":"http://localhost:9000/greeter/pojo?name=World"}}}
     *
     * @param name String
     * @return {@link ResponseEntity}
     */
    @GetMapping("/pojo")
    public HttpEntity<EntityModel<GreetingPOJO>> greetingPOJO(@RequestParam(value = "name", required = false, defaultValue = "World") final String name)
    {
        String message = String.format(TEMPLATE, name);

        GreetingPOJO pojo = new GreetingPOJO(message);

        EntityModel<GreetingPOJO> resource = new EntityModel<>(pojo);
        // GreetingResource resource = new GreetingResource(pojo);
        resource.add(linkTo(methodOn(GreetingController.class).greetingPOJO(name)).withSelfRel());
        resource.add(linkTo(methodOn(GreetingController.class).greetingPOJO(name)).withRel("forPojo"));
        resource.add(linkTo(methodOn(GreetingController.class).greetingSimple(name)).withRel("forSimple"));
        resource.add(new Link(LocalDateTime.now().toString(), IanaLinkRelations.LAST));

        return ResponseEntity.ok(resource);
    }

    /**
     * Ergebnis: {"greeting":"Hello, World!"}
     *
     * @param name String
     * @return {@link ResponseEntity}
     */
    @GetMapping("/simple")
    public GreetingPOJO greetingSimple(@RequestParam(value = "name", required = false, defaultValue = "World") final String name)
    {
        String message = String.format(TEMPLATE, name);

        GreetingPOJO pojo = new GreetingPOJO(message);

        return pojo;
    }
}
