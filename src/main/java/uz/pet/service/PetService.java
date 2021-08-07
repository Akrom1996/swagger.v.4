package uz.pet.service;

import com.google.gson.JsonObject;
import org.wso2.msf4j.Microservice;
import uz.pet.utils.ServiceFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/service")
public class PetService implements Microservice {
    @POST
    @Path("/json")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response myService(JsonObject jsonObject) throws Exception {

        ServiceFactory serviceFactory = new ServiceFactory();
        try {
            return Response.ok().entity(serviceFactory.successResponses(jsonObject)).build();
        }catch (Exception ex) {
            return Response.status(Response.Status.BAD_GATEWAY).entity(serviceFactory.failedResponses(400,ex.toString())).build();
        }
    }
}
