module se.backede.generics.persistence {

    requires static lombok;
    requires static org.mapstruct;

    requires java.xml.bind;
    requires java.annotation;
    requires java.validation;
    requires jakarta.persistence;
    requires jakarta.transaction;

    requires org.apache.commons.lang3;
    requires org.apache.commons.collections4;

    requires org.slf4j;

    requires ehcache;

    requires org.hibernate.orm.core;

    exports se.backede.generics.persistence;
    exports se.backede.generics.persistence.entity;
    exports se.backede.generics.persistence.dto;
    exports se.backede.generics.persistence.exception;
    exports se.backede.generics.persistence.mapper;
    exports se.backede.generics.persistence.search;
    exports se.backede.generics.persistence.update;

}
