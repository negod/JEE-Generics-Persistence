module se.backede.generics.persietence {

    requires java.xml.bind;
    requires java.transaction;
    requires lombok;
    requires java.persistence;
    requires org.apache.commons.lang3;
    requires org.apache.lucene.core;
    requires org.hibernate.orm.core;
    requires org.slf4j;

    requires org.hibernate.search.engine;
    requires org.hibernate.search.orm;
    requires lucene.analyzers.common;

    exports se.backede.generics.persistence;
    exports se.backede.generics.persistence.entity;
    exports se.backede.generics.persistence.dto;
    exports se.backede.generics.persistence.exception;
    exports se.backede.generics.persistence.mapper;
    exports se.backede.generics.persistence.search;
    exports se.backede.generics.persistence.update;

}
