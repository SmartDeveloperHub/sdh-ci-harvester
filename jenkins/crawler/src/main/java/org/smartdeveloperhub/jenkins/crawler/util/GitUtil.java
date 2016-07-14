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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.3.0
 *   Bundle      : ci-jenkins-crawler-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler.util;

/**
 * Utility class to facilitate the normalization of <i>branch specifier</i> of the
 * <b>Jenkins Git Plugin</b>:
 *
 * <blockquote>
 * <p>
 * Specify the branches if you'd like to track a specific branch in a
 * repository. If left blank, all branches will be examined for changes and
 * built.
 * </p>
 *
 * <p>
 * The safest way is to use the <tt>refs/heads/&lt;branchName&gt;</tt> syntax.
 * This way the expected branch is unambiguous.
 * </p>
 *
 * <p>
 * If your branch name has a <tt>/</tt> in it make sure to use the full
 * reference above. When not presented with a full path the plugin will only use
 * the part of the string right of the last slash. Meaning <tt>foo/bar</tt> will
 * actually match <tt>bar</tt>
 * </p>
 *
 * <p>
 * Possible options:
 * </p>
 * <ul>
 * <li><b><tt>&lt;branchName&gt;</tt></b><br>
 * Tracks/checks out the specified branch. If ambiguous the first result is
 * taken, which is not necessarily the expected one. Better use
 * <tt>refs/heads/&lt;branchName&gt;</tt>.<br>
 * E.g. <tt>master</tt>, <tt>feature1</tt>,...</li>
 * <li><b><tt>refs/heads/&lt;branchName&gt;</tt></b><br>
 * Tracks/checks out the specified branch.<br>
 * E.g. <tt>refs/heads/master</tt>, <tt>refs/heads/feature1/master</tt>,...</li>
 * <li><b><tt>&lt;remoteRepoName&gt;/&lt;branchName&gt;</tt></b><br>
 * Tracks/checks out the specified branch. If ambiguous the first result is
 * taken, which is not necessarily the expected one.<br>
 * Better use <tt>refs/heads/&lt;branchName&gt;</tt>.<br>
 * E.g. <tt>origin/master</tt></li>
 * <li><b><tt>remotes/&lt;remoteRepoName&gt;/&lt;branchName&gt;</tt></b><br>
 * Tracks/checks out the specified branch.<br>
 * E.g. <tt>remotes/origin/master</tt></li>
 * <li><b><tt>refs/remotes/&lt;remoteRepoName&gt;/&lt;branchName&gt;</tt></b><br>
 * Tracks/checks out the specified branch.<br>
 * E.g. <tt>refs/remotes/origin/master</tt></li>
 * <li><b><tt>&lt;tagName&gt;</tt></b><br>
 * This does not work since the tag will not be recognized as tag.<br>
 * Use <tt>refs/tags/&lt;tagName&gt;</tt> instead.<br>
 * E.g. <tt>git-2.3.0</tt></li>
 * <li><b><tt>refs/tags/&lt;tagName&gt;</tt></b><br>
 * Tracks/checks out the specified tag.<br>
 * E.g. <tt>refs/tags/git-2.3.0</tt></li>
 * <li><b><tt>&lt;commitId&gt;</tt></b><br>
 * Checks out the specified commit.<br>
 * E.g. <tt>5062ac843f2b947733e6a3b105977056821bd352</tt>, <tt>5062ac84</tt>,
 * ...</li>
 * <li><b><tt>${ENV_VARIABLE}</tt></b><br>
 * It is also possible to use environment variables. In this case the variables
 * are evaluated and the result is used as described above.<br>
 * E.g. <tt>${TREEISH}</tt>, <tt>refs/tags/${TAGNAME}</tt>,...</li>
 * <li><b><tt>&lt;Wildcards&gt;</tt></b><br>
 * The syntax is of the form: <tt>REPOSITORYNAME/BRANCH</tt>. In addition,
 * <tt>BRANCH</tt> is recognized as a shorthand of <tt>*&#47;BRANCH</tt>, '*' is
 * recognized as a wildcard, and '**' is recognized as wildcard that includes
 * the separator '/'. Therefore, <tt>origin/branches*</tt> would match
 * <tt>origin/branches-foo</tt> but not <tt>origin/branches/foo</tt>, while
 * <tt>origin/branches**</tt> would match both <tt>origin/branches-foo</tt> and
 * <tt>origin/branches/foo</tt>.</li>
 * <li><b><tt>:&lt;regular expression&gt;</tt></b><br>
 * The syntax is of the form: <tt>:regexp</tt>. Regular expression syntax in
 * branches to build will only build those branches whose names match the
 * regular expression.</li>
 * </ul>
 * <p>
 * </p>
 * </blockquote>
 *
 * @author Miguel Esteban Guti&eacute;rrez
 *
 */
public final class GitUtil {

	private static String[] PREFERRED={"master","develop"};

	private static final String REFS_REMOTES = "refs/remotes/";
	private static final String REFS_HEADS   = "refs/heads/";
	private static final String REFS_TAGS    = "refs/tags/";
	private static final String REMOTES      = "remotes/";

	private GitUtil() {
	}

	public static String normalizeBranchName(final String branchName) {
		String normalized=branchName;
		if(normalized!=null) {
			final String[] parts=normalizeBranchNames(normalized);
			normalized=selectBranchName(parts);
		}
		return normalized;
	}

	private static String[] normalizeBranchNames(final String normalized) {
		final String[] parts=normalized.split("\\s");
		for(int i=0;i<parts.length;i++) {
			parts[i]=normalize(parts[i]);
		}
		return parts;
	}

	private static String selectBranchName(final String[] parts) {
		String result=parts[0];
		if(parts.length>1) {
			final int preferred = findPreferred(parts);
			if(preferred<PREFERRED.length) {
				result=PREFERRED[preferred];
			}
		}
		return result;
	}

	private static int findPreferred(final String[] parts) {
		int preferred=Integer.MAX_VALUE;
		for(int i=0;i<parts.length;i++) {
			for(int j=0;j<PREFERRED.length;j++) {
				if(PREFERRED[j].equalsIgnoreCase(parts[i])) {
					preferred=Math.min(preferred, j);
				}
			}
		}
		return preferred;
	}

	private static String normalize(final String branchName) {
		String result=branchName;
		if(branchName.contains("*") || branchName.startsWith(REFS_TAGS)) {
			result=null;
		} else if(branchName.startsWith(REFS_REMOTES)) {
			result=chomp(branchName,REFS_REMOTES,1);
		} else if(branchName.startsWith(REMOTES)) {
			result=chomp(branchName,REMOTES,1);
		} else if(branchName.startsWith(REFS_HEADS)) {
			result=chomp(branchName,REFS_HEADS,0);
		} else if(branchName.contains("/")) {
			result=chomp(branchName,"",1);
		}
		return result;
	}

	private static String chomp(final String branch, final String prefix, final int segments) {
		String trim=branch;
		trim=trim.substring(prefix.length());
		for(int i=0;i<segments;i++) {
			trim=trim.substring(trim.indexOf('/')+1);
		}
		return trim;
	}

}
