/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.negod.generics;

import com.negod.generics.mock.ServiceEntitiesMock;
import com.negod.generics.mock.service.DomainEntity;
import com.negod.generics.mock.service.ServiceEntity;
import com.negod.generics.mock.service.UserEntity;
import com.negod.generics.persistence.CacheInitializer;
import com.negod.generics.persistence.DomainEntityDao;
import com.negod.generics.persistence.PersistenceUnitTest;
import com.negod.generics.persistence.ServiceEntityDao;
import com.negod.generics.persistence.UserEntityDao;
import com.negod.generics.persistence.exception.DaoException;
import com.negod.generics.persistence.update.ObjectUpdate;
import com.negod.generics.persistence.update.UpdateType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Joakim Backede ( joakim.backede@outlook.com )
 */
@Slf4j
public class TestGenericDao extends ServiceEntityDao {

    DomainEntityDao DOMAIN_DAO = new DomainEntityDao();
    UserEntityDao USER_DAO = new UserEntityDao();
    CacheInitializer CACHE = new CacheInitializer();

    public TestGenericDao() throws DaoException {
    }

    @Override
    public EntityManager getEntityManager() {
        return PersistenceUnitTest.getEntityManager();
    }

    @Test
    public void assertFields() throws DaoException {
        log.debug("Asserting fields");
        assert getEntityClass().equals(ServiceEntity.class
        );
        assert getClassName().equals(ServiceEntity.class
                .getSimpleName());
        assert getSearchFields().equals(new HashSet<>(Arrays.asList(new String[]{"detail.name", "users.name", "domain.name", "name"})));
    }

    @Test
    public void testPersist() throws DaoException {
        log.debug("Testing persist");

        String NAME = "PersistName";

        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        entity.setName(NAME);

        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> persist = persist(entity);
        getEntityManager().getTransaction().commit();

        assert persist.isPresent();
        assert persist.get().getName().equals(NAME);
    }

    @Test
    public void testUpdate() throws DaoException {
        log.debug("Testing update");

        String NAME = "PersistName2";
        String UPDATED_NAME = "UPDATED_NAME";
        String ID = "";

        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        entity.setName(NAME);

        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> persist = persist(entity);
        getEntityManager().getTransaction().commit();
        ID = persist.get().getId();

        assert persist.isPresent();
        assert persist.get().getName().equals(NAME);

        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> byId = getById(ID);
        getEntityManager().getTransaction().commit();

        assert byId.isPresent();

        byId.get().setName(UPDATED_NAME);

        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> update = update(byId.get());
        getEntityManager().getTransaction().commit();

        Optional<ServiceEntity> byIdUpdated = getById(ID);

        assert byIdUpdated.isPresent();
        assert byIdUpdated.get().getName().equals(UPDATED_NAME);

    }

    @Test
    public void testDeleteWithEntity() throws DaoException {
        log.debug("Testing delete with entity");

        String ID = "";

        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> persist = persist(entity);
        getEntityManager().getTransaction().commit();
        ID = persist.get().getId();

        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> byId = getById(ID);
        getEntityManager().getTransaction().commit();
        assert byId.isPresent();

        getEntityManager().getTransaction().begin();
        Boolean delete = delete(entity);
        getEntityManager().getTransaction().commit();
        assert delete;

        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> deletedById = getById(ID);
        getEntityManager().getTransaction().commit();
        assert !deletedById.isPresent();

    }

    @Test
    public void testDeleteWithID() throws DaoException {
        log.debug("Testing delete with entity");

        String ID = "";

        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> persist = persist(entity);
        getEntityManager().getTransaction().commit();
        ID = persist.get().getId();

        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> byId = getById(ID);
        getEntityManager().getTransaction().commit();
        assert byId.isPresent();

        getEntityManager().getTransaction().begin();
        Boolean delete = delete(ID);
        getEntityManager().getTransaction().commit();
        assert delete;

        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> deletedById = getById(ID);
        getEntityManager().getTransaction().commit();
        assert !deletedById.isPresent();

    }

    @Test
    public void testManyToOneUpdateWithObject() throws DaoException {

        log.debug("Testing update with OneToOne Object");

        String SERVICE_ID = "";
        String DOMAIN_ID = "";

        //Create a ServiceEntity
        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> persistedService = persist(entity);
        getEntityManager().getTransaction().commit();
        SERVICE_ID = persistedService.get().getId();
        assert persistedService.isPresent();
        assert persistedService.get().getDomain() == null;

        //Create a DomainEntity
        DomainEntity domain = ServiceEntitiesMock.getDomainEntity();
        getEntityManager().getTransaction().begin();
        Optional<DomainEntity> persistedDomain = DOMAIN_DAO.persist(domain);
        getEntityManager().getTransaction().commit();
        DOMAIN_ID = persistedDomain.get().getId();
        assert persistedDomain.isPresent();

        //Update 
        ObjectUpdate objectUpdate = ServiceEntitiesMock.getObjectUpdate();
        objectUpdate.setObject("domain");
        objectUpdate.setObjectId(DOMAIN_ID);
        objectUpdate.setType(UpdateType.ADD);
        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> updatedServiceEntity = update(SERVICE_ID, objectUpdate);
        getEntityManager().getTransaction().commit();
        assert updatedServiceEntity.isPresent();

        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> updatedServiceById = getById(SERVICE_ID);
        getEntityManager().getTransaction().commit();
        assert updatedServiceById.isPresent();
        assert updatedServiceById.get().getDomain() != null;
        assert updatedServiceById.get().getDomain().getId().equals(DOMAIN_ID);

    }

    @Test
    public void testManyToOneUpdateWithNewObject() throws DaoException {

        log.debug("Testing update with OneToOne Object");

        String SERVICE_ID = "";
        String DOMAIN_ID = "";
        String NEW_DOMAIN_ID = "";

        //Create a ServiceEntity
        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> persistedService = persist(entity);
        getEntityManager().getTransaction().commit();
        SERVICE_ID = persistedService.get().getId();
        assert persistedService.isPresent();
        assert persistedService.get().getDomain() == null;

        //Create a DomainEntity
        DomainEntity domain = ServiceEntitiesMock.getDomainEntity();
        getEntityManager().getTransaction().begin();
        Optional<DomainEntity> persistedDomain = DOMAIN_DAO.persist(domain);
        getEntityManager().getTransaction().commit();
        DOMAIN_ID = persistedDomain.get().getId();
        assert persistedDomain.isPresent();

        //Update 
        ObjectUpdate objectUpdate = ServiceEntitiesMock.getObjectUpdate();
        objectUpdate.setObject("domain");
        objectUpdate.setObjectId(DOMAIN_ID);
        objectUpdate.setType(UpdateType.ADD);
        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> updatedServiceEntity = update(SERVICE_ID, objectUpdate);
        getEntityManager().getTransaction().commit();
        assert updatedServiceEntity.isPresent();

        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> updatedServiceById = getById(SERVICE_ID);
        getEntityManager().getTransaction().commit();
        assert updatedServiceById.isPresent();
        assert updatedServiceById.get().getDomain() != null;
        assert updatedServiceById.get().getDomain().getId().equals(DOMAIN_ID);

        //Create a new DomainEntity
        DomainEntity newDomain = ServiceEntitiesMock.getDomainEntity();
        getEntityManager().getTransaction().begin();
        Optional<DomainEntity> persistedNewDomain = DOMAIN_DAO.persist(newDomain);
        getEntityManager().getTransaction().commit();
        NEW_DOMAIN_ID = persistedNewDomain.get().getId();
        assert persistedNewDomain.isPresent();

        //Get the persisted ServiceEntity
        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> serviceForUpdate = getById(SERVICE_ID);
        getEntityManager().getTransaction().commit();
        assert serviceForUpdate.isPresent();

        //Set the new DomainEntity to ServiceEntity  
        ObjectUpdate newObjectUpdate = ServiceEntitiesMock.getObjectUpdate();
        newObjectUpdate.setObject("domain");
        newObjectUpdate.setObjectId(NEW_DOMAIN_ID);
        newObjectUpdate.setType(UpdateType.UPDATE);
        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> updatedServiceEntityWithNewDomain = update(SERVICE_ID, newObjectUpdate);
        getEntityManager().getTransaction().commit();
        assert updatedServiceEntityWithNewDomain.isPresent();
        assert persistedNewDomain.isPresent();

        //Get the persisted ServiceEntity
        getEntityManager().getTransaction().begin();
        serviceForUpdate = getById(SERVICE_ID);
        getEntityManager().getTransaction().commit();
        assert serviceForUpdate.isPresent();

    }

    //TODO
    @Test
    public void testManyToOneDelete() throws DaoException {

        log.debug("Testing deletion of OneToOne Object");

        String SERVICE_ID = "";
        String DOMAIN_ID = "";

        //Create a ServiceEntity
        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> persistedService = persist(entity);
        getEntityManager().getTransaction().commit();
        SERVICE_ID = persistedService.get().getId();
        assert persistedService.isPresent();
        assert persistedService.get().getDomain() == null;

        //Create a DomainEntity
        DomainEntity domain = ServiceEntitiesMock.getDomainEntity();
        getEntityManager().getTransaction().begin();
        Optional<DomainEntity> persistedDomain = DOMAIN_DAO.persist(domain);
        getEntityManager().getTransaction().commit();
        DOMAIN_ID = persistedDomain.get().getId();
        assert persistedDomain.isPresent();

        //Update 
        ObjectUpdate objectUpdate = ServiceEntitiesMock.getObjectUpdate();
        objectUpdate.setObject("domain");
        objectUpdate.setObjectId(DOMAIN_ID);
        objectUpdate.setType(UpdateType.ADD);
        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> updatedServiceEntity = update(SERVICE_ID, objectUpdate);
        getEntityManager().getTransaction().commit();
        assert updatedServiceEntity.isPresent();

        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> updatedServiceById = getById(SERVICE_ID);
        getEntityManager().getTransaction().commit();
        assert updatedServiceById.isPresent();
        assert updatedServiceById.get().getDomain() != null;
        assert updatedServiceById.get().getDomain().getId().equals(DOMAIN_ID);

        //Delete the DomainEntity
        ObjectUpdate deleteDomain = ServiceEntitiesMock.getObjectUpdate();
        objectUpdate.setObject("domain");
        objectUpdate.setObjectId(DOMAIN_ID);
        objectUpdate.setType(UpdateType.DELETE);
        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> seriveWithDeletedDomain = update(SERVICE_ID, objectUpdate);
        getEntityManager().getTransaction().commit();
        assert seriveWithDeletedDomain.isPresent();

        //Get the persisted ServiceEntity and assert domain removed
        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> serviceWithDomainDeleted = getById(SERVICE_ID);
        getEntityManager().getTransaction().commit();
        assert serviceWithDomainDeleted.isPresent();
        assert serviceWithDomainDeleted.get().getDomain() == null;

    }

    @Test
    public void testGetById() throws DaoException {

        log.debug("Testing getById");

        String NAME = "NAME4";

        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        entity.setName(NAME);

        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> persist = persist(entity);
        getEntityManager().getTransaction().commit();

        assert persist.isPresent();
        assert persist.get().getName().equals(NAME);

        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> byId = getById(persist.get().getId());
        getEntityManager().getTransaction().commit();

        assert persist.get().getId().equals(byId.get().getId());
        assert persist.get().getName().equals(byId.get().getName());
    }

    @Test
    public void testManyToManyUpdateWithObject() throws DaoException {

        log.debug("Testing update with OneToOne Object");

        String SERVICE_ID = "";
        String USER_ID_1 = "";
        String USER_ID_2 = "";
        String USERS_REGION = "users";

        //Create a Service
        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> persistedService = persist(entity);
        getEntityManager().getTransaction().commit();
        SERVICE_ID = persistedService.get().getId();
        assert persistedService.isPresent();
        assert persistedService.get().getDomain() == null;

        //Create User 1
        UserEntity user1 = ServiceEntitiesMock.getUserEntity();
        getEntityManager().getTransaction().begin();
        Optional<UserEntity> persistedUser1 = USER_DAO.persist(user1);
        getEntityManager().getTransaction().commit();
        USER_ID_1 = persistedUser1.get().getId();
        assert persistedUser1.isPresent();

        //Create User 2
        UserEntity user2 = ServiceEntitiesMock.getUserEntity();
        getEntityManager().getTransaction().begin();
        Optional<UserEntity> persistedUser2 = USER_DAO.persist(user2);
        getEntityManager().getTransaction().commit();
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
        getEntityManager().getTransaction().begin();
        Boolean updatedServiceEntity = update(SERVICE_ID, objectUpdateSet);
        getEntityManager().getTransaction().commit();
        assert updatedServiceEntity;

        //Assert Users added
        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> updatedServiceById = getById(SERVICE_ID);
        getEntityManager().getTransaction().commit();
        assert updatedServiceById.isPresent();
        assert updatedServiceById.get().getUsers() != null;

        System.out.println(updatedServiceById.get().getUsers().toString());
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
    public void testManyToManyDeleteWithObject() throws DaoException {

        log.debug("Testing update with OneToOne Object");

        String SERVICE_ID = "";
        String USER_ID_1 = "";
        String USER_ID_2 = "";
        String USERS_REGION = "users";

        //Create a Service
        ServiceEntity entity = ServiceEntitiesMock.getServiceEntity();
        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> persistedService = persist(entity);
        getEntityManager().getTransaction().commit();
        SERVICE_ID = persistedService.get().getId();
        assert persistedService.isPresent();
        assert persistedService.get().getDomain() == null;

        //Create User 1
        UserEntity user1 = ServiceEntitiesMock.getUserEntity();
        getEntityManager().getTransaction().begin();
        Optional<UserEntity> persistedUser1 = USER_DAO.persist(user1);
        getEntityManager().getTransaction().commit();
        USER_ID_1 = persistedUser1.get().getId();
        assert persistedUser1.isPresent();

        //Create User 2
        UserEntity user2 = ServiceEntitiesMock.getUserEntity();
        getEntityManager().getTransaction().begin();
        Optional<UserEntity> persistedUser2 = USER_DAO.persist(user2);
        getEntityManager().getTransaction().commit();
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
        getEntityManager().getTransaction().begin();
        Boolean updatedServiceEntity = update(SERVICE_ID, objectUpdateSet);
        getEntityManager().getTransaction().commit();
        assert updatedServiceEntity;

        //Assert Users added
        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> updatedServiceById = getById(SERVICE_ID);
        getEntityManager().getTransaction().commit();
        assert updatedServiceById.isPresent();
        assert updatedServiceById.get().getUsers() != null;

        System.out.println(updatedServiceById.get().getUsers().toString());
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

        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> deletedUser1 = update(SERVICE_ID, deleteUser1);
        getEntityManager().getTransaction().commit();
        assert deletedUser1.isPresent();

        //Assert User1 deleted
        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> serviceByIdUser1Removed = getById(SERVICE_ID);
        getEntityManager().getTransaction().commit();
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

        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> deletedUser2 = update(SERVICE_ID, deleteUser2);
        getEntityManager().getTransaction().commit();
        assert deletedUser2.isPresent();

        //Assert User1 and User2 deleted
        getEntityManager().getTransaction().begin();
        Optional<ServiceEntity> serviceByIdUser2Removed = getById(SERVICE_ID);
        getEntityManager().getTransaction().commit();
        assert serviceByIdUser2Removed.isPresent();
        assert serviceByIdUser2Removed.get().getUsers() != null;

        System.out.println(serviceByIdUser2Removed.get().getUsers().toString());
        assert serviceByIdUser2Removed.get().getUsers().isEmpty();

    }

}
