/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lamhdt.service;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.time.chrono.ThaiBuddhistEra;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lamhdt.model.Question;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author HL
 */
@Stateless
@Path("/question")
public class QuestionFacadeREST extends AbstractFacade<Question> {

    @PersistenceContext(unitName = "DB")
    private EntityManager em;

    public QuestionFacadeREST() {
        super(Question.class);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void createQuestion(String entity) {
        try {
            boolean check = false;
            HashMap result = new ObjectMapper().readValue(entity, HashMap.class);
            String subjectCode = (String) result.get("subjectCode");
            String question = (String) result.get("question");
            String description = (String) result.get("description");
            System.out.println(subjectCode);
            System.out.println(question);
            System.out.println(description);
            List answer = (List)result.get("answer");
            em = getEntityManager();
            em.getTransaction().begin();
            String question_jpql = "INSERT INTO Question(subjectCode, question, message) VALUES(?, ?, ?)";
            Query query = em.createNativeQuery(question_jpql);
            query.setParameter(1, subjectCode);
            query.setParameter(2, question);
            query.setParameter(3, description);
            check = query.executeUpdate() > 0;
            if(!check) em.getTransaction().rollback();
            em.getTransaction().commit();
            String id_question = "SELECT TOP 1 ID FROM Question ORDER BY ID DESC";
            int id = (int) em.createNativeQuery(id_question).getSingleResult();
            em.getTransaction().begin();
            for (Object object : answer) {
                HashMap k = (HashMap) object;
                String ans = (String) k.get("answer");
                boolean isRight = (boolean) k.get("isRight");
                String sql = "INSERT INTO Answer(Answer, IsRight, QuestionID) VALUES(?,?,?)";
                Query qr = em.createNativeQuery(sql);
                qr.setParameter(1, ans);
                qr.setParameter(2, isRight);
                qr.setParameter(3, id);
                check = qr.executeUpdate() > 0;
                if(!check) em.getTransaction().rollback();
            }
            em.getTransaction().commit();
        } catch (JsonProcessingException ex) {
            Logger.getLogger(QuestionFacadeREST.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void edit(@PathParam("id") Integer id, Question entity) {
        EntityManager em = null;
        entity.setId(id);
        Question exits = find(id);
        if (exits != null) {
            em = getEntityManager();
            em.getTransaction().begin();
            String jpql = "Update Question Set question = :question Where id = :id";
            Query query = em.createQuery(jpql);
            query.setParameter("question", entity.getQuestion());
            query.setParameter("id", id);
            query.executeUpdate();
            em.getTransaction().commit();
        }
//        super.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public Response remove(@PathParam("id") Integer id) {
        em = getEntityManager();
        em.getTransaction().begin();
        String jpql = "UPDATE Question SET status = :status WHERE id = :id";
        Query query = em.createQuery(jpql);
        query.setParameter("status", false);
        query.setParameter("id", id);
        query.executeUpdate();
        em.getTransaction().commit();
        return Response.noContent().build();
    }
    
    @PUT
    @Path("/active/{id}")
    public Response active(@PathParam("id") Integer id) {
        em = getEntityManager();
        em.getTransaction().begin();
        String jpql = "UPDATE Question SET status = :status WHERE id = :id";
        Query query = em.createQuery(jpql);
        query.setParameter("status", true);
        query.setParameter("id", id);
        query.executeUpdate();
        em.getTransaction().commit();
        return Response.noContent().build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Question find(@PathParam("id") Integer id) {
        return super.find(id);
    }

    @GET
    @Path("/search/{subject}/{searchValue}/{status}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Question> search(@PathParam("subject") String subject, @PathParam("searchValue") String searchValue, @PathParam("status") String status) {
        em = getEntityManager();
        List<Question> list = null;
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Question.class));
            Query q = em.createQuery(cq);
            list = q.getResultList();
        } finally {
            em.close();
        }
        List<Question> result = null;
        boolean sub = !subject.equals("none");
        boolean sea = !searchValue.equals("none");
        boolean sta = !status.equals("none");
        boolean statuss = true;
        System.out.println(subject);
        System.out.println(searchValue);
        System.out.println(status);
        if (sta) {
            statuss = Boolean.parseBoolean(status);
        }
            for (Question question : list) {
                boolean check = true;
                if (sub && !question.getSubjectCode().getSubjectCode().equals(subject)) {
                    check = false;
                }
                if (sea && !question.getQuestion().toLowerCase().contains(searchValue.toLowerCase())) {
                    check = false;
                }
                if (sta && question.getStatus() != statuss) {
                    check = false;
                }
                if (check) {
                    if (result == null) {
                        result = new ArrayList<>();
                    }
                    result.add(question);
                }
            }
        
        return result;
    }
    
    @GET
    @Path("/quiz/{subject}/{num}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Question> quizNow(@PathParam("subject") String subject, @PathParam("num") Integer num) {
        em = getEntityManager();
        List<Question> result = null;
        System.out.println(subject);
        System.out.println(num);
        em.getTransaction().begin();
        String jpql = "SELECT * FROM Question WHERE SubjectCode = ? AND Status = ? ORDER BY NEWID()";
        Query query = em.createNativeQuery(jpql, Question.class);
        query.setParameter(1, subject);
        query.setParameter(2, true);
        query.setMaxResults(num);
        result = query.getResultList();
        em.getTransaction().commit();
        for (Question question : result) {
            System.out.println(question.toString());
        }
        return result;
    }

    @GET
    @Override
    @Produces(MediaType.APPLICATION_JSON)
    public List<Question> findAll() {
        List<Question> list = super.findAll();
        List<Question> result = null;
        for (Question question : list) {
            if (question.getStatus()) {
                if (result == null) {
                    result = new ArrayList<>();
                }
                result.add(question);
            }
        }
        return result;
    }

    @GET
    @Path("{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Question> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        return super.findRange(new int[]{from, to});
    }

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String countREST() {
        return String.valueOf(super.count());
    }

}
