/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://www.smartdeveloperhub.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2015-2016 Center for Open Middleware.
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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.3.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

public class GitUtilTest {

	@Test
	public void testNormalizeBranchName$null() throws Exception {
		assertThat(GitUtil.normalizeBranchName(null),nullValue());
	}

	@Test
	public void testNormalizeBranchName$branch() throws Exception {
		assertThat(GitUtil.normalizeBranchName("branchName"),equalTo("branchName"));
	}

	@Test
	public void testNormalizeBranchName$shortRemotePlusBranch() throws Exception {
		assertThat(GitUtil.normalizeBranchName("origin/master"),equalTo("master"));
	}

	@Test
	public void testNormalizeBranchName$ambiguousBranch() throws Exception {
		assertThat(GitUtil.normalizeBranchName("feature/my-feature"),equalTo("my-feature"));
	}

	@Test
	public void testNormalizeBranchName$remotes$simpleBranch() throws Exception {
		assertThat(GitUtil.normalizeBranchName("remotes/repoName/branchName"),equalTo("branchName"));
	}

	@Test
	public void testNormalizeBranchName$remotes$compositeBranch() throws Exception {
		assertThat(GitUtil.normalizeBranchName("remotes/repoName/composite/branchName"),equalTo("composite/branchName"));
	}

	@Test
	public void testNormalizeBranchName$remoteRefPlusBranch() throws Exception {
		assertThat(GitUtil.normalizeBranchName("refs/remotes/origin/master"),equalTo("master"));
	}

	@Test
	public void testNormalizeBranchName$remoteRefPlusBranch$other() throws Exception {
		assertThat(GitUtil.normalizeBranchName("refs/remotes/mine/master"),equalTo("master"));
	}

	@Test
	public void testNormalizeBranchName$simpleHeadRef() throws Exception {
		assertThat(GitUtil.normalizeBranchName("refs/heads/branchName"),equalTo("branchName"));
	}

	@Test
	public void testNormalizeBranchName$compositeHeadRef() throws Exception {
		assertThat(GitUtil.normalizeBranchName("refs/heads/feature/branchName"),equalTo("feature/branchName"));
	}

	@Test
	public void testNormalizeBranchName$tagRef() throws Exception {
		assertThat(GitUtil.normalizeBranchName("refs/tags/v0.1.0"),nullValue());
	}

	@Test
	public void testNormalizeBranchName$simpleWildcardOnRepositoryName() throws Exception {
		assertThat(GitUtil.normalizeBranchName("*/master"),nullValue());
	}

	@Test
	public void testNormalizeBranchName$compositeWildcardOnRepositoryName() throws Exception {
		assertThat(GitUtil.normalizeBranchName("**/master"),nullValue());
	}

	@Test
	public void testNormalizeBranchName$simpleWildcardOnBranchName() throws Exception {
		assertThat(GitUtil.normalizeBranchName("origin/feature-*"),nullValue());
	}

	@Test
	public void testNormalizeBranchName$compositeWildcardOnBranchName() throws Exception {
		assertThat(GitUtil.normalizeBranchName("origin/feature**"),nullValue());
	}

	@Test
	public void testNormalizeBranchName$multipleBranches() throws Exception {
		assertThat(GitUtil.normalizeBranchName("refs/remotes/mine/one refs/remotes/origin/other"),equalTo("one"));
	}

	@Test
	public void testNormalizeBranchName$multipleBranches$preferred() throws Exception {
		assertThat(GitUtil.normalizeBranchName("develop master"),equalTo("master"));
	}

	@Test
	public void testNormalizeBranchName$nonValid() throws Exception {
		assertThat(GitUtil.normalizeBranchName("origin/feature** refs/tags/v.1.0.0"),nullValue());
	}

}
