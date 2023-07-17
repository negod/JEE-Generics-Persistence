/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.backede.generics;

import jakarta.persistence.EntityManager;
import se.backede.generics.mock.ServiceEntitiesMock;
import se.backede.generics.mock.service.DomainEntity;
import se.backede.generics.mock.service.ServiceEntity;
import se.backede.generics.mock.service.UserEntity;
import se.backede.generics.persistence.CacheInitializer;
import se.backede.generics.persistence.DomainEntityDao;
import se.backede.generics.persistence.PersistenceUnitTest;
import se.backede.generics.persistence.ServiceEntityDao;
import se.backede.generics.persistence.UserEntityDao;
import se.backede.generics.persistence.exception.ConstraintException;
import se.backede.generics.persistence.exception.DaoException;
import se.backede.generics.persistence.update.ObjectUpdate;
import se.backede.generics.persistence.update.UpdateType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
@Slf4j
public class TestGenericDao extends ServiceEntityDao {

    static CacheInitializer CACHE;
    DomainEntityDao DOMAIN_DAO = new DomainEntityDao();
    UserEntityDao USER_DAO = new UserEntityDao();

    public TestGenericDao() throws DaoException {
    }

    @Override
    public EntityManager getEntityManager() {
        return PersistenceUnitTest.getEntityManager();
    }

    @BeforeClass
    public static void init() {
        CACHE = new CacheInitializer();
    }

    @Test
    public void testPersist() throws DaoException, ConstraintException {
        log.debug("Testing persist");

        String NAME = "PersistName";

        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        entity.setName(NAME);

        Optional<ServiceEntity> persist = executeTransaction(() -> persist(entity));

        assert persist.isPresent();
        assert persist.get().getName().equals(NAME);
    }

    @Test
    public void testUpdate() throws DaoException, ConstraintException {
        log.debug("Testing update");

        String NAME = "PersistName2";
        String UPDATED_NAME = "UPDATED_NAME";

        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        entity.setName(NAME);

        Optional<ServiceEntity> persist = executeTransaction(() -> persist(entity));

        final String NEW_ID = persist.get().getId();

        assert persist.isPresent();
        assert persist.get().getName().equals(NAME);

        Optional<ServiceEntity> byId = executeTransaction(() -> getById(NEW_ID));
        assert byId.isPresent();

        byId.get().setName(UPDATED_NAME);

        Optional<ServiceEntity> update = executeTransaction(() -> update(byId.get()));

        Optional<ServiceEntity> byIdUpdated = executeTransaction(() -> getById(NEW_ID));

        assert byIdUpdated.isPresent();
        assert byIdUpdated.get().getName().equals(UPDATED_NAME);

    }

    @Test
    public void testDeleteWithEntity() throws DaoException, ConstraintException {
        log.debug("Testing delete with entity");

        String ID = "";

        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        Optional<ServiceEntity> persist = executeTransaction(() -> persist(entity));

        ID = persist.get().getId();

        startTransaction();
        Optional<ServiceEntity> byId = getById(ID);
        assert byId.isPresent();

        Optional<Boolean> delete = delete(byId.get());
        assert delete.isPresent();

        assert delete.get().equals(Boolean.TRUE);

        Optional<ServiceEntity> deletedById = getById(ID);
        commitTransaction();
        assert !deletedById.isPresent();
    }

    @Test
    public void testDeleteWithID() throws DaoException, ConstraintException {
        log.debug("Testing delete with entity");

        String ID = "";

        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        Optional<ServiceEntity> persist = executeTransaction(() -> persist(entity));

        final String NEW_ID = persist.get().getId();

        Optional<ServiceEntity> byId = executeTransaction(() -> getById(NEW_ID));
        assert byId.isPresent();

        Optional<Boolean> delete = executeTransactionBoolean(() -> delete(NEW_ID));

        assert delete.isPresent();
        assert delete.get().equals(Boolean.TRUE);

        Optional<ServiceEntity> deletedById = executeTransaction(() -> getById(NEW_ID));
        assert !deletedById.isPresent();

    }

    @Test
    public void testManyToOneUpdateWithObject() throws DaoException, ConstraintException {

        log.debug("Testing update with OneToOne Object");

        String SERVICE_ID = "";
        String DOMAIN_ID = "";

        //Create a ServiceEntity
        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        startTransaction();
        Optional<ServiceEntity> persistedService = persist(entity);
        commitTransaction();
        SERVICE_ID = persistedService.get().getId();
        assert persistedService.isPresent();
        assert persistedService.get().getDomain() == null;

        //Create a DomainEntity
        DomainEntity domain = ServiceEntitiesMock.getDomainEntity();
        startTransaction();
        Optional<DomainEntity> persistedDomain = DOMAIN_DAO.persist(domain);
        commitTransaction();
        DOMAIN_ID = persistedDomain.get().getId();
        assert persistedDomain.isPresent();

        //Update
        ObjectUpdate objectUpdate = ServiceEntitiesMock.getObjectUpdate();
        objectUpdate.setObject("domain");
        objectUpdate.setObjectId(DOMAIN_ID);
        objectUpdate.setType(UpdateType.ADD);
        startTransaction();
        Optional<ServiceEntity> updatedServiceEntity = update(SERVICE_ID, objectUpdate);
        commitTransaction();
        assert updatedServiceEntity.isPresent();

        startTransaction();
        Optional<ServiceEntity> updatedServiceById = getById(SERVICE_ID);
        commitTransaction();
        assert updatedServiceById.isPresent();
        assert updatedServiceById.get().getDomain() != null;
        assert updatedServiceById.get().getDomain().getId().equals(DOMAIN_ID);

    }

    @Test
    public void testManyToOneUpdateWithNewObject() throws DaoException, ConstraintException {

        log.debug("Testing update with OneToOne Object");

        String SERVICE_ID = "";
        String DOMAIN_ID = "";
        String NEW_DOMAIN_ID = "";

        //Create a ServiceEntity
        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        startTransaction();
        Optional<ServiceEntity> persistedService = persist(entity);
        commitTransaction();
        SERVICE_ID = persistedService.get().getId();
        assert persistedService.isPresent();
        assert persistedService.get().getDomain() == null;

        //Create a DomainEntity
        DomainEntity domain = ServiceEntitiesMock.getDomainEntity();
        startTransaction();
        Optional<DomainEntity> persistedDomain = DOMAIN_DAO.persist(domain);
        commitTransaction();
        DOMAIN_ID = persistedDomain.get().getId();
        assert persistedDomain.isPresent();

        //Update
        ObjectUpdate objectUpdate = ServiceEntitiesMock.getObjectUpdate();
        objectUpdate.setObject("domain");
        objectUpdate.setObjectId(DOMAIN_ID);
        objectUpdate.setType(UpdateType.ADD);
        startTransaction();
        Optional<ServiceEntity> updatedServiceEntity = update(SERVICE_ID, objectUpdate);
        commitTransaction();
        assert updatedServiceEntity.isPresent();

        startTransaction();
        Optional<ServiceEntity> updatedServiceById = getById(SERVICE_ID);
        commitTransaction();
        assert updatedServiceById.isPresent();
        assert updatedServiceById.get().getDomain() != null;
        assert updatedServiceById.get().getDomain().getId().equals(DOMAIN_ID);

        //Create a new DomainEntity
        DomainEntity newDomain = ServiceEntitiesMock.getDomainEntity();
        startTransaction();
        Optional<DomainEntity> persistedNewDomain = DOMAIN_DAO.persist(newDomain);
        commitTransaction();
        NEW_DOMAIN_ID = persistedNewDomain.get().getId();
        assert persistedNewDomain.isPresent();

        //Get the persisted ServiceEntity
        startTransaction();
        Optional<ServiceEntity> serviceForUpdate = getById(SERVICE_ID);
        commitTransaction();
        assert serviceForUpdate.isPresent();

        //Set the new DomainEntity to ServiceEntity
        ObjectUpdate newObjectUpdate = ServiceEntitiesMock.getObjectUpdate();
        newObjectUpdate.setObject("domain");
        newObjectUpdate.setObjectId(NEW_DOMAIN_ID);
        newObjectUpdate.setType(UpdateType.UPDATE);
        startTransaction();
        Optional<ServiceEntity> updatedServiceEntityWithNewDomain = update(SERVICE_ID, newObjectUpdate);
        commitTransaction();
        assert updatedServiceEntityWithNewDomain.isPresent();
        assert persistedNewDomain.isPresent();

        //Get the persisted ServiceEntity
        startTransaction();
        serviceForUpdate = getById(SERVICE_ID);
        commitTransaction();
        assert serviceForUpdate.isPresent();

    }

    //TODO
    @Test
    public void testManyToOneDelete() throws DaoException, ConstraintException {

        log.debug("Testing deletion of OneToOne Object");

        String SERVICE_ID = "";
        String DOMAIN_ID = "";

        //Create a ServiceEntity
        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        startTransaction();
        Optional<ServiceEntity> persistedService = persist(entity);
        commitTransaction();
        SERVICE_ID = persistedService.get().getId();
        assert persistedService.isPresent();
        assert persistedService.get().getDomain() == null;

        //Create a DomainEntity
        DomainEntity domain = ServiceEntitiesMock.getDomainEntity();
        startTransaction();
        Optional<DomainEntity> persistedDomain = DOMAIN_DAO.persist(domain);
        commitTransaction();
        DOMAIN_ID = persistedDomain.get().getId();
        assert persistedDomain.isPresent();

        //Update
        ObjectUpdate objectUpdate = ServiceEntitiesMock.getObjectUpdate();
        objectUpdate.setObject("domain");
        objectUpdate.setObjectId(DOMAIN_ID);
        objectUpdate.setType(UpdateType.ADD);
        startTransaction();
        Optional<ServiceEntity> updatedServiceEntity = update(SERVICE_ID, objectUpdate);
        commitTransaction();
        assert updatedServiceEntity.isPresent();

        startTransaction();
        Optional<ServiceEntity> updatedServiceById = getById(SERVICE_ID);
        commitTransaction();
        assert updatedServiceById.isPresent();
        assert updatedServiceById.get().getDomain() != null;
        assert updatedServiceById.get().getDomain().getId().equals(DOMAIN_ID);

        //Delete the DomainEntity
        ObjectUpdate deleteDomain = ServiceEntitiesMock.getObjectUpdate();
        objectUpdate.setObject("domain");
        objectUpdate.setObjectId(DOMAIN_ID);
        objectUpdate.setType(UpdateType.DELETE);
        startTransaction();
        Optional<ServiceEntity> seriveWithDeletedDomain = update(SERVICE_ID, objectUpdate);
        commitTransaction();
        assert seriveWithDeletedDomain.isPresent();

        //Get the persisted ServiceEntity and assert domain removed
        startTransaction();
        Optional<ServiceEntity> serviceWithDomainDeleted = getById(SERVICE_ID);
        commitTransaction();
        assert serviceWithDomainDeleted.isPresent();
        assert serviceWithDomainDeleted.get().getDomain() == null;

    }

    @Test
    public void testGetById() throws DaoException, ConstraintException {

        log.debug("Testing getById");

        String NAME = "NAME4";

        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        entity.setName(NAME);

        startTransaction();
        Optional<ServiceEntity> persist = persist(entity);
        commitTransaction();

        assert persist.isPresent();
        assert persist.get().getName().equals(NAME);

        startTransaction();
        Optional<ServiceEntity> byId = getById(persist.get().getId());
        commitTransaction();

        assert persist.get().getId().equals(byId.get().getId());
        assert persist.get().getName().equals(byId.get().getName());
    }

    @Test
    public void testManyToManyUpdateWithObject() throws DaoException, ConstraintException {

        log.debug("Testing update with OneToOne Object");

        String SERVICE_ID = "";
        String USER_ID_1 = "";
        String USER_ID_2 = "";
        String USERS_REGION = "users";

        //Create a Service
        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        startTransaction();
        Optional<ServiceEntity> persistedService = persist(entity);
        commitTransaction();
        SERVICE_ID = persistedService.get().getId();
        assert persistedService.isPresent();
        assert persistedService.get().getDomain() == null;

        //Create User 1
        UserEntity user1 = ServiceEntitiesMock.getUserEntity();
        startTransaction();
        Optional<UserEntity> persistedUser1 = USER_DAO.persist(user1);
        commitTransaction();
        USER_ID_1 = persistedUser1.get().getId();
        assert persistedUser1.isPresent();

        //Create User 2
        UserEntity user2 = ServiceEntitiesMock.getUserEntity();
        startTransaction();
        Optional<UserEntity> persistedUser2 = USER_DAO.persist(user2);
        commitTransaction();
        USER_ID_2 = persistedUser2.get().getId();
        assert persistedUser2.isPresent();

        //Create UpdateObjects for the UserEntities
        Set<ObjectUpdate> objectUpdateSet = ServiceEntitiesMock.getObjectUpdateSet();

        ObjectUpdate objectUpdate1 = ServiceEntitiesMock.getObjectUpdate();
        objectUpdate1.setObject(USERS_REGION);
        objectUpdate1.setObjectId(USER_ID_1);
        objectUpdate1.setType(UpdateType.ADD);

        ObjectUpdate objectUpdate2 = ServiceEntitiesMock.getObjectUpdate();
        objectUpdate2.setObject(USERS_REGION);
        objectUpdate2.setObjectId(USER_ID_2);
        objectUpdate2.setType(UpdateType.ADD);

        objectUpdateSet.add(objectUpdate1);
        objectUpdateSet.add(objectUpdate2);

        //Update ServiceEntity with UserEntities
        startTransaction();
        Boolean updatedServiceEntity = update(SERVICE_ID, objectUpdateSet);
        commitTransaction();
        assert updatedServiceEntity;

        //Assert Users added
        startTransaction();
        Optional<ServiceEntity> updatedServiceById = getById(SERVICE_ID);
        commitTransaction();
        assert updatedServiceById.isPresent();
        assert updatedServiceById.get().getUsers() != null;

        assert updatedServiceById.get().getUsers().size() == 2;

        UserEntity[] userArray = (UserEntity[]) updatedServiceById.get().getUsers().toArray(new UserEntity[updatedServiceById.get().getUsers().size()]);

        if (userArray[0].getId().equals(USER_ID_1)) {
            assert userArray[1].getId().equals(USER_ID_2);
        } else {
            assert userArray[0].getId().equals(USER_ID_2);
            assert userArray[1].getId().equals(USER_ID_1);
        }

    }

    @Test
    public void testManyToManyDeleteWithObject() throws DaoException, ConstraintException {

        log.debug("Testing update with OneToOne Object");

        String SERVICE_ID = "";
        String USER_ID_1 = "";
        String USER_ID_2 = "";
        String USERS_REGION = "users";

        //Create a Service
        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        startTransaction();
        Optional<ServiceEntity> persistedService = persist(entity);
        commitTransaction();
        SERVICE_ID = persistedService.get().getId();
        assert persistedService.isPresent();
        assert persistedService.get().getDomain() == null;

        //Create User 1
        UserEntity user1 = ServiceEntitiesMock.getUserEntity();
        startTransaction();
        Optional<UserEntity> persistedUser1 = USER_DAO.persist(user1);
        commitTransaction();
        USER_ID_1 = persistedUser1.get().getId();
        assert persistedUser1.isPresent();

        //Create User 2
        UserEntity user2 = ServiceEntitiesMock.getUserEntity();
        startTransaction();
        Optional<UserEntity> persistedUser2 = USER_DAO.persist(user2);
        commitTransaction();
        USER_ID_2 = persistedUser2.get().getId();
        assert persistedUser2.isPresent();

        //Create UpdateObjects for the UserEntities
        Set<ObjectUpdate> objectUpdateSet = ServiceEntitiesMock.getObjectUpdateSet();

        ObjectUpdate objectUpdate1 = ServiceEntitiesMock.getObjectUpdate();
        objectUpdate1.setObject(USERS_REGION);
        objectUpdate1.setObjectId(USER_ID_1);
        objectUpdate1.setType(UpdateType.ADD);

        ObjectUpdate objectUpdate2 = ServiceEntitiesMock.getObjectUpdate();
        objectUpdate2.setObject(USERS_REGION);
        objectUpdate2.setObjectId(USER_ID_2);
        objectUpdate2.setType(UpdateType.ADD);

        objectUpdateSet.add(objectUpdate1);
        objectUpdateSet.add(objectUpdate2);

        //Update ServiceEntity with UserEntities
        startTransaction();
        Boolean updatedServiceEntity = update(SERVICE_ID, objectUpdateSet);
        commitTransaction();
        assert updatedServiceEntity;

        //Assert Users added
        startTransaction();
        Optional<ServiceEntity> updatedServiceById = getById(SERVICE_ID);
        //commitTransaction();
        assert updatedServiceById.isPresent();
        assert updatedServiceById.get().getUsers() != null;

        assert updatedServiceById.get().getUsers().size() == 2;

        UserEntity[] userArray = (UserEntity[]) updatedServiceById.get().getUsers().toArray(new UserEntity[updatedServiceById.get().getUsers().size()]);

        if (userArray[0].getId().equals(USER_ID_1)) {
            assert userArray[1].getId().equals(USER_ID_2);
        } else {
            assert userArray[0].getId().equals(USER_ID_2);
            assert userArray[1].getId().equals(USER_ID_1);
        }

        //Delete User1
        ObjectUpdate deleteUser1 = ServiceEntitiesMock.getObjectUpdate();
        deleteUser1.setObject(USERS_REGION);
        deleteUser1.setObjectId(USER_ID_1);
        deleteUser1.setType(UpdateType.DELETE);

        //startTransaction();
        Optional<ServiceEntity> deletedUser1 = update(SERVICE_ID, deleteUser1);
        commitTransaction();
        assert deletedUser1.isPresent();

        //Assert User1 deleted
        startTransaction();
        Optional<ServiceEntity> serviceByIdUser1Removed = getById(SERVICE_ID);
        commitTransaction();
        assert serviceByIdUser1Removed.isPresent();
        assert serviceByIdUser1Removed.get().getUsers() != null;
        assert serviceByIdUser1Removed.get().getUsers().size() == 1;

        userArray = (UserEntity[]) updatedServiceById.get().getUsers().toArray(new UserEntity[updatedServiceById.get().getUsers().size()]);
        assert userArray[0].getId().equals(USER_ID_2);

        //Delete User2
        ObjectUpdate deleteUser2 = ServiceEntitiesMock.getObjectUpdate();
        deleteUser2.setObject(USERS_REGION);
        deleteUser2.setObjectId(USER_ID_2);
        deleteUser2.setType(UpdateType.DELETE);

        startTransaction();
        Optional<ServiceEntity> deletedUser2 = update(SERVICE_ID, deleteUser2);
        commitTransaction();
        assert deletedUser2.isPresent();

        //Assert User1 and User2 deleted
        startTransaction();
        Optional<ServiceEntity> serviceByIdUser2Removed = getById(SERVICE_ID);
        commitTransaction();
        assert serviceByIdUser2Removed.isPresent();
        assert serviceByIdUser2Removed.get().getUsers() != null;

        assert serviceByIdUser2Removed.get().getUsers().isEmpty();

    }

}
