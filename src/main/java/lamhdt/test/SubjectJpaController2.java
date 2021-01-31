/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lamhdt.test;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import lamhdt.model.Question;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import lamhdt.model.Result;
import lamhdt.model.Subject;
import lamhdt.test.exceptions.NonexistentEntityException;
import lamhdt.test.exceptions.PreexistingEntityException;

/**
 *
 * @author HL
 */
public class SubjectJpaController2 implements Serializable {

    public SubjectJpaController2(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Subject subject) throws PreexistingEntityException, Exception {
        if (subject.getQuestionCollection() == null) {
            subject.setQuestionCollection(new ArrayList<Question>());
        }
        if (subject.getResultCollection() == null) {
            subject.setResultCollection(new ArrayList<Result>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Collection<Question> attachedQuestionCollection = new ArrayList<Question>();
            for (Question questionCollectionQuestionToAttach : subject.getQuestionCollection()) {
                questionCollectionQuestionToAttach = em.getReference(questionCollectionQuestionToAttach.getClass(), questionCollectionQuestionToAttach.getId());
                attachedQuestionCollection.add(questionCollectionQuestionToAttach);
            }
            subject.setQuestionCollection(attachedQuestionCollection);
            Collection<Result> attachedResultCollection = new ArrayList<Result>();
            for (Result resultCollectionResultToAttach : subject.getResultCollection()) {
                resultCollectionResultToAttach = em.getReference(resultCollectionResultToAttach.getClass(), resultCollectionResultToAttach.getId());
                attachedResultCollection.add(resultCollectionResultToAttach);
            }
            subject.setResultCollection(attachedResultCollection);
            em.persist(subject);
            for (Question questionCollectionQuestion : subject.getQuestionCollection()) {
                Subject oldSubjectCodeOfQuestionCollectionQuestion = questionCollectionQuestion.getSubjectCode();
                questionCollectionQuestion.setSubjectCode(subject);
                questionCollectionQuestion = em.merge(questionCollectionQuestion);
                if (oldSubjectCodeOfQuestionCollectionQuestion != null) {
                    oldSubjectCodeOfQuestionCollectionQuestion.getQuestionCollection().remove(questionCollectionQuestion);
                    oldSubjectCodeOfQuestionCollectionQuestion = em.merge(oldSubjectCodeOfQuestionCollectionQuestion);
                }
            }
            for (Result resultCollectionResult : subject.getResultCollection()) {
                Subject oldSubjectCodeOfResultCollectionResult = resultCollectionResult.getSubjectCode();
                resultCollectionResult.setSubjectCode(subject);
                resultCollectionResult = em.merge(resultCollectionResult);
                if (oldSubjectCodeOfResultCollectionResult != null) {
                    oldSubjectCodeOfResultCollectionResult.getResultCollection().remove(resultCollectionResult);
                    oldSubjectCodeOfResultCollectionResult = em.merge(oldSubjectCodeOfResultCollectionResult);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findSubject(subject.getSubjectCode()) != null) {
                throw new PreexistingEntityException("Subject " + subject + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Subject subject) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Subject persistentSubject = em.find(Subject.class, subject.getSubjectCode());
            Collection<Question> questionCollectionOld = persistentSubject.getQuestionCollection();
            Collection<Question> questionCollectionNew = subject.getQuestionCollection();
            Collection<Result> resultCollectionOld = persistentSubject.getResultCollection();
            Collection<Result> resultCollectionNew = subject.getResultCollection();
            Collection<Question> attachedQuestionCollectionNew = new ArrayList<Question>();
            for (Question questionCollectionNewQuestionToAttach : questionCollectionNew) {
                questionCollectionNewQuestionToAttach = em.getReference(questionCollectionNewQuestionToAttach.getClass(), questionCollectionNewQuestionToAttach.getId());
                attachedQuestionCollectionNew.add(questionCollectionNewQuestionToAttach);
            }
            questionCollectionNew = attachedQuestionCollectionNew;
            subject.setQuestionCollection(questionCollectionNew);
            Collection<Result> attachedResultCollectionNew = new ArrayList<Result>();
            for (Result resultCollectionNewResultToAttach : resultCollectionNew) {
                resultCollectionNewResultToAttach = em.getReference(resultCollectionNewResultToAttach.getClass(), resultCollectionNewResultToAttach.getId());
                attachedResultCollectionNew.add(resultCollectionNewResultToAttach);
            }
            resultCollectionNew = attachedResultCollectionNew;
            subject.setResultCollection(resultCollectionNew);
            subject = em.merge(subject);
            for (Question questionCollectionOldQuestion : questionCollectionOld) {
                if (!questionCollectionNew.contains(questionCollectionOldQuestion)) {
                    questionCollectionOldQuestion.setSubjectCode(null);
                    questionCollectionOldQuestion = em.merge(questionCollectionOldQuestion);
                }
            }
            for (Question questionCollectionNewQuestion : questionCollectionNew) {
                if (!questionCollectionOld.contains(questionCollectionNewQuestion)) {
                    Subject oldSubjectCodeOfQuestionCollectionNewQuestion = questionCollectionNewQuestion.getSubjectCode();
                    questionCollectionNewQuestion.setSubjectCode(subject);
                    questionCollectionNewQuestion = em.merge(questionCollectionNewQuestion);
                    if (oldSubjectCodeOfQuestionCollectionNewQuestion != null && !oldSubjectCodeOfQuestionCollectionNewQuestion.equals(subject)) {
                        oldSubjectCodeOfQuestionCollectionNewQuestion.getQuestionCollection().remove(questionCollectionNewQuestion);
                        oldSubjectCodeOfQuestionCollectionNewQuestion = em.merge(oldSubjectCodeOfQuestionCollectionNewQuestion);
                    }
                }
            }
            for (Result resultCollectionOldResult : resultCollectionOld) {
                if (!resultCollectionNew.contains(resultCollectionOldResult)) {
                    resultCollectionOldResult.setSubjectCode(null);
                    resultCollectionOldResult = em.merge(resultCollectionOldResult);
                }
            }
            for (Result resultCollectionNewResult : resultCollectionNew) {
                if (!resultCollectionOld.contains(resultCollectionNewResult)) {
                    Subject oldSubjectCodeOfResultCollectionNewResult = resultCollectionNewResult.getSubjectCode();
                    resultCollectionNewResult.setSubjectCode(subject);
                    resultCollectionNewResult = em.merge(resultCollectionNewResult);
                    if (oldSubjectCodeOfResultCollectionNewResult != null && !oldSubjectCodeOfResultCollectionNewResult.equals(subject)) {
                        oldSubjectCodeOfResultCollectionNewResult.getResultCollection().remove(resultCollectionNewResult);
                        oldSubjectCodeOfResultCollectionNewResult = em.merge(oldSubjectCodeOfResultCollectionNewResult);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = subject.getSubjectCode();
                if (findSubject(id) == null) {
                    throw new NonexistentEntityException("The subject with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(String id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Subject subject;
            try {
                subject = em.getReference(Subject.class, id);
                subject.getSubjectCode();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The subject with id " + id + " no longer exists.", enfe);
            }
            Collection<Question> questionCollection = subject.getQuestionCollection();
            for (Question questionCollectionQuestion : questionCollection) {
                questionCollectionQuestion.setSubjectCode(null);
                questionCollectionQuestion = em.merge(questionCollectionQuestion);
            }
            Collection<Result> resultCollection = subject.getResultCollection();
            for (Result resultCollectionResult : resultCollection) {
                resultCollectionResult.setSubjectCode(null);
                resultCollectionResult = em.merge(resultCollectionResult);
            }
            em.remove(subject);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Subject> findSubjectEntities() {
        return findSubjectEntities(true, -1, -1);
    }

    public List<Subject> findSubjectEntities(int maxResults, int firstResult) {
        return findSubjectEntities(false, maxResults, firstResult);
    }

    private List<Subject> findSubjectEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Subject.class));
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

    public Subject findSubject(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Subject.class, id);
        } finally {
            em.close();
        }
    }

    public int getSubjectCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Subject> rt = cq.from(Subject.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
