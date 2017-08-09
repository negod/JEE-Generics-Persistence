package com.negod.generics.persistence;

import com.negod.generics.persistence.entity.DefaultCacheNames;
import com.negod.generics.persistence.entity.GenericEntity;
import com.negod.generics.persistence.entity.GenericEntity_;
import com.negod.generics.persistence.exception.DaoException;
import com.negod.generics.persistence.exception.NotFoundException;
import com.negod.generics.persistence.mapper.BaseMapper;
import com.negod.generics.persistence.mapper.Mapper;
import com.negod.generics.persistence.search.GenericFilter;
import com.negod.generics.persistence.search.Pagination;
import com.negod.generics.persistence.update.ObjectUpdate;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
    private final BaseMapper mapper;

    @PersistenceContext
    EntityManager entityManager;

    /**
     * Constructor
     *
     * @param entityClass The entityclass the DAO will handle
     * @throws DaoException
     */
    public GenericDao() {
        this.entityClass = extractEntityClass();
        log.trace("Instantiating GenericDao for entity class {} [ DatabaseLayer ] method:constructor", entityClass.getSimpleName());

        this.className = entityClass.getSimpleName();
        this.searchFields.addAll(getSearchFieldsFromCache());
        this.mapper = new BaseMapper(entityClass, entityClass);
    }

    private Class<T> extractEntityClass() {
        Type genericSuperClass = getClass().getGenericSuperclass();

        ParameterizedType parametrizedType = null;
        while (parametrizedType == null) {
            if ((genericSuperClass instanceof ParameterizedType)) {
                parametrizedType = (ParameterizedType) genericSuperClass;
            } else {
                genericSuperClass = ((Class<?>) genericSuperClass).getGenericSuperclass();
            }
        }
        return (Class<T>) parametrizedType.getActualTypeArguments()[0];
    }

    private Set<String> getSearchFieldsFromCache() {
        log.trace("Getting SearchFields for class: {} [ DatabaseLayer ] method:getSearchFieldsFromCache", entityClass.getSimpleName());
        try {
            CacheManager manager = CacheManager.getInstance();
            Cache cache = manager.getCache(DefaultCacheNames.SEARCH_FIELD_CACHE);
            if (!cache.isDisabled()) {
                return (HashSet<String>) cache.get(entityClass).getValue();
            }
        } catch (CacheException | ClassCastException | IllegalStateException ex) {
            log.error("Error when getting search fields for class {} [ DatabaseLayer ] ErrorMessage:{}", entityClass.getSimpleName(), ex);
        }
        return new HashSet<>();
    }

    /**
     * Creates a criteria query from the entity manager
     *
     * @return Criteria builder created by Entity Manager
     * @throws DaoException
     */
    private Optional<CriteriaQuery<T>> getCriteriaQuery() throws DaoException {
        log.trace("Getting criteria query for {} [ DatabaseLayer ] method:getCriteriaQuery", entityClass.getSimpleName());
        try {
            CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
            return Optional.ofNullable(criteriaBuilder.createQuery(entityClass));
        } catch (Exception e) {
            log.error("Error when getting Criteria Query in Generic Dao [ DatabaseLayer ]");
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
        log.debug("Persisting entity of type {} with values {} [ DatabaseLayer ] method:persist", entityClass.getSimpleName(), entity.toString());
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
     * @param id
     * @param update
     * @return
     * @throws DaoException
     */
    public Boolean update(String id, Set<ObjectUpdate> update) throws DaoException {
        log.debug("Updating entitylist of type {} with values {} [ DatabaseLayer ] method:update", entityClass.getSimpleName(), update.toString());
        for (ObjectUpdate objectUpdate : update) {
            Optional<T> update1 = update(id, objectUpdate);
        }
        return true;
    }

    private Optional<Class<?>> getEntityClassToUpdate(String objectName) {
        CacheManager manager = CacheManager.getInstance();
        Cache cache = manager.getCache(DefaultCacheNames.ENTITY_REGISTRY_CACHE);
        if (cache.isKeyInCache(entityClass)) {
            Element get = cache.get(entityClass);
            HashMap<String, Class> cachedData = (HashMap<String, Class>) get.getValue();
            Class<?> entityClassToUpdate = cachedData.get(objectName);
            return Optional.ofNullable(entityClassToUpdate);
        } else {
            return Optional.empty();
        }
    }

    /**
     *
     * @param id The id of the Entity to update
     * @param updateInstructions The data of the object to ADD, REMOVE or UPDATE
     * @return
     * @throws DaoException
     */
    public Optional<T> update(String id, ObjectUpdate updateInstructions) throws DaoException {
        log.debug("Updating Entity {} with id {} [ DatabaseLayer ] method:update", entityClass.getSimpleName(), id);
        Optional<T> entityForUpdate = getById(id);

        if (entityForUpdate.isPresent()) {
            log.trace("Entity is fetched [ DatabaseLayer ] method:update", entityClass.getSimpleName(), id);
            try {
                Optional<Class<?>> entityClassToUpdate = getEntityClassToUpdate(updateInstructions.getObject());
                if (entityClassToUpdate.isPresent()) {

                    Optional entity = getById(updateInstructions.getObjectId(), entityClassToUpdate.get());
                    Field field = entityForUpdate.get().getClass().getDeclaredField(updateInstructions.getObject());
                    field.setAccessible(true);

                    Class<?> clazz = field.getType();
                    if (clazz.equals(Set.class) || clazz.equals(List.class)) {
                        invokeGenericSetData(field, entity, entityForUpdate, updateInstructions);
                    } else {
                        invokeGenericObjectData(field, entity, entityForUpdate, updateInstructions);
                    }
                    field.setAccessible(false);
                }

                return Optional.ofNullable(entityForUpdate.get());
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
                log.error("Error when updating Object to Entity {} [ DatabaseLayer ]", ex);
                throw new DaoException("Error when updating Object to Entity {}", ex);
            }
        }
        return Optional.empty();
    }

    private void invokeGenericObjectData(Field field, Optional updateEntity, Optional<T> entity, ObjectUpdate update) throws IllegalArgumentException, IllegalAccessException, AssertionError {
        log.trace("Invoking ObjectData on entity {} with Object {} [ DatabaseLayer ] method:invokeGenericObjectData", entityClass.getSimpleName(), entity.get().getClass().getSimpleName());
        switch (update.getType()) {
            case ADD:
            case UPDATE:
                field.set(entity.get(), updateEntity.get());
                break;
            case DELETE:
                field.set(entity.get(), null);
                break;
            default:
                throw new AssertionError();
        }
    }

    private void invokeGenericSetData(Field field, Optional updateEntity, Optional<T> entity, ObjectUpdate update) throws InvocationTargetException, SecurityException, IllegalAccessException, IllegalArgumentException, AssertionError, NoSuchMethodException {
        log.trace("Invoking ListData on entity {} with Object {} [ DatabaseLayer ] method:invokeGenericSetData", entityClass.getSimpleName(), entity.get().getClass().getSimpleName());

        ParameterizedType objectListType = (ParameterizedType) field.getGenericType();
        Class<?> clazz = (Class<?>) objectListType.getActualTypeArguments()[0];
        if (clazz.getName().equals(updateEntity.get().getClass().getName())) {
            Method add = entity.get().getClass().getDeclaredMethod("get" + StringUtils.capitalize(update.getObject()));
            Set entitySet = (Set) add.invoke(entity.get());

            switch (update.getType()) {
                case ADD:
                case UPDATE:
                    entitySet.add(updateEntity.get());
                    break;
                case DELETE:
                    entitySet.remove(updateEntity.get());
                    break;
                default:
                    throw new AssertionError();
            }
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
        log.debug("Updating entity of type {} with values {} [ DatabaseLayer ] method:update", entityClass.getSimpleName(), entity.toString());
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
            log.error("Error when updating entity in Generic Dao [ DatabaseLayer ] ");
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
        log.debug("Deleting entity of type {} with id {} [ DatabaseLayer ] method:delete ( with only id )", entityClass.getSimpleName(), externalId);
        try {
            Optional<T> entity = getById(externalId);
            if (entity.isPresent()) {
                return delete(entity.get());
            } else {
                log.error("No entity of type: {} found with id: {} [ DatabaseLayer ]", entityClass.getSimpleName(), externalId);
                throw new NotFoundException("Entity not found for ID: " + externalId, null);
            }
        } catch (DaoException ex) {
            log.error("Error when deleting entity of type: {} with id: {}. ErrorMessage: {} [ DatabaseLayer ]", entityClass.getSimpleName(), externalId, ex.getMessage());
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
        log.debug("Deleting entity of type {} with values {} [ DatabaseLayer ] method:delete ( with whole entity )", entityClass.getSimpleName(), entity.toString());
        try {
            getEntityManager().remove(entity);
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error(" [delete]  Error when deleting entity in Generic Dao [ DatabaseLayer ]");
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
        log.debug("Getting entity of type {} with id {} [ DatabaseLayer ] method:getById ( with id only )", entityClass.getSimpleName(), id);
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
            log.error("Entity not found when getting entity by id: {} in Generic Dao [ DatabaseLayer ]", id);
            throw new NotFoundException("Error when getting entity by id ", nfex);
        } catch (DaoException e) {
            log.error("Error when getting entity by id: {} in Generic Dao [ DatabaseLayer ]", id);
            throw new DaoException("[getById] Error when getting entity by id ", e);
        }
    }

    /**
     * Get an entity by its external id
     *
     * @param id The external id (GUID) of the entity
     * @return The entity that matches the id
     * @throws DaoException
     */
    private Optional getById(String id, Class clazz) throws DaoException, NotFoundException {
        log.debug("Getting entity (Generic method) int DAO for {} with id {} [ DatabaseLayer ] method:getById ( with id and class )", entityClass.getSimpleName(), id);
        try {
            CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
            CriteriaQuery cq = criteriaBuilder.createQuery(clazz);
            Root entity = cq.from(clazz);
            cq.where(entity.get(GenericEntity_.id).in(id));
            TypedQuery typedQuery = getEntityManager().createQuery(cq);
            return Optional.ofNullable(typedQuery.getSingleResult());
        } catch (Exception e) {
            log.error("[getById] Error when getting entity by id: {} in Generic Dao [ DatabaseLayer ]", id);
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
    public Optional<List<T>> search(GenericFilter filter) throws DaoException {
        log.debug("Getting all values of type {} and filter {} [ DatabaseLayer ] method:search ", entityClass.getSimpleName(), filter.toString());
        try {

            FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(getEntityManager());
            QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(entityClass).get();

            String[] keys = filter.getSearchFields().toArray(new String[filter.getSearchFields().size()]);
            Optional<String> searchWord = Optional.ofNullable(filter.getGlobalSearchWord());
            Optional<Pagination> pagination = Optional.ofNullable(filter.getPagination());

            if (!ArrayUtils.isEmpty(keys) && searchWord.isPresent() && pagination.isPresent()) {
                log.trace("Executing Lucene wildcard search, KEYS: {} VALUE: {} [ DatabaseLayer ] method:search", keys, searchWord.get().toLowerCase());
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
            } else {
                log.error("Either pagination, search fields or search word or all is not present, aborting search [ DatabaseLayer ] method:search, "
                        + "Present? [Pagination:{} SearchWord:{} SearchFields:{} ]", pagination.isPresent(), searchWord.isPresent(), ArrayUtils.isEmpty(keys));
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error(" [getAll] Error when getting filtered list in Generic Dao [ DatabaseLayer ]");
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
        log.debug("Getting all values of type {} with pagination {} [ DatabaseLayer ] method:getAll ( with pagination )", entityClass.getSimpleName(), pagination);
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
        } catch (DaoException ex) {
            log.error("Error when getting all in Generic Dao [ DatabaseLayer ] {} ", ex);
            throw new DaoException("Error when getting all in Generic Dao", ex);
        }
    }

    public Optional<List<T>> getAll() throws DaoException {
        log.debug("Getting all values of type {} [ DatabaseLayer ] method:getAll ( creating empty pagination )", entityClass.getSimpleName());
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
        log.trace("Getting entity of type {} [ DatabaseLayer ] method:get", entityClass.getSimpleName());
        try {
            TypedQuery<T> typedQuery = getEntityManager().createQuery(query);
            return executeTypedQuery(typedQuery);
        } catch (NotFoundException ex) {
            return Optional.empty();
        } catch (DaoException e) {
            log.error("Error when gettting entity {} in Generic DAO", query.getResultType());
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
        log.trace("Executing TypedQuery ( List ) for type {} with query: [ {} ] [ DatabaseLayer ] method:executeTypedQueryList ( with TypedQuery )", entityClass.getSimpleName(), query.unwrap(org.hibernate.Query.class).getQueryString());
        try {
            List<T> resultList = query.getResultList();
            return Optional.ofNullable(resultList);
        } catch (Exception e) {
            log.error("Error when executing TypedQuery [ Get list ] for type {} [ DatabaseLayer ]", entityClass.getSimpleName());
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
        log.trace("Executing TypedQuery ( Filtered List ) for type {} with query: [ {} ] [ DatabaseLayer ] method:executeTypedQueryList ( with pagination )", entityClass.getSimpleName(), query.unwrap(org.hibernate.Query.class).getQueryString());
        try {

            if (Optional.ofNullable(pagination).isPresent()) {

                Optional<Integer> listSize = Optional.ofNullable(pagination.getListSize());
                Optional<Integer> page = Optional.ofNullable(pagination.getPage());

                if (listSize.isPresent() && page.isPresent()) {
                    query.setMaxResults(pagination.getListSize());
                    query.setFirstResult(pagination.getListSize() * pagination.getPage());
                } else {
                    log.error("Pagination present but listsize or page missing {} returning empty list [ DatabaseLayer ] method:executeTypedQueryList ( with TypedQuery and pagination )", pagination);
                    return Optional.empty();
                }

            }

            List<T> resultList = query.getResultList();
            return Optional.ofNullable(resultList);
        } catch (Exception e) {
            log.error("Error when executing TypedQuery [ Get filtered list ] for type {} [ DatabaseLayer ]", entityClass.getSimpleName());
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
        log.trace("Executing TypedQuery ( Single Entity ) query for type {} with query: [ {} ] [ DatabaseLayer ] method:executeTypedQuery", entityClass.getSimpleName(), query.unwrap(org.hibernate.Query.class).getQueryString());
        try {
            T result = query.getSingleResult();
            return Optional.ofNullable(result);
        } catch (NoResultException | EntityNotFoundException nrex) {
            log.error("Entity not found! [ Get single entity ] for type {} [ DatabaseLayer ]", entityClass.getSimpleName());
            throw new NotFoundException(" [executeTypedQuery] Error when executing TypedQuery [ Get single entity ] for type " + entityClass.getSimpleName(), nrex);
        } catch (Exception e) {
            log.error("Error when executing TypedQuery [ Get single entity ] for type {} [ DatabaseLayer ]", entityClass.getSimpleName(), e);
            throw new DaoException("Error when executing TypedQuery [ Get single entity ] for type " + entityClass.getSimpleName(), e);
        }
    }

    /**
     *
     * @return
     */
    public Boolean indexEntity() {
        log.debug("Starting Lucene indexing of type {} [ DatabaseLayer ] method:indexEntity", entityClass.getSimpleName());
        try {
            FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(getEntityManager());
            fullTextEntityManager.createIndexer().startAndWait();
            log.debug("SUCCESS! Indexing started....... returning {} [ DatabaseLayer ]", this.className);
        } catch (InterruptedException ex) {
            log.error("Failure when indexing {} [ DatabaseLayer ] ErrorMessage: {}", this.className, ex);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

}
