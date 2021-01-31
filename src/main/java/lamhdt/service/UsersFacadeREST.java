/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lamhdt.service;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lamhdt.model.Users;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author HL
 */
@Stateless
@Path("/users")
public class UsersFacadeREST extends AbstractFacade<Users> {

    @PersistenceContext(unitName = "DB")
    private EntityManager em;

    public UsersFacadeREST() {
        super(Users.class);
    }

    @POST
    @Override
    @Consumes(MediaType.APPLICATION_JSON)
    public void create(Users entity) {
        System.out.println(entity.getFullname());
        System.out.println(entity.getUsername());
        String password_SHA = DigestUtils.sha256Hex(entity.getPassword());
        em = getEntityManager();
       em.getTransaction().begin();
       String jpql = "Insert Into Users(Username, Password, Fullname, Role, Status) VALUES(?,?,?,?,?)";
       Query query = em.createNativeQuery(jpql);
       query.setParameter(1, entity.getUsername());
       query.setParameter(2, password_SHA);
       query.setParameter(3, entity.getFullname());
       query.setParameter(4, "student");
       query.setParameter(5, "New");
       query.executeUpdate();
       em.getTransaction().commit();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void edit(@PathParam("id") String id, Users entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") String id) {
        super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Users find(@PathParam("id") String id) {
        return super.find(id);
    }

    @GET
    @Path("/checkLogin/{username}/{password}")
    @Produces(MediaType.APPLICATION_JSON)
    public Users checkLogin(@PathParam("username") String username, @PathParam("password") String password) {
        Users user = super.find(username);
        boolean check = false;
        if (user != null) {
            if (user.getUsername().equals(username)) {
                String pass_sha = DigestUtils.sha256Hex(password);
                if (user.getPassword().equals(pass_sha)) {
                    check = true;
                }
            }
        }
        if (check) {
            return user;
        } else {
            return null;
        }
    }

    @GET
    @Override
    @Produces(MediaType.APPLICATION_JSON)
    public List<Users> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Users> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        return String.valueOf(super.count());
    }

}
