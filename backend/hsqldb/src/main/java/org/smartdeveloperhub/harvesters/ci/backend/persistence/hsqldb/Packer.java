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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-hsqldb:1.0.0-SNAPSHOT
 *   Bundle      : ci-backend-hsqldb-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.persistence.hsqldb;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import net.java.truevfs.access.TFile;
import net.java.truevfs.access.TFileOutputStream;
import net.java.truevfs.access.TVFS;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Packer {

	private static final Logger LOGGER=LoggerFactory.getLogger(Packer.class);

	private static final Packer SINGLETON=new Packer();

	private Packer() {
		Runtime.
			getRuntime().
				addShutdownHook(
						new Thread("TVFSCleaner") {
							@Override
							public void run() {
								try {
									TVFS.umount();
								} catch (Exception e) {
									LOGGER.error("Could not unmount the packed resources. Full stacktrace follows",e);
								}
							}
						}
				);
	}

	static File pack(String targetLocation, List<File> resources) throws IOException {
		for(File source:resources) {
			SINGLETON.addToPack(targetLocation,source);
		}
		return new File(targetLocation);
	}

	static File unpack(String sourceLocation, String targetLocation) throws IOException {
		File target=new File(targetLocation);
		TFile source=new TFile(sourceLocation);
		if(source.canRead()) {
			for(TFile sourceFile:source.listFiles()) {
				SINGLETON.extractFromPack(sourceFile, new File(target,sourceFile.getName()));
			}
		}
		return new File(targetLocation,new File(sourceLocation).getName());
	}

	private void extractFromPack(TFile sourceFile, File targetFile) throws IOException {
		LOGGER.debug("Unpacking {}...",targetFile);
		try {
			sourceFile.cp(targetFile);
		} catch (IOException e) {
			LOGGER.error("Could not unpack {}. Full stacktrace follows",targetFile,e);
			throw e;
		}
	}

	private void addToPack(String targetLocation, File source) throws IOException {
		if(!source.canRead()) {
			LOGGER.debug("Discarded missing file {}",source);
			return;
		}

		LOGGER.debug("Packing {}...",source);
		TFile target=new TFile(targetLocation+File.separator+source.getName());
		try {
			OutputStream writer=new TFileOutputStream(target);
			try {
				FileUtils.copyFile(source,writer);
			} catch (IOException e) {
				LOGGER.error("Could not pack {}. Full stacktrace follows",source);
				throw e;
			} finally {
				IOUtils.closeQuietly(writer);
			}
		} catch(IOException e) {
			LOGGER.error("Could not create pack entry for {}. Full stacktrace follows",source);
			throw e;
		}
	}

}
