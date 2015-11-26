package com.gary.dao.hibernate;

import java.io.Serializable;
import java.util.UUID;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;

public class DaoUUID implements IdentifierGenerator {

	public Serializable generate(SessionImplementor session, Object object)
			throws HibernateException {
		return generate();
	}

	public static String generate(){
		return UUID.randomUUID().toString().replace("-", "");
	}
}
