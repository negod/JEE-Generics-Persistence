package com.negod.generics.persistence;

import com.negod.generics.persistence.entity.GenericEntity;
import com.negod.generics.persistence.entity.GenericEntity_;
import com.negod.generics.persistence.exception.DaoException;
import com.negod.generics.persistence.search.GenericFilter;
import com.negod.generics.persistence.search.Pagination;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.jpa.criteria.OrderImpl;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 *
 * @author Joakim Johansson ( joakimjohansson@outlook.com )
 * @param <T> The entity to handle
 */
@Slf4j
@Data
public class GenericDao<T extends GenericEntity> {

    @PersistenceContext(unitName = "persistancePU")
    private EntityManager em;

    private final Class<T> entityClass;
    private final String className;
    private final Set<String> searchFields = new HashSet<>();

    /**
     * Constructor
     *
     * @param entityClass The entityclass the DAO will handle
     * @throws DaoException
     */
    public GenericDao(Class entityClass) throws DaoException {
        log.trace("Instantiating GenericDao for entity class {} ", entityClass.getSimpleName());
        if (entityClass == null) {
            log.error("Entity class cannot be null in constructor when instantiating GenericDao");
            throw new DaoException("Entity class cannot be null in constructor when instantiating GenericDao");
        } else {
            this.entityClass = entityClass;
            this.className = entityClass.getSimpleName();
            this.searchFields.addAll(extractSearchFields(entityClass));
        }
    }

    public final Set<String> extractSearchFields(Class<T> entityClass) {
        Set<String> fields = new HashSet<>();
        Field[] declaredFields = entityClass.getDeclaredFields();
        for (Field field : declaredFields) {
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                String annotationName = org.hibernate.search.annotations.Field.class.getName();
                if (annotation.annotationType().getName().equals(annotationName)) {
                    fields.add(field.getName());
                }
            }
        }
        return fields;
    }

    /**
     * Creates a criteria query from the entity manager
     *
     * @return Criteria builder created by Entity Manager
     * @throws DaoException
     */
    private Optional<CriteriaQuery<T>> getCriteriaQuery() throws DaoException {
        log.trace("Getting criteria query for {}", entityClass.getSimpleName());
        try {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            return Optional.ofNullable(criteriaBuilder.createQuery(entityClass));
        } catch (Exception e) {
            log.error("Error when getting Criteria Query in Generic Dao");
            throw new DaoException("Error when getting Criteria Query ", e);
        }
    }

    /**
     * Persist the Entity to DB
     *
     * @param entity The entity to persist
     * @return The persisted entity
     * @throws DaoException
     */
    public Optional<T> persist(T entity) throws DaoException {
        log.debug("Persisting entity of type {} with values {} ", entityClass.getSimpleName(), entity.toString());
        try {
            em.persist(entity);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            log.error("Error when persisting entity in Generic Dao");
            throw new DaoException("Error when persisting entity ", e);
        }
    }

    /**
     *
     * Updates the selected Entity
     *
     * @param entity The entity to update
     * @return The updated entity
     * @throws DaoException
     */
    public Optional<T> update(T entity) throws DaoException {
        try {
            Optional<T> entityToUpdate = getById(entity.getId());

            if (entityToUpdate.isPresent()) {
                log.debug("Updating entity of type {} with values {} ", entityClass.getSimpleName(), entity.toString());
                em.detach(entityToUpdate.get());
                entity.setInternalId(entityToUpdate.get().getInternalId());
                entity.setUpdatedDate(new Date());
            } else {
                return Optional.empty();
            }

            return Optional.ofNullable(em.merge(entity));
        } catch (Exception e) {
            log.error("Error when updating entity in Generic Dao");
            throw new DaoException("Error when updating entity ", e);
        }
    }

    /**
     * Deletes an entity with the provided External Id
     *
     * @param externalId
     * @return true or false depenent on the success of the deletion
     */
    public Boolean delete(String externalId) {
        try {
            Optional<T> entity = getById(externalId);
            if (entity.isPresent()) {
                return delete(entity.get());
            } else {
                log.error("No entity of type: {} found with id: {}", entityClass.getSimpleName(), externalId);
            }
        } catch (DaoException ex) {
            log.error("Error when deleting entity of type: {} with id: {}. ErrorMessage: {}", entityClass.getSimpleName(), externalId, ex.getMessage());
        }
        return Boolean.FALSE;

    }

    /**
     *
     * Deletes the selected Entity
     *
     * @param entity The entity to delete
     * @return true or false depenent on the success of the deletion
     * @throws DaoException
     */
    protected Boolean delete(T entity) throws DaoException {
        log.debug("Deleting entity of type {} with values {} ", entityClass.getSimpleName(), entity.toString());
        try {
            em.remove(entity);
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("Error when deleting entity in Generic Dao");
            throw new DaoException("Error when deleting entity ", e);
        }

    }

    /**
     * Get an entity by its external id
     *
     * @param id The external id (GUID) of the entity
     * @return The entity that matches the id
     * @throws DaoException
     */
    public Optional<T> getById(String id) throws DaoException {
        log.debug("Getting entity of type {} with id {} ", entityClass.getSimpleName(), id);
        try {

            Optional<CriteriaQuery<T>> data = this.getCriteriaQuery();

            if (data.isPresent()) {
                CriteriaQuery<T> cq = data.get();
                Root<T> entity = cq.from(entityClass);
                cq.where(entity.get(GenericEntity_.id).in(id));
                return get(cq);
            } else {
                return Optional.empty();
            }

        } catch (Exception e) {
            log.error("Error when getting entity by id: {} in Generic Dao", id);
            throw new DaoException("Error when getting entity by id ", e);
        }
    }

    /**
     *
     * Gets all entities that are persisted to the database
     *
     * @param filter The filter for the search
     * @return All persisted entities
     * @throws DaoException
     */
    public Optional<List<T>> getAll(GenericFilter filter) throws DaoException {
        log.debug("Getting all values of type {} and filter {} ", entityClass.getSimpleName(), filter.toString());
        try {

            FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
            QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(entityClass).get();

            String[] keys = filter.getSearchFields().toArray(new String[filter.getSearchFields().size()]);
            Optional<String> searchWord = Optional.ofNullable(filter.getGlobalSearchWord());
            Optional<Pagination> pagination = Optional.ofNullable(filter.getPagination());

            if (!ArrayUtils.isEmpty(keys) && searchWord.isPresent()) {
                log.trace("Executing Lucene wildcard search, KEYS: {} VALUE: {}", keys, searchWord.get().toLowerCase());
                org.apache.lucene.search.Query query = qb
                        .keyword()
                        .wildcard()
                        .onFields(keys)
                        .matching(searchWord.get().toLowerCase() + "*")
                        .createQuery();

                Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, entityClass);

                persistenceQuery.setMaxResults(filter.getPagination().getListSize());
                persistenceQuery.setFirstResult(filter.getPagination().getListSize() * filter.getPagination().getPage());

                return Optional.ofNullable(persistenceQuery.getResultList());
            } else if (pagination.isPresent()) {
                return getAll(filter.getPagination());
            } else {
                log.error("No pagination, search fields or search word present, aborting search");
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error when getting filtered list in Generic Dao");
            throw new DaoException("Error when getting filtered list in Generic Dao", e);
        }
    }

    /**
     *
     * Gets all entities that are persisted to the database
     *
     * @param pagination the pagination for the query
     * @return All persisted entities
     * @throws DaoException
     */
    public Optional<List<T>> getAll(Pagination pagination) throws DaoException {
        log.debug("Getting all values of type {} with pagination {} ", entityClass.getSimpleName(), pagination);
        try {
            Optional<CriteriaQuery<T>> data = this.getCriteriaQuery();
            if (data.isPresent()) {

                CriteriaQuery<T> cq = data.get();
                Root<T> rootEntity = cq.from(entityClass);
                Order order = new OrderImpl(rootEntity.get(GenericEntity_.updatedDate), true);
                cq.orderBy(order);
                CriteriaQuery<T> allQuery = cq.select(rootEntity);

                if (Optional.ofNullable(pagination.getListSize()).isPresent()
                        && Optional.ofNullable(pagination.getPage()).isPresent()) {
                    return executeTypedQueryList(em.createQuery(allQuery), pagination);
                } else {
                    return executeTypedQueryList(em.createQuery(allQuery));
                }
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error when getting all in Generic Dao");
            throw new DaoException("Error when getting all in Generic Dao", e);
        }
    }

    public Optional<List<T>> getAll() throws DaoException {
        return getAll(new Pagination());
    }

    /**
     *
     * Gets an entity based on a query
     *
     * @param query The query to execute
     * @return The queried entity
     * @throws DaoException
     */
    protected Optional<T> get(CriteriaQuery<T> query) throws DaoException {
        log.trace("Getting object of type {}", entityClass.getSimpleName());
        try {
            TypedQuery<T> typedQuery = em.createQuery(query);
            return executeTypedQuery(typedQuery);
        } catch (Exception e) {
            log.error("Error when gettting entity" + query.getResultType());
            throw new DaoException("Error when gettting entity " + query.getResultType(), e);
        }
    }

    /**
     *
     * Executes a Typed Query
     *
     * @param query The query to execute
     * @return The queried entity
     * @throws DaoException
     */
    protected Optional<List<T>> executeTypedQueryList(TypedQuery<T> query) throws DaoException {
        log.trace("Executing TypedQuery ( List ) for type {} with query: [ {} ]", entityClass.getSimpleName(), query.unwrap(org.hibernate.Query.class).getQueryString());
        try {
            List<T> resultList = query.getResultList();
            return Optional.ofNullable(resultList);
        } catch (Exception e) {
            log.error("Error when executing TypedQuery [ Get list ] for type {} ", entityClass.getSimpleName());
            throw new DaoException("Error when executing TypedQuery [ Get list ] for type " + entityClass.getSimpleName(), e);
        }
    }

    /**
     *
     * Executes a Typed Query
     *
     * @param query The query to execute
     * @param pagination
     * @return The queried entity
     * @throws DaoException
     */
    protected Optional<List<T>> executeTypedQueryList(TypedQuery<T> query, Pagination pagination) throws DaoException {
        log.trace("Executing TypedQuery ( Filtered List ) for type {} with query: [ {} ]", entityClass.getSimpleName(), query.unwrap(org.hibernate.Query.class).getQueryString());
        try {

            if (Optional.ofNullable(pagination).isPresent()) {

                Optional<Integer> listSize = Optional.ofNullable(pagination.getListSize());
                Optional<Integer> page = Optional.ofNullable(pagination.getPage());

                if (listSize.isPresent() && page.isPresent()) {
                    query.setMaxResults(pagination.getListSize());
                    query.setFirstResult(pagination.getListSize() * pagination.getPage());
                } else {
                    log.error("Pagination present but listsize or page missing {} returning empty list", pagination);
                    return Optional.empty();
                }

            }

            List<T> resultList = query.getResultList();
            return Optional.ofNullable(resultList);
        } catch (Exception e) {
            log.error("Error when executing TypedQuery [ Get filtered list ] for type {} ", entityClass.getSimpleName());
            throw new DaoException("Error when executing TypedQuery [ Get filtered list ] for type " + entityClass.getSimpleName(), e);
        }
    }

    /**
     *
     * Executes a Typed Query
     *
     * @param query The query to execute
     * @return The queried entity list
     * @throws DaoException
     */
    protected Optional<T> executeTypedQuery(TypedQuery<T> query) throws DaoException {
        log.trace("Executing TypedQuery ( Single Entity ) query for type {} with query: [ {} ]", entityClass.getSimpleName(), query.unwrap(org.hibernate.Query.class).getQueryString());
        try {
            T result = query.getSingleResult();
            return Optional.ofNullable(result);
        } catch (Exception e) {
            log.error("Error when executing TypedQuery [ Get single entity ] for type {} ", entityClass.getSimpleName());
            throw new DaoException("Error when executing TypedQuery [ Get single entity ] for type " + entityClass.getSimpleName(), e);
        }
    }

    /**
     *
     * @return
     */
    public Boolean indexEntity() {
        try {
            FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
            fullTextEntityManager.createIndexer().startAndWait();
            log.debug("SUCCESS! Done indexing {}", this.className);
        } catch (InterruptedException ex) {
            log.error("Failure when indexing " + this.className, ex);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

}
