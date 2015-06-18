package org.smartdeveloperhub.jenkins.crawler.event;

import java.net.URI;
import java.util.Date;

public final class ServiceFoundEvent extends JenkinsEvent {

	private ServiceFoundEvent(URI service, Date date) {
		super(service, date);
	}

	@Override
	void accept(JenkinsEventVisitor visitor) {
		if(visitor!=null) {
			visitor.visitServiceFoundEvent(this);
		}
	}

	static ServiceFoundEvent create(URI service) {
		return new ServiceFoundEvent(service, new Date());
	}

}