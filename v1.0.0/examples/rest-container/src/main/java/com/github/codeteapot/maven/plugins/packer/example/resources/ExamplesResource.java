package com.github.codeteapot.maven.plugin.packer.example.resources;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.status;
import static jakarta.ws.rs.core.Response.Status.OK;
import static java.util.Collections.singletonMap;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

// START SNIPPET: exampleResourceClass
@Path("/examples")
@Produces(APPLICATION_JSON)
public class ExamplesResource {

  @GET
  @Path("/{id}")
  public Response get(@PathParam("id") long id) {
    return status(OK)
        .entity(singletonMap("id", id))
        .build();
  }
}
// END SNIPPET: exampleResourceClass
