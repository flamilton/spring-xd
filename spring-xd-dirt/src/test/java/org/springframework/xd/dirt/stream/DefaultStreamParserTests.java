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

package org.springframework.xd.dirt.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import org.springframework.xd.dirt.module.ModuleDeploymentRequest;

/**
 * @author Mark Fisher
 * @author David Turanski
 */
public class DefaultStreamParserTests {

	@Test
	public void simpleStream() {
		DefaultStreamParser parser = new DefaultStreamParser();
		List<ModuleDeploymentRequest> requests = parser.parse("test", "foo | bar");
		assertEquals(2, requests.size());
		ModuleDeploymentRequest sink = requests.get(0);
		ModuleDeploymentRequest source = requests.get(1);
		assertEquals("foo", source.getModule());
		assertEquals("test", source.getGroup());
		assertEquals(0, source.getIndex());
		assertEquals("source", source.getType());
		assertEquals(0, source.getParameters().size());
		assertEquals("bar", sink.getModule());
		assertEquals("test", sink.getGroup());
		assertEquals(1, sink.getIndex());
		assertEquals("sink", sink.getType());
		assertEquals(0, sink.getParameters().size());
	}

	@Test
	public void parameterizedModules() {
		DefaultStreamParser parser = new DefaultStreamParser();
		List<ModuleDeploymentRequest> requests = parser.parse("test", "foo --x=1 --y=two | bar --z=3");
		assertEquals(2, requests.size());
		ModuleDeploymentRequest sink = requests.get(0);
		ModuleDeploymentRequest source = requests.get(1);
		assertEquals("foo", source.getModule());
		assertEquals("test", source.getGroup());
		assertEquals(0, source.getIndex());
		assertEquals("source", source.getType());
		Map<String, String> sourceParameters = source.getParameters();
		assertEquals(2, sourceParameters.size());
		assertEquals("1", sourceParameters.get("x"));
		assertEquals("two", sourceParameters.get("y"));
		assertEquals("bar", sink.getModule());
		assertEquals("test", sink.getGroup());
		assertEquals(1, sink.getIndex());
		assertEquals("sink", sink.getType());
		Map<String, String> sinkParameters = sink.getParameters();
		assertEquals(1, sinkParameters.size());
		assertEquals("3", sinkParameters.get("z"));
	}

	@Test
	public void testParameters() {
		String module = "gemfire-cq --query=Select * from /Stocks where symbol='VMW' --regionName=foo --foo=bar";
		DefaultStreamParser parser = new DefaultStreamParser();
		Properties parameters = parser.getParameters(module);
		assertEquals(3, parameters.size());
		assertEquals("Select * from /Stocks where symbol='VMW'", parameters.get("query"));
		assertEquals("foo", parameters.get("regionName"));
		assertEquals("bar", parameters.get("foo"));

		module = "test";
		parameters = parser.getParameters(module);
		assertEquals(0, parameters.size());

		module = "foo --x=1 --y=two ";
		parameters = parser.getParameters(module);
		assertEquals(2, parameters.size());
		assertEquals("1", parameters.get("x"));
		assertEquals("two", parameters.get("y"));

		module = "foo --x=2";
		parameters = parser.getParameters(module);
		assertEquals(1, parameters.size());
		assertEquals("2", parameters.get("x"));
		module = "--foo = bar";

		try {
			parser.getParameters(module);
			fail(module + " is invalid. Should throw exception");
		}
		catch (Exception e) {

		}
	}

	@Test
	public void testInvalidModules() {
		String config = "test | foo--x=13";
		DefaultStreamParser parser = new DefaultStreamParser();
		try {
			parser.parse("t", config);
			fail(config + " is invalid. Should throw exception");
		}
		catch (Exception e) {

		}
	}

}
