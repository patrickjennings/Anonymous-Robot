package com.anonymous.engine;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class ShadowBlip {
	@PrimaryKey
	private String anonBlipId;

	@Persistent
	private String robotBlipId;

	ShadowBlip(String a, String r) {
		anonBlipId = a;
		robotBlipId = r;
	}

	public String getAnonBlipId() {
		return anonBlipId;
	}

	public String getRobotBlipId() {
		return robotBlipId;
	}

	public void anonBlipId(String a) {
		anonBlipId = a;
	}

	public void setRobotBlipId(String r) {
		robotBlipId = r;
	}
}
