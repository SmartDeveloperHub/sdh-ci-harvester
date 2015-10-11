/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://www.smartdeveloperhub.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2015 Center for Open Middleware.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.2.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler.util;

public final class GitUtil {

	private static String[] PREFERRED={"master","develop"};

	private static final String ORIGIN = "origin/";
	private static final String REFS_REMOTES = "refs/remotes/";

	private GitUtil() {
	}

	public static String normalizeBranchName(String branchName) {
		String normalized=branchName;
		if(normalized!=null) {
			String[] parts = normalizeBranchNames(normalized);
			normalized=selectBranchName(parts);
		}
		return normalized;
	}

	private static String[] normalizeBranchNames(String normalized) {
		String[] parts=normalized.split("\\s");
		for(int i=0;i<parts.length;i++) {
			parts[i]=normalize(parts[i]);
		}
		return parts;
	}

	private static String selectBranchName(String[] parts) {
		String result=parts[0];
		if(parts.length>1) {
			int preferred = findPreferred(parts);
			if(preferred<PREFERRED.length) {
				result=PREFERRED[preferred];
			}
		}
		return result;
	}

	private static int findPreferred(String[] parts) {
		int preferred=Integer.MAX_VALUE;
		for(int i=0;i<parts.length;i++) {
			for(int j=0;j<PREFERRED.length;j++) {
				if(parts[i].equalsIgnoreCase(PREFERRED[j])) {
					preferred=Math.min(preferred, j);
				}
			}
		}
		return preferred;
	}

	private static String normalize(String normalized) {
		String tmp=normalized;
		if(tmp.startsWith(REFS_REMOTES)) {
			tmp=tmp.substring(REFS_REMOTES.length());
			tmp=tmp.substring(tmp.indexOf('/')+1);
		} else if(tmp.startsWith(ORIGIN)) {
			tmp=tmp.substring(ORIGIN.length());
		}
		return tmp;
	}

}
