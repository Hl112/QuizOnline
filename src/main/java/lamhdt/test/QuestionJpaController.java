/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lamhdt.test;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import lamhdt.model.Question;
import lamhdt.model.Subject;
import lamhdt.test.exceptions.NonexistentEntityException;
import lamhdt.test.exceptions.PreexistingEntityException;

/**
 *
 * @author HL
 */
public class QuestionJpaController implements Serializable {

    public QuestionJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Question question) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Subject subjectCode = question.getSubjectCode();
            if (subjectCode != null) {
                subjectCode = em.getReference(subjectCode.getClass(), subjectCode.getSubjectCode());
                question.setSubjectCode(subjectCode);
            }
            em.persist(question);
            if (subjectCode != null) {
                subjectCode.getQuestionCollection().add(question);
                subjectCode = em.merge(subjectCode);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findQuestion(question.getId()) != null) {
                throw new PreexistingEntityException("Question " + question + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Question question) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Question persistentQuestion = em.find(Question.class, question.getId());
            Subject subjectCodeOld = persistentQuestion.getSubjectCode();
            Subject subjectCodeNew = question.getSubjectCode();
            if (subjectCodeNew != null) {
                subjectCodeNew = em.getReference(subjectCodeNew.getClass(), subjectCodeNew.getSubjectCode());
                question.setSubjectCode(subjectCodeNew);
            }
            question = em.merge(question);
            if (subjectCodeOld != null && !subjectCodeOld.equals(subjectCodeNew)) {
                subjectCodeOld.getQuestionCollection().remove(question);
                subjectCodeOld = em.merge(subjectCodeOld);
            }
            if (subjectCodeNew != null && !subjectCodeNew.equals(subjectCodeOld)) {
                subjectCodeNew.getQuestionCollection().add(question);
                subjectCodeNew = em.merge(subjectCodeNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = question.getId();
                if (findQuestion(id) == null) {
                    throw new NonexistentEntityException("The question with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Question question;
            try {
                question = em.getReference(Question.class, id);
                question.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The question with id " + id + " no longer exists.", enfe);
            }
            Subject subjectCode = question.getSubjectCode();
            if (subjectCode != null) {
                subjectCode.getQuestionCollection().remove(question);
                subjectCode = em.merge(subjectCode);
            }
            em.remove(question);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Question> findQuestionEntities() {
        return findQuestionEntities(true, -1, -1);
    }

    public List<Question> findQuestionEntities(int maxResults, int firstResult) {
        return findQuestionEntities(false, maxResults, firstResult);
    }

    private List<Question> findQuestionEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Question.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Question findQuestion(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Question.class, id);
        } finally {
            em.close();
        }
    }

    public int getQuestionCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Question> rt = cq.from(Question.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
