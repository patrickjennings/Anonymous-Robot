/*
 * Created by: Patrick Jennings
 * 
 * Uses events of a wave server in order to setup
 * anonymous question asking by the participants
 * of a wave.
 * 
 * TODO: Update datastore and blips on deletion
 * TODO: Update datastore on removal of robot
 */

package com.anonymous.engine;

import com.google.wave.api.*;
import com.google.wave.api.JsonRpcConstant.ParamsProperty;
import com.google.wave.api.event.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

public class AnonymousEngineServlet extends AbstractRobot {

	private static final String CONSUMER_KEY = "anonymous@sycadellicman.ath.cx";
	private static final String CONSUMER_SECRET = "Mh6uOkHHlGH3ropxiDTH4QSSHBiwjrexe8rhP_pRoYJwTWdX";
	private static final String DOMAIN_ROOT = "http://sycadellicman.ath.cx:9898";
	private static final String rpcserverurl = DOMAIN_ROOT + "/robot/rpc";

	public AnonymousEngineServlet() {
		setupOAuth(CONSUMER_KEY, CONSUMER_SECRET, rpcserverurl);
		setAllowUnsignedRequests(true);
	}

	@Override
	protected String getRobotName() {
		return "Anonymous Robot";
	}

	@Override
	protected String getRobotProfilePageUrl() {
		return "http://anonymousgadget.appspot.com/";
	}

	/*
	 * When the robot is added to a wave, it creates a new wavelet
	 * for each participant which may be used by the participant
	 * in order to ask anonymous questions. The associated IDs are
	 * stored in persistent storage.
	 */
	@Override
	public void onWaveletSelfAdded(WaveletSelfAddedEvent event) {
		Wavelet wavelet;
		String domain, robot;
		Iterator<String> it;
		PersistenceManager pm = PMF.get().getPersistenceManager();

		wavelet = event.getWavelet();
		robot = wavelet.getRobotAddress();
		domain = wavelet.getDomain();

		if(wavelet.getCreator().equals(robot))	// Prevent possible infinite recursion
			return;

		wavelet.reply("\n\nAnonymous Question Robot added.\n");

		it = wavelet.getParticipants().iterator();
		while(it.hasNext()) {
			Wavelet newwave;
			String student = (String)it.next();
			Set<String> partset = new HashSet<String>();

			if(robot.equals(student))
				continue;

			partset.add(student);
			partset.add(robot);
			newwave = newWave(domain, partset);
			newwave.setRobotAddress(robot);
			newwave.getRootBlip().append("Type anonymous questions here!\n" +
					"Creator: " + wavelet.getCreator() + "\nWave description: " +
					wavelet.getRootBlip().getContent());

			try {
				String newwaveid = getParam(submit(newwave, rpcserverurl),
						ParamsProperty.WAVE_ID);

				pm.makePersistent(new WavePair(domain, wavelet.getWaveId().getId(),
						wavelet.getWaveletId().getId(), newwaveid));
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				pm.close();
			}
		}
	}

	/*
	 * This event is called whenever a blip is created, edited,
	 * or deleted. This will be used by the robot in order to
	 * delegate the anonymous questions.
	 */
	@Override
	public void onDocumentChanged(DocumentChangedEvent event) {
		Wavelet wave;
		String bid;
		ShadowBlip blip;
		PersistenceManager pm = PMF.get().getPersistenceManager();
		String newcontent = event.getBlip().getContent();
		/* We don't want to use the robot blip */
		if(event.getBlip().getCreator().equals(event.getWavelet().getRobotAddress()))
			return;
		try {
			/* Fetch the wavelet that the robot should copy the content to. */
			WavePair wp = pm.getObjectById(WavePair.class, event.getWavelet().getWaveId().getDomain() + 
					"!" + event.getWavelet().getWaveId().getId());
			wave = fetchWavelet(wp.getWaveId(), wp.getWaveletId(), rpcserverurl);
		} catch(IOException e) {
			/* The event was not from a watched wavelet. */
			return;
		} try {
			/* Get ID of blip that was edited */
			bid = event.getBlip().getBlipId();
		} catch(NullPointerException e) {
			/* Blip was deleted, no ID associated */
			return;
		} try {
			/* Fetch the stored blip from the datastore. */
			blip = pm.getObjectById(ShadowBlip.class, bid);
		} catch(JDOObjectNotFoundException e) {
			/* Blip was just created and is not in the datastore. */
			wave.reply(newcontent);
			/* Create new anonymous blip as the robot */
			String newblipid;
			try {
				newblipid = getParam(submit(wave, rpcserverurl), ParamsProperty.NEW_BLIP_ID);
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}
			/* Create new Shadowblip and add it to the datastore. */
			blip = new ShadowBlip(bid, newblipid);
			pm.makePersistent(blip);
			return;
		} finally {
			pm.close();
		}
		/* Replace content of blip with the edited content and submit to server */
		wave.getBlip(blip.getRobotBlipId()).all().replace(newcontent);
		try {
			submit(wave, rpcserverurl);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Originally written by Avishay Balderman
	 * http://www.mail-archive.com/google-wave-api@googlegroups.com/msg04969.html
	 * Modified by Patrick Jennings
	*/
	private String getParam(List<JsonRpcResponse> response, ParamsProperty p) {
		for(JsonRpcResponse jsonRpcResponse : response) {
			Map<ParamsProperty, Object> data = jsonRpcResponse.getData();
			String param = (String) data.get(p);
			if(param != null)
				return param;
		}
		return null;
	}
}