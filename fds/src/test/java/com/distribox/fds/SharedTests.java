package com.distribox.fds;

import com.distribox.fds.controllers.ServersController;
import com.distribox.fds.entities.File;
import com.distribox.fds.entities.Server;
import com.distribox.fds.entities.User;
import com.distribox.fds.repos.FilesRepository;
import com.distribox.fds.repos.ServersRepository;
import com.distribox.fds.repos.UsersRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PropertySource("classpath:application.properties")
public class SharedTests {

	private static final Logger log = LoggerFactory.getLogger(SharedTests.class);

	@Autowired
	public ServersRepository serversRepository;

	@Autowired
	public FilesRepository filesRepository;
	@Autowired
	public UsersRepository usersRepository;

//	@Autowired
//	public TestRestTemplate restTemplate;

	@BeforeAll
	public void setupDB() {
		List<File> files;
		List<User> users;
		Server s1 = new Server("s1");
		Server s2 = new Server("s2");
		s1 = serversRepository.save(s1);
		s2 = serversRepository.save(s2);
		log.info("S1: " + s1.toString());
		users = new ArrayList<>();
		users.add(new User("benE"));
		users.add(new User("mberk"));
		users.add(new User("yberner"));
		users.add(new User("yolshin"));

		users.forEach(usersRepository::save);

		files = new ArrayList<>();
		files.add(new File("benFile", "benE"));
		File berkFile = new File("file1", "mberk");
		berkFile.addServer(s2);
		files.add(berkFile);
		files.add(new File("bernerFile", "yberner"));
		files.add(new File("olFile", "yolshin"));
		files.forEach(filesRepository::save);
		final Server fs1 = s1;
		files.forEach(f -> f.addServer(fs1));

		files.forEach(filesRepository::save);
		serversRepository.save(fs1);
		serversRepository.save(s2);
	}

	public List<Server> serverList() {
		return serversRepository.findAll();
	}



	@AfterAll
	public void cleanup() {
		serversRepository.deleteAll();
		filesRepository.deleteAll();
		usersRepository.deleteAll();
	}


}
