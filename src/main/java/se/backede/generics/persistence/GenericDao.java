package se.backede.generics.persistence;

import se.backede.generics.persistence.entity.DefaultCacheNames;
import se.backede.generics.persistence.entity.GenericEntity;
import com.negod.generics.persistence.entity.GenericEntity_;
import se.backede.generics.persistence.exception.DaoException;
import se.backede.generics.persistence.mapper.EntityBaseMapper;
import se.backede.generics.persistence.mapper.Mapper;
import se.backede.generics.persistence.search.GenericFilter;
import se.backede.generics.persistence.search.Pagination;
import se.backede.generics.persistence.update.ObjectUpdate;
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
import java.util.function.Supplier;
import javax.persistence.EntityManager;
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
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.criteria.internal.OrderImpl;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.PhraseContext;
import org.hibernate.search.query.dsl.PhraseMatchingContext;
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
    private final EntityBaseMapper mapper;

    @PersistenceContext
    EntityManager entityManager;

    public abstract EntityManager getEntityManager(String name);

    Session hibernateSession;

    /**
     * Constructor
     *
     */
    public GenericDao() {
        this.entityClass = extractEntityClass();
        log.trace("Instantiating GenericDao for entity class {} [ DatabaseLayer ] method:constructor", entityClass.getSimpleName());

        this.className = entityClass.getSimpleName();
        this.searchFields.addAll(getSearchFieldsFromCache());
        this.mapper = new EntityBaseMapper(entityClass, entityClass);
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
                if (Optional.ofNullable(cache.get(entityClass)).isPresent()) {
                    return (HashSet<String>) cache.get(entityClass).getObjectValue();
                } else {
                    log.debug("Cache for entityclass {} not present in cache", entityClass.getCanonicalName());
                }
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
    private Optional<CriteriaQuery<T>> getCriteriaQuery() {
        log.trace("Getting criteria query for {} [ DatabaseLayer ] method:getCriteriaQuery", entityClass.getSimpleName());
        try {
            CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
            return Optional.ofNullable(criteriaBuilder.createQuery(entityClass));
        } catch (Exception e) {
            log.error("Error when getting Criteria Query in Generic Dao [ DatabaseLayer ]");
            return Optional.empty();
        }
    }

    /**
     * Persist the Entity to DB
     *
     * @param entity The entity to persist
     * @return The persisted entity
     * @throws DaoException
     */
    public Optional<T> persist(T entity) {
        log.debug("Persisting entity of type {} with values {} [ DatabaseLayer ] method:persist", entityClass.getSimpleName(), entity.toString());
        try {
            getEntityManager().persist(entity);
            return Optional.ofNullable(entity);
        } catch (ConstraintViolationException e) {
            log.error("Error when persisting entity in Generic Dao: Constraing violation on field{}", e.getConstraintName());
        } catch (Exception e) {
            log.error("Error when persisting entity in Generic Dao");
        }
        return Optional.empty();
    }

    /**
     *
     * @param id
     * @param update
     * @return
     * @throws DaoException
     */
    public Boolean update(String id, Set<ObjectUpdate> update) {
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
    public Optional<T> update(String id, ObjectUpdate updateInstructions) {
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
     */
    public Optional<T> update(T entity) {
        log.debug("Updating entity of type {} with values {} [ DatabaseLayer ] method:update", entityClass.getSimpleName(), entity.toString());
        return getById(entity.getId()).map(entityToUpdate -> {
            Mapper.getInstance().getMapper().map(entity, entityToUpdate);
            entityToUpdate.setUpdatedDate(new Date());
            return Optional.ofNullable(getEntityManager().merge(entityToUpdate));
        }).orElse(Optional.empty());
    }

    /**
     * Deletes an entity with the provided External Id
     *
     * @param externalId
     * @return true or false depenent on the success of the deletion
     */
    public Optional<Boolean> delete(String externalId) {
        log.debug("Deleting entity of type {} with id {} [ DatabaseLayer ] method:delete ( with only id )", entityClass.getSimpleName(), externalId);
        return getById(externalId).map(byId -> {
            return delete(byId);
        }).orElse(Optional.empty());
    }

    /**
     *
     * Deletes the selected Entity
     *
     * @param entity The entity to delete
     * @return true or false depenent on the success of the deletion
     * @throws DaoException
     */
    protected Optional<Boolean> delete(T entity) {
        log.debug("Deleting entity of type {} with values {} [ DatabaseLayer ] method:delete ( with whole entity )", entityClass.getSimpleName(), entity.toString());
        try {
            getEntityManager().remove(entity);
            return Optional.ofNullable(Boolean.TRUE);
        } catch (Exception e) {
            log.error(" [delete]  Error when deleting entity in Generic Dao [ DatabaseLayer ]");
        }
        return Optional.empty();
    }

    /**
     * Get an entity by its external id
     *
     * @param id The external id (GUID) of the entity
     * @return The entity that matches the id
     */
    public Optional<T> getById(String id) {
        log.debug("Getting entity of type {} with id {} [ DatabaseLayer ] method:getById ( with id only )", entityClass.getSimpleName(), id);
        return this.getCriteriaQuery().map(data -> {
            Root<T> entity = data.from(entityClass);
            data.where(entity.get(GenericEntity_.id).in(id));
            return get(data);
        }).orElse(Optional.empty());
    }

    /**
     * Get an entity by its external id
     *
     * @param id The external id (GUID) of the entity
     * @return The entity that matches the id
     * @throws DaoException
     */
    private Optional<T> getById(String id, Class clazz) {
        log.debug("Getting entity (Generic method) int DAO for {} with id {} [ DatabaseLayer ] method:getById ( with id and class )", entityClass.getSimpleName(), id);
        try {
            CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
            CriteriaQuery cq = criteriaBuilder.createQuery(clazz);
            Root entity = cq.from(clazz);
            cq.where(entity.get(GenericEntity_.id).in(id));
            TypedQuery<T> typedQuery = getEntityManager().createQuery(cq);
            return Optional.ofNullable(typedQuery.getSingleResult());
        } catch (Exception e) {
            log.error("[getById] Error when getting entity by id: {} in Generic Dao [ DatabaseLayer ]", id);
        }
        return Optional.empty();
    }

    /**
     *
     * Gets all entities that are persisted to the database
     *
     * @param filter The filter for the search
     * @return All persisted entities
     * @throws DaoException
     */
    public Optional<Set<T>> search(GenericFilter filter) throws DaoException {
        log.debug("Getting all values of type {} and filter {} [ DatabaseLayer ] method:search ", entityClass.getSimpleName(), filter.toString());
        try {

            FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(getEntityManager());
            QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(entityClass).get();

            String[] keys = filter.getSearchFields().toArray(new String[filter.getSearchFields().size()]);
            Optional<String> searchWord = Optional.ofNullable(filter.getGlobalSearchWord());
            Optional<Pagination> pagination = Optional.ofNullable(filter.getPagination());

            if (!ArrayUtils.isEmpty(keys) && searchWord.isPresent()) {

                String[] specialChars = {"-", "!", "?"};

                String keyword = searchWord.get();

                for (String specialChar : specialChars) {
                    if (keyword.contains(specialChar)) {
                        keyword = keyword.replace(specialChar, "\\" + specialChar);
                    }
                }

                log.trace("Executing Lucene wildcard search, KEYS: {} VALUE: {} [ DatabaseLayer ] method:search", keys, searchWord.get().toLowerCase());

                org.apache.lucene.search.Query query;

                switch (filter.getSearchMatch()) {
                    case EXACT_MATCH:
                        PhraseContext phrase = qb.phrase().withSlop(0);
                        PhraseMatchingContext onField = phrase.onField(keys[0]);
                        if (keys.length > 1) {
                            for (int i = 1; i < keys.length; i++) {
                                onField.andField(keys[i]);
                            }
                        }
                        query = onField.sentence(keyword.toLowerCase()).createQuery();
                        break;
                    case STANDARD:
                        query = qb
                                .keyword()
                                .onFields(keys)
                                .matching(keyword.toLowerCase())
                                .createQuery();
                        break;
                    case WILDCARD:
                        query = qb
                                .keyword()
                                .wildcard()
                                .onFields(keys)
                                .matching(keyword.toLowerCase())
                                .createQuery();
                        break;
                    default:
                        throw new AssertionError();
                }

                Query persistenceQuery = fullTextEntityManager.createFullTextQuery(query, entityClass);

                if (pagination.isPresent()) {
                    persistenceQuery.setMaxResults(filter.getPagination().getListSize());
                    persistenceQuery.setFirstResult(filter.getPagination().getListSize() * filter.getPagination().getPage());
                } else {
                    log.info("Pagination not present in query returning all data");
                }

                Set<T> resultList = new HashSet<T>(persistenceQuery.getResultList());
                return Optional.ofNullable(resultList);
            } else {
                log.error("Either search fields or search word or all is not present, aborting search [ DatabaseLayer ] method:search, "
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
    public Optional<List<T>> getAll(Pagination pagination) {
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
            return Optional.empty();
        }
    }

    public Optional<List<T>> getAll() {
        log.debug("Getting all values of type {} [ DatabaseLayer ] method:getAll ( creating empty pagination )", entityClass.getSimpleName());
        return getAll(new Pagination());
    }

    /**
     *
     * Gets an entity based on a Criteria query
     *
     * @param query The query to executeTransaction
     * @return The queried entity
     * @throws DaoException
     * @throws se.backede.generics.persistence.exception.NotFoundException
     */
    public Optional<T> get(CriteriaQuery<T> query) {
        log.trace("Getting entity of type {} [ DatabaseLayer ] method:get", entityClass.getSimpleName());
        TypedQuery<T> typedQuery = getEntityManager().createQuery(query);
        return executeTypedQuery(typedQuery);
    }

    /**
     *
     * Executes a Typed Query
     *
     * @param query The query to executeTransaction
     * @return The queried entity
     * @throws DaoException
     */
    protected Optional<List<T>> executeTypedQueryList(TypedQuery<T> query) {
        log.trace("Executing TypedQuery ( List ) for type {} with query: [ {} ] [ DatabaseLayer ] method:executeTypedQueryList ( with TypedQuery )", entityClass.getSimpleName(), query.unwrap(org.hibernate.Query.class).getQueryString());
        return Optional.ofNullable(query.getResultList());
    }

    /**
     *
     * Executes a Typed Query
     *
     * @param query The query to executeTransaction
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
        }
        return Optional.empty();
    }

    /**
     *
     * Executes a Typed Query
     *
     * @param query The query to executeTransaction
     * @return The queried entity list
     */
    protected Optional<T> executeTypedQuery(TypedQuery<T> query) {
        log.trace("Executing TypedQuery ( Single Entity ) query for type {} with query: [ {} ] [ DatabaseLayer ] method:executeTypedQuery", entityClass.getSimpleName(), query.unwrap(org.hibernate.Query.class).getQueryString());
        try {
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException ex) {
            log.debug("No result for TypedQuery [ Get single entity ] for type {} [ DatabaseLayer ]", entityClass.getSimpleName(), ex.getMessage());
        } catch (Exception e) {
            log.error("Error when executing TypedQuery [ Get single entity ] for type {} [ DatabaseLayer ] QUERY: {}", entityClass.getSimpleName(), query.unwrap(org.hibernate.Query.class).getQueryString(), e);
        }
        return Optional.empty();
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

    public synchronized Optional<Boolean> startTransaction() {
        try {
            getEntityManager().getTransaction().begin();
            return Optional.of(Boolean.TRUE);
        } catch (Exception ex) {
            log.error("Failure starting transaction {} [ DatabaseLayer ] ErrorMessage: {}", this.className, ex);
            return Optional.empty();
        }
    }

    public synchronized Optional<Boolean> commitTransaction() {
        try {
            getEntityManager().getTransaction().commit();
            getEntityManager().close();
            return Optional.of(Boolean.TRUE);
        } catch (Exception ex) {
            log.error("Failure when committing transaction {} [ DatabaseLayer ] ErrorMessage: {}", this.className, ex);
            return Optional.empty();
        }
    }

    /**
     * Execute a supplier method and use transactions
     *
     * @param supplier
     * @return
     */
    public Optional<T> executeTransaction(Supplier<Optional<T>> supplier) {
        Supplier<Optional<T>> getCompany = () -> {
            this.startTransaction();
            Optional<T> get = supplier.get();
            return this.commitTransaction().map(success -> {
                return get;
            }).orElse(Optional.empty());
        };
        return getCompany.get();
    }

    public Optional<Boolean> executeTransactionBoolean(Supplier<Optional<Boolean>> supplier) {
        Supplier<Optional<Boolean>> getData = () -> {
            this.startTransaction();
            Optional<Boolean> get = supplier.get();
            this.commitTransaction();
            return get;
        };
        return getData.get();
    }

    public Optional<List<T>> executeTransactionList(Supplier<Optional<List<T>>> supplier) {
        Supplier<Optional<List<T>>> getCompany = () -> {
            this.startTransaction();
            Optional<List<T>> get = supplier.get();
            this.commitTransaction();
            return get;
        };
        return getCompany.get();
    }

}
