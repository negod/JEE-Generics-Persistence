package com.negod.generics.persistence;

import com.negod.generics.persistence.entity.GenericEntity;
import com.negod.generics.persistence.entity.GenericEntity_;
import com.negod.generics.persistence.exception.DaoException;
import com.negod.generics.persistence.exception.NotFoundException;
import com.negod.generics.persistence.mapper.BaseMapper;
import com.negod.generics.persistence.mapper.Mapper;
import com.negod.generics.persistence.search.GenericFilter;
import com.negod.generics.persistence.search.Pagination;
import com.negod.generics.persistence.update.ObjectUpdate;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.jpa.criteria.OrderImpl;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 * @param <T> The entity to handle
 */
@Slf4j
@Data
public abstract class GenericDao<T extends GenericEntity> {
    
    private final Class<T> entityClass;
    private final String className;
    private final Set<String> searchFields = new HashSet<>();
    private BaseMapper mapper;
    
    public abstract EntityManager getEntityManager();

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
            throw new DaoException("Entity class cannot be null in constructor when instantiating GenericDao", null);
        } else {
            this.entityClass = entityClass;
            this.className = entityClass.getSimpleName();
            this.searchFields.addAll(extractSearchFields(entityClass, null));
            this.mapper = new BaseMapper(entityClass, entityClass);
        }
    }
    
    private final Set<String> extractSearchFields(Class<?> entityClass, Set<String> alreadyExtractedClasses) throws DaoException {

        // Used to avoid StackOverflow One class can only be extracted once
        if (alreadyExtractedClasses == null) {
            alreadyExtractedClasses = new HashSet<>(Arrays.asList(new String[]{entityClass.getName()}));
        }
        
        try {
            
            String fieldAnnotation = org.hibernate.search.annotations.Field.class.getName();
            String indexedEmbeddedAnnotation = org.hibernate.search.annotations.IndexedEmbedded.class.getName();
            
            Set<String> fields = new HashSet<>();
            Field[] declaredFields = entityClass.getDeclaredFields();
            for (Field field : declaredFields) {
                
                Annotation[] annotations = field.getAnnotations();
                for (Annotation annotation : annotations) {
                    
                    if (annotation.annotationType().getName().equals(fieldAnnotation)) {
                        fields.add(field.getName());
                    }
                    if (annotation.annotationType().getName().equals(indexedEmbeddedAnnotation)) {
                        
                        Class<?> clazz = field.getType();
                        
                        if (clazz.equals(Set.class) || clazz.equals(List.class)) {
                            ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
                            clazz = (Class<?>) stringListType.getActualTypeArguments()[0];
                        }

                        // To avoid StackOverFlow
                        if (alreadyExtractedClasses.contains(clazz.getName())) {
                            continue;
                        } else {
                            alreadyExtractedClasses.add(clazz.getName());
                        }
                        
                        Object entity = clazz.newInstance();
                        Set<String> extractSearchFields = extractSearchFields(entity.getClass(), alreadyExtractedClasses);
                        
                        for (String extractSearchField : extractSearchFields) {
                            fields.add(field.getName().concat(".").concat(extractSearchField));
                        }
                        
                    }
                    
                }
            }
            return fields;
            
        } catch (IllegalAccessException | InstantiationException | IllegalArgumentException ex) {
            log.error("Error whgen extracting serachFields {}", ex);
            throw new DaoException("Error whgen extracting serachFields {}", ex);
        }
    }

    /**
     * Creates a criteria query from the entity manager
     *
     * @return Criteria builder created by Entity Manager
     * @throws DaoException
     */
    private Optional<CriteriaQuery<T>> getCriteriaQuery() throws DaoException {
        log.trace(" [getCriteriaQuery] Getting criteria query for {}", entityClass.getSimpleName());
        try {
            CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
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
        log.debug(" [persist] Persisting entity of type {} with values {} ", entityClass.getSimpleName(), entity.toString());
        try {
            getEntityManager().persist(entity);
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
        log.debug(" [update] Updating entity of type {} with values {} ", entityClass.getSimpleName(), entity.toString());
        try {
            Optional<T> entityToUpdate = getById(entity.getId());
            if (entityToUpdate.isPresent()) {
                Mapper.getInstance().getMapper().map(entity, entityToUpdate.get());
                entityToUpdate.get().setUpdatedDate(new Date());
            } else {
                return Optional.empty();
            }
            return Optional.ofNullable(getEntityManager().merge(entityToUpdate.get()));
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
    public Boolean delete(String externalId) throws NotFoundException {
        try {
            Optional<T> entity = getById(externalId);
            if (entity.isPresent()) {
                return delete(entity.get());
            } else {
                log.error("No entity of type: {} found with id: {}", entityClass.getSimpleName(), externalId);
                throw new NotFoundException("Entity not found for ID: " + externalId, null);
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
        log.debug(" [delete] Deleting entity of type {} with values {} ", entityClass.getSimpleName(), entity.toString());
        try {
            getEntityManager().remove(entity);
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error(" [delete]  Error when deleting entity in Generic Dao");
            throw new DaoException(" [delete]  Error when deleting entity ", e);
        }
        
    }

    /**
     * Get an entity by its external id
     *
     * @param id The external id (GUID) of the entity
     * @return The entity that matches the id
     * @throws DaoException
     */
    public Optional<T> getById(String id) throws DaoException, NotFoundException {
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
            
        } catch (NotFoundException nfex) {
            throw new NotFoundException("Error when getting entity by id ", nfex);
        } catch (DaoException e) {
            log.error("[getById] Error when getting entity by id: {} in Generic Dao", id);
            throw new DaoException("[getById] Error when getting entity by id ", e);
        }
    }
    
    public Optional updateObject(String id, ObjectUpdate update) throws DaoException {
        log.debug("Updating Entity {} with id {} ", entityClass.getSimpleName(), id);
        Optional<T> entity = getById(id);
        
        if (entity.isPresent()) {
            
            log.trace("Entity is fetched", entityClass.getSimpleName(), id);
            
            CacheManager manager = CacheManager.getInstance();
            Cache cache = manager.getCache("entity_registry");
            
            if (cache.isKeyInCache(update.getObject())) {
                
                try {
                    Element get = cache.get(update.getObject());
                    
                    Class<?> entityClass = (Class) get.getValue();
                    Optional updateEntity = getById(update.getObjectId(), entityClass);
                    
                    Field field = entity.get().getClass().getDeclaredField(update.getObject());
                    field.setAccessible(true);
                    field.set(entity.get(), updateEntity.get());
                    field.setAccessible(false);
                    
                    return Optional.ofNullable(update(entity.get()));
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                    log.error("Error when updating Object to Entity {}", ex);
                    throw new DaoException("Error when updating Object to Entity {}", ex);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Get an entity by its external id
     *
     * @param id The external id (GUID) of the entity
     * @return The entity that matches the id
     * @throws DaoException
     */
    public Optional getById(String id, Class clazz) throws DaoException, NotFoundException {
        log.debug("Getting entity of type {} with id {} ", entityClass.getSimpleName(), id);
        try {
            CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
            CriteriaQuery cq = criteriaBuilder.createQuery(clazz);
            Root entity = cq.from(clazz);
            cq.where(entity.get(GenericEntity_.id).in(id));
            TypedQuery typedQuery = getEntityManager().createQuery(cq);
            return Optional.ofNullable(typedQuery.getSingleResult());
        } catch (Exception e) {
            log.error("[getById] Error when getting entity by id: {} in Generic Dao", id);
            throw new DaoException("[getById] Error when getting entity by id ", e);
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
            
            FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(getEntityManager());
            QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(entityClass).get();
            
            String[] keys = filter.getSearchFields().toArray(new String[filter.getSearchFields().size()]);
            Optional<String> searchWord = Optional.ofNullable(filter.getGlobalSearchWord());
            Optional<Pagination> pagination = Optional.ofNullable(filter.getPagination());
            
            if (!ArrayUtils.isEmpty(keys) && searchWord.isPresent()) {
                log.trace(" [getAll] Executing Lucene wildcard search, KEYS: {} VALUE: {}", keys, searchWord.get().toLowerCase());
                org.apache.lucene.search.Query query = qb
                        .keyword()
                        .wildcard()
                        .onFields(keys)
                        .matching(searchWord.get().toLowerCase())
                        .createQuery();
                
                Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, entityClass);
                
                persistenceQuery.setMaxResults(filter.getPagination().getListSize());
                persistenceQuery.setFirstResult(filter.getPagination().getListSize() * filter.getPagination().getPage());
                
                return Optional.ofNullable(persistenceQuery.getResultList());
            } else if (pagination.isPresent()) {
                return getAll(filter.getPagination());
            } else {
                log.error(" [getAll] No pagination, search fields or search word present, aborting search");
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error(" [getAll] Error when getting filtered list in Generic Dao");
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
        log.debug(" [getAll] Getting all values of type {} with pagination {} ", entityClass.getSimpleName(), pagination);
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
                    return executeTypedQueryList(getEntityManager().createQuery(allQuery), pagination);
                } else {
                    return executeTypedQueryList(getEntityManager().createQuery(allQuery));
                }
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error(" [getAll] Error when getting all in Generic Dao");
            throw new DaoException(" [getAll] Error when getting all in Generic Dao", e);
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
    protected Optional<T> get(CriteriaQuery<T> query) throws DaoException, NotFoundException {
        log.trace(" [get] Getting entity of type {}", entityClass.getSimpleName());
        try {
            TypedQuery<T> typedQuery = getEntityManager().createQuery(query);
            return executeTypedQuery(typedQuery);
        } catch (NotFoundException ex) {
            return Optional.empty();
        } catch (DaoException e) {
            log.error(" [get] Error when gettting entity {} in Generic DAO", query.getResultType());
            throw new DaoException(" [get] Error when gettting entity " + query.getResultType(), e);
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
        log.trace(" [executeTypedQueryList] Executing TypedQuery ( List ) for type {} with query: [ {} ]", entityClass.getSimpleName(), query.unwrap(org.hibernate.Query.class).getQueryString());
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
        log.trace(" [executeTypedQueryList] Executing TypedQuery ( Filtered List ) for type {} with query: [ {} ]", entityClass.getSimpleName(), query.unwrap(org.hibernate.Query.class).getQueryString());
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
            throw new DaoException(" [executeTypedQueryList] Error when executing TypedQuery [ Get filtered list ] for type " + entityClass.getSimpleName(), e);
        }
    }

    /**
     *
     * Executes a Typed Query
     *
     * @param query The query to execute
     * @return The queried entity list
     * @throws DaoException
     * @throws com.negod.generics.persistence.exception.NotFoundException
     */
    protected Optional<T> executeTypedQuery(TypedQuery<T> query) throws DaoException, NotFoundException {
        log.trace(" [executeTypedQuery] Executing TypedQuery ( Single Entity ) query for type {} with query: [ {} ]", entityClass.getSimpleName(), query.unwrap(org.hibernate.Query.class).getQueryString());
        try {
            T result = query.getSingleResult();
            return Optional.ofNullable(result);
        } catch (NoResultException nrex) {
            log.error("Entity not found! [ Get single entity ] for type {} ", entityClass.getSimpleName());
            throw new NotFoundException(" [executeTypedQuery] Error when executing TypedQuery [ Get single entity ] for type " + entityClass.getSimpleName(), nrex);
        } catch (EntityNotFoundException enfx) {
            log.error("Entity not found! [ Get single entity ] for type {} ", entityClass.getSimpleName());
            throw new NotFoundException("Error when executing TypedQuery [ Get single entity ] for type " + entityClass.getSimpleName(), enfx);
        } catch (Exception e) {
            log.error("Error when executing TypedQuery [ Get single entity ] for type {} ", entityClass.getSimpleName(), e);
            throw new DaoException("Error when executing TypedQuery [ Get single entity ] for type " + entityClass.getSimpleName(), e);
        }
    }

    /**
     *
     * @return
     */
    public Boolean indexEntity() {
        try {
            FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(getEntityManager());
            fullTextEntityManager.createIndexer().startAndWait();
            log.debug("SUCCESS! Done indexing {}", this.className);
        } catch (InterruptedException ex) {
            log.error("Failure when indexing" + this.className, ex);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
    
}
