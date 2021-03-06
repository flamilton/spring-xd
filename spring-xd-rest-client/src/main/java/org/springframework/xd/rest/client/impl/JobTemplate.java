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

package org.springframework.xd.rest.client.impl;

import java.util.Collections;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.xd.rest.client.JobOperations;
import org.springframework.xd.rest.client.domain.JobDefinitionResource;

/**
 * Implementation of the Job-related part of the API.
 * 
 * @author Glenn Renfro
 * @author Ilayaperumal Gopinathan
 * @author Gunnar Hillert
 */
public class JobTemplate extends AbstractTemplate implements JobOperations {

	JobTemplate(AbstractTemplate source) {
		super(source);
	}

	@Override
	public JobDefinitionResource createJob(String name, String definition, Boolean deploy) {
		MultiValueMap<String, Object> values = new LinkedMultiValueMap<String, Object>();
		values.add("name", name);
		values.add("definition", definition);
		values.add("deploy", deploy.toString());
		JobDefinitionResource job = restTemplate.postForObject(resources.get("jobs"), values,
				JobDefinitionResource.class);
		return job;
	}

	@Override
	public void destroy(String name) {
		String uriTemplate = resources.get("jobs").toString() + "/{name}";
		restTemplate.delete(uriTemplate, Collections.singletonMap("name", name));
	}

	@Override
	public void deployJob(String name, String jobParameters, String dateFormat, String numberFormat, Boolean makeUnique) {

		String uriTemplate = resources.get("jobs").toString() + "/{name}";
		MultiValueMap<String, Object> values = new LinkedMultiValueMap<String, Object>();
		values.add("deploy", "true");

		if (jobParameters != null) {
			values.add("jobParameters", jobParameters);
		}
		if (dateFormat != null) {
			values.add("dateFormat", dateFormat);
		}
		if (numberFormat != null) {
			values.add("numberFormat", numberFormat);
		}
		if (makeUnique != null) {
			values.add("makeUnique", String.valueOf(makeUnique));
		}

		restTemplate.put(uriTemplate, values, name);
	}

	@Override
	public void deploy(String name) {
		deployJob(name, null, null, null, null);
	}

	@Override
	public void undeploy(String name) {
		String uriTemplate = resources.get("jobs").toString() + "/{name}";
		MultiValueMap<String, Object> values = new LinkedMultiValueMap<String, Object>();
		values.add("deploy", "false");
		restTemplate.put(uriTemplate, values, name);
	}

	@Override
	public JobDefinitionResource.Page list() {
		String uriTemplate = resources.get("jobs").toString();
		// TODO handle pagination at the client side
		uriTemplate = uriTemplate + "?size=10000";
		return restTemplate.getForObject(uriTemplate, JobDefinitionResource.Page.class);
	}

	@Override
	public void undeployAll() {
		String uriTemplate = resources.get("jobs").toString() + DEPLOYMENTS_URI;
		MultiValueMap<String, Object> values = new LinkedMultiValueMap<String, Object>();
		values.add("deploy", "false");
		restTemplate.put(uriTemplate, values);
	}

	@Override
	public void deployAll() {
		String uriTemplate = resources.get("jobs").toString() + DEPLOYMENTS_URI;
		MultiValueMap<String, Object> values = new LinkedMultiValueMap<String, Object>();
		values.add("deploy", "true");
		restTemplate.put(uriTemplate, values);
	}

	@Override
	public void destroyAll() {
		restTemplate.delete(resources.get("jobs"));
	}

	@Override
	public String toString() {
		return "JobTemplate [restTemplate=" + restTemplate + ", resources=" + resources + "]";
	}

}
