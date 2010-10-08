package com.vegalabs.amail.server.dao;

import com.vegalabs.amail.server.model.SeriallizableParticipantProfile;

public interface SeriallizableParticipantProfileDao {

	SeriallizableParticipantProfile save(SeriallizableParticipantProfile entry);

	SeriallizableParticipantProfile getSeriallizableParticipantProfile(
			String email);

}
