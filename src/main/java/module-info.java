module se.backede.generics.persistence {

    requires static lombok;
    requires static org.mapstruct;

    requires java.xml.bind;
    requires java.transaction;
    requires java.persistence;
    requires java.annotation;

    requires org.apache.commons.lang3;
    requires org.apache.commons.collections4;

    requires org.slf4j;
    requires java.validation;

    requires ehcache;

    requires org.hibernate.orm.core;
    requires org.hibernate.search.engine;
    requires org.hibernate.search.mapper.orm;
    requires org.hibernate.search.mapper.pojo;
    requires org.hibernate.search.backend.lucene;

    exports se.backede.generics.persistence;
    exports se.backede.generics.persistence.entity;
    exports se.backede.generics.persistence.dto;
    exports se.backede.generics.persistence.exception;
    exports se.backede.generics.persistence.mapper;
    exports se.backede.generics.persistence.search;
    exports se.backede.generics.persistence.update;

}
