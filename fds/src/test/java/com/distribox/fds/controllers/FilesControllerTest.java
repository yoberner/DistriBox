package com.distribox.fds.controllers;

import com.distribox.fds.SharedTests;
import com.distribox.fds.entities.File;
import com.distribox.fds.entities.Server;
import com.distribox.fds.entities.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.http.HttpClient;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PropertySource("classpath:application.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FilesControllerTest extends SharedTests {

	private static final Logger log = LoggerFactory.getLogger(FilesControllerTest.class);
	@Autowired
	private ServersController serversController;

	@Autowired
	private FilesController filesController;
	@Autowired
	public TestRestTemplate restTemplate;

	@Value(value="${local.server.port}")
	public int port;

	@Test
	public void contextLoads() {
		assertNotNull(serversController);
	}

	@Test
	public void saveFileTest() {
		Map<String, Object> body = new HashMap<>();
		String filepath = "/saveFileTest/file1.txt";
		body.put("filepath", filepath);
		body.put("userid", "sftUser");
		List<String> serverids = serverList().stream().map(Server::getId).toList();
		body.put("serverids", serverids);
		Map<String, String> response = restTemplate.postForObject("http://localhost:" + port + "/saveFile", body,
				Map.class);
		String filePath2 = response.get("filepath");
		assertNotNull(filePath2);
		User u = usersRepository.findByUserid("sftUser");
		Set<String> filepaths = u.files.stream().map(f -> f.filepath).collect(Collectors.toSet());
		assertFalse(filepaths.contains(filepath));
		ResponseEntity<String> savedResponse = restTemplate.postForEntity("http://localhost:" + port +
						"/savedFile?filePath=" + filePath2, null, String.class);
		assertTrue(savedResponse.getStatusCode().is2xxSuccessful());
		RequestEntity<String> re = RequestEntity.method(HttpMethod.POST, "http://localhost:" + port +
						"/getFile")
				.body("filePath=" + filePath2);
		ResponseEntity<String> responseEntity = restTemplate.exchange(re, String.class);
		assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
	}

	@Test
	public void addServersToFileTest() {
		String filePath = "benFile";
		List<String> serverids = List.of("s2");
		Map<String, Object> body = new HashMap<>();
		body.put("filepath", filePath);
		body.put("userid", "benE");
		body.put("serverids", serverids);
		ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:" + port + "/saveFile", body,
				Map.class);
		assertTrue(response.getStatusCode().is2xxSuccessful());
		String[] expectedIds = {"s1", "s2"};
		ResponseEntity<String> ackResponse = restTemplate.postForEntity("http://localhost:" + port +
				"/savedFile?filePath=" + filePath, null, String.class);
		assertTrue(ackResponse.getStatusCode().is2xxSuccessful());
		File file = filesRepository.findByFilepath(filePath).get();
		List<String> actualIds = file.getServers().stream().map(Server::getId).toList();
		assertArrayEquals(actualIds.toArray(), expectedIds);
	}

	@Test
	public void addServersToFileWithoutAckFist() {

		String filePath = "myFile";
		Map<String, Object> body = new HashMap<>();
		body.put("filepath", filePath);
		body.put("userid", "benE");
		body.put("serverids", List.of("s1"));
		//Add server, but don't send ack yet
		ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:" + port + "/saveFile", body,
				Map.class);
		assertTrue(response.getStatusCode().is2xxSuccessful());
		body.put("serverids", List.of("s2"));
		//Add another server to the file
		response = restTemplate.postForEntity("http://localhost:" + port + "/saveFile", body,
				Map.class);
		assertTrue(response.getStatusCode().is2xxSuccessful());
		String[] expectedIds = {"s1", "s2"};
		//Finally, send the ACK
		ResponseEntity<String> ackResponse = restTemplate.postForEntity("http://localhost:" + port +
				"/savedFile?filePath=" + filePath, null, String.class);
		assertTrue(ackResponse.getStatusCode().is2xxSuccessful());
		File file = filesRepository.findByFilepath(filePath).get();
		List<String> actualIds = file.getServers().stream().map(Server::getId).toList();
		assertArrayEquals(actualIds.toArray(), expectedIds);
	}

	@Test
	public void removeServerTest() {
		String filePath = "file1";
		File file = filesRepository.findByFilepath(filePath).get();
		assertEquals(2, file.getServers().size());
		String serverid = "s2";
		Server s2 = serversRepository.findById(serverid).get();
		assertTrue(file.getServers().contains(s2));
		RequestEntity<Map> re = RequestEntity.method(HttpMethod.DELETE, "http://localhost:" + port +
						"/removeServerFromFile")
						.body(Map.of("filePath", filePath,
									"serverid", serverid));

		ResponseEntity<String> response = restTemplate.exchange(re, String.class);
		assertTrue(response.getStatusCode().is2xxSuccessful());
		file = filesRepository.findByFilepath(filePath).get();
		s2 = serversRepository.findById(serverid).get();
		assertFalse(file.getServers().contains(s2));
	}

	@Test
	public void noFilepath() {
		String userId = "mberk";
		List<String> serverids = List.of("s1");
		Map<String, Object> body = new HashMap<>();
		body.put("userid", userId);
		body.put("serverids", serverids);
		ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:" + port + "/saveFile", body,
				Map.class);
		assertTrue(response.getStatusCode().is4xxClientError());
	}

	@Test
	public void noUserid() {
		List<String> serverids = List.of("s1");
		Map<String, Object> body = new HashMap<>();
		body.put("filepath", "/mberk/noUserId");
		body.put("serverids", serverids);
		ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:" + port + "/saveFile", body,
				Map.class);
		assertTrue(response.getStatusCode().is4xxClientError());
	}

	@Test
	public void noServerids() {
		Map<String, Object> body = new HashMap<>();
		body.put("filepath", "/mberk/noUserId");
		body.put("userid", "mberk");
		ResponseEntity<Map> response = restTemplate.postForEntity("http://localhost:" + port + "/saveFile", body,
				Map.class);
		assertTrue(response.getStatusCode().is4xxClientError());
	}
}
