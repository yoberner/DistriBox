package com.distribox.fss.controllers;

import com.distribox.fss.services.FDSService;
import com.distribox.fss.services.SaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class SaveController {

    @Autowired
    SaveService saveService; // automatically-instantiated singleton

    @Autowired
    FDSService fdsService;

    @PostMapping("/save")
    public void save(@RequestBody String file) throws IOException {
        // Save file to disk. (call SaveService)
        saveService.saveFile(file);
        // Send ACK to FDS. (fds stores metadata of file - metadata includes file name and where it is stored as well
        //  as server status)
        fdsService.sendAck();
        //TODO: Return response body!
    }

}