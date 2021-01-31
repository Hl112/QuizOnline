/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lamhdt.service;

import java.util.ArrayList;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lamhdt.model.Answer;
import lamhdt.model.Question;

/**
 *
 * @author HL
 */
@Stateless
@Path("/answer")
public class AnswerFacadeREST extends AbstractFacade<Answer> {

    @PersistenceContext(unitName = "DB")
    private EntityManager em;

    public AnswerFacadeREST() {
        super(Answer.class);
    }

    @POST
    @Override
    @Consumes(MediaType.APPLICATION_JSON)
    public void create(Answer entity) {
        super.create(entity);
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response edit(@PathParam("id") Integer id, Answer entity) {
        EntityManager em = null;
        entity.setId(id);
        boolean check = false;
        if (entity.getIsRight() == true) {
            check = true;
        }
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Answer persistentAnswer = find(id);
            entity.setQuestionID(persistentAnswer.getQuestionID());
            if (check) {
                editCorrect(persistentAnswer.getQuestionID());
            }
            if(entity.getAnswer() == null){
                entity.setAnswer(persistentAnswer.getAnswer());
            }
            entity = em.merge(entity);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer ids = entity.getId();
                if (find(ids) == null) {
                    throw new WebApplicationException("The answer with id " + ids + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return Response.noContent().build();
    }

    private boolean editCorrect(Question id) {
        boolean check = false;
        EntityManager em = getEntityManager();
        String jpql = "UPDATE Answer SET isRight = :isRight WHERE questionID = :questionID";
        em.getTransaction().begin();
        Query query = em.createQuery(jpql,Answer.class);
        
        query.setParameter("isRight", false);
        query.setParameter("questionID", id);
        check = query.executeUpdate() > 0;
        em.getTransaction().commit();
        return true;
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Integer id) {
        super.remove(super.find(id));
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Answer find(@PathParam("id") Integer id) {
        return super.find(id);
    }

    @GET
    @Override
    @Produces(MediaType.APPLICATION_JSON)
    public List<Answer> findAll() {
        return super.findAll();
    }

    @GET
    @Path("{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Answer> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("/QID/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Answer> findByQuestionId(@PathParam("id") Integer id) {
        List<Answer> list = super.findAll();
        List<Answer> result = null;
        for (Answer answer : list) {
            if (answer.getQuestionID().getId() == id) {
                if (result == null) {
                    result = new ArrayList<>();
                }
                result.add(answer);
            }
        }
        return result;
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        return String.valueOf(super.count());
    }

}
