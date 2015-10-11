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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public class GitUtilTest {

	@Test
	public void testNormalizeBranchName$remoteRefPlusBranch() throws Exception {
		assertThat(GitUtil.normalizeBranchName("refs/remotes/origin/master"),equalTo("master"));
	}

	@Test
	public void testNormalizeBranchName$remoteRefPlusBranch$ither() throws Exception {
		assertThat(GitUtil.normalizeBranchName("refs/remotes/mine/master"),equalTo("master"));
	}

	@Test
	public void testNormalizeBranchName$shortRemotePlusBranch() throws Exception {
		assertThat(GitUtil.normalizeBranchName("origin/master"),equalTo("master"));
	}

	@Test
	public void testNormalizeBranchName$branch() throws Exception {
		assertThat(GitUtil.normalizeBranchName("feature/my-feature"),equalTo("feature/my-feature"));
	}

	@Test
	public void testNormalizeBranchName$multipleBranches() throws Exception {
		assertThat(GitUtil.normalizeBranchName("refs/remotes/mine/one refs/remotes/origin/other"),equalTo("one"));
	}

	@Test
	public void testNormalizeBranchName$multipleBranches$preferred() throws Exception {
		assertThat(GitUtil.normalizeBranchName("develop master"),equalTo("master"));
	}

}
