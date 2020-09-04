package com.mprribeiro.libraryapi.api.service;

import org.springframework.stereotype.Service;

import java.util.List;

public interface EmailService {
    void sendMails(String message, List<String> mailList);
}
