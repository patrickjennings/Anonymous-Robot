package com.anonymous.engine;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.waveprotocol.wave.model.id.WaveId;
import org.waveprotocol.wave.model.id.WaveletId;

@PersistenceCapable
class WavePair {
	@PrimaryKey
	private String key;
	
	@Persistent
	private String waveId;

	@Persistent
	private String waveletId;

	@Persistent
	private String domain;

	WavePair(String d, String w, String wl, String k) {
		domain = d;
		waveId = w;
		waveletId = wl;
		key = k;
	}

	public String getDomain() {
		return domain;
	}
	public WaveId getWaveId() {
		return new WaveId(domain, waveId);
	}

	public WaveletId getWaveletId() {
		return new WaveletId(domain, waveletId);
	}

	public String getKey() {
		return key;
	}

	public void setDomain(String d) {
		domain = d;
	}

	public void setWaveId(String w) {
		waveId = w;
	}
	
	public void setWaveletId(String w) {
		waveletId = w;
	}

	public void setKey(String k) {
		key = k;
	}
}
