/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lamhdt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import lamhdt.model.Result;

/**
 *
 * @author HL
 */
@Stateless
@Path("/result")
public class ResultFacadeREST extends AbstractFacade<Result> {

    @PersistenceContext(unitName = "DB")
    private EntityManager em;

    public ResultFacadeREST() {
        super(Result.class);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void create(String entity) {
        try {
            HashMap result = new ObjectMapper().readValue(entity, HashMap.class);
            String username = (String) result.get("username");
            String subjectCode = (String) result.get("subjectCode");
            String score = (String) result.get("score");
            em = getEntityManager();
            em.getTransaction().begin();
            String jpql = "INSERT INTO Result(username, subjectCode, score) VALUES(?,?,?)";
            Query query = em.createNativeQuery(jpql);
            query.setParameter(1, username);
            query.setParameter(2, subjectCode);
            query.setParameter(3, score);
            query.executeUpdate();
            em.getTransaction().commit();
        } catch (JsonProcessingException ex) {
            Logger.getLogger(ResultFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void edit(@PathParam("id") Integer id, Result entity) {
        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Integer id) {
        super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Result find(@PathParam("id") Integer id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces(MediaType.APPLICATION_JSON)
    public List<Result> findAll() {
        return super.findAll();
    }

    @GET
    @Path("search/{subject}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Result> search(@PathParam("subject") String subject) {
        em = getEntityManager();
        em.getTransaction().begin();
        List<Result> result = null;
        String sql = "SELECT * FROM Result WHERE subjectCode = ?";
        Query query = em.createNativeQuery(sql, Result.class);
        query.setParameter(1, subject);
        result = query.getResultList();
        return result;
    }

    @GET
    @Path("{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Result> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        return String.valueOf(super.count());
    }

}
