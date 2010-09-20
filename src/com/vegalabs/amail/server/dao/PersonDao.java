package com.vegalabs.amail.server.dao;

import com.vegalabs.amail.server.model.Person;

public interface PersonDao {

	Person save(Person entry);

	Person getPerson(String wavemail);

}
