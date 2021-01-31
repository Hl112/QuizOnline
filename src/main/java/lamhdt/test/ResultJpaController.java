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
import lamhdt.model.Result;
import lamhdt.model.Subject;
import lamhdt.test.exceptions.NonexistentEntityException;
import lamhdt.test.exceptions.PreexistingEntityException;

/**
 *
 * @author HL
 */
public class ResultJpaController implements Serializable {

    public ResultJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Result result) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Subject subjectCode = result.getSubjectCode();
            if (subjectCode != null) {
                subjectCode = em.getReference(subjectCode.getClass(), subjectCode.getSubjectCode());
                result.setSubjectCode(subjectCode);
            }
            em.persist(result);
            if (subjectCode != null) {
                subjectCode.getResultCollection().add(result);
                subjectCode = em.merge(subjectCode);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findResult(result.getId()) != null) {
                throw new PreexistingEntityException("Result " + result + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Result result) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Result persistentResult = em.find(Result.class, result.getId());
            Subject subjectCodeOld = persistentResult.getSubjectCode();
            Subject subjectCodeNew = result.getSubjectCode();
            if (subjectCodeNew != null) {
                subjectCodeNew = em.getReference(subjectCodeNew.getClass(), subjectCodeNew.getSubjectCode());
                result.setSubjectCode(subjectCodeNew);
            }
            result = em.merge(result);
            if (subjectCodeOld != null && !subjectCodeOld.equals(subjectCodeNew)) {
                subjectCodeOld.getResultCollection().remove(result);
                subjectCodeOld = em.merge(subjectCodeOld);
            }
            if (subjectCodeNew != null && !subjectCodeNew.equals(subjectCodeOld)) {
                subjectCodeNew.getResultCollection().add(result);
                subjectCodeNew = em.merge(subjectCodeNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = result.getId();
                if (findResult(id) == null) {
                    throw new NonexistentEntityException("The result with id " + id + " no longer exists.");
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
            Result result;
            try {
                result = em.getReference(Result.class, id);
                result.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The result with id " + id + " no longer exists.", enfe);
            }
            Subject subjectCode = result.getSubjectCode();
            if (subjectCode != null) {
                subjectCode.getResultCollection().remove(result);
                subjectCode = em.merge(subjectCode);
            }
            em.remove(result);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Result> findResultEntities() {
        return findResultEntities(true, -1, -1);
    }

    public List<Result> findResultEntities(int maxResults, int firstResult) {
        return findResultEntities(false, maxResults, firstResult);
    }

    private List<Result> findResultEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Result.class));
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

    public Result findResult(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Result.class, id);
        } finally {
            em.close();
        }
    }

    public int getResultCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Result> rt = cq.from(Result.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
