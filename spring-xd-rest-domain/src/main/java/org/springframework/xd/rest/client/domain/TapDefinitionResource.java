/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xd.rest.client.domain;

import org.springframework.hateoas.PagedResources;
import org.springframework.util.Assert;

/**
 * Represents a tap definition
 *
 * @author David Turanski
 * @author Gunnar Hillert
 *
 */
public class TapDefinitionResource extends StreamDefinitionResource {

	private String streamName;

	/**
	 * Default constructor for serialization frameworks.
	 */
	@SuppressWarnings("unused")
	private TapDefinitionResource() {
		super();
	}

	/**
	 * @param name
	 * @param definition
	 */
	public TapDefinitionResource(String name, String streamName, String definition) {
		super(name, definition);
		Assert.hasText(streamName, "stream name cannot be empty or null");
		this.streamName = streamName;
	}

	/**
	 * @return the streamName
	 */
	public String getStreamName() {
		return streamName;
	}

	@Override
	public String toString() {
		return "TapDefinitionResource [name=" + getName() + ", streamName=" + streamName + ", definition="
				+ getDefinition() + "]";
	}

	/**
	 * Dedicated subclass to workaround type erasure.
	 *
	 * @author Eric Bottard
	 */
	public static class Page extends PagedResources<TapDefinitionResource> {

	}

}