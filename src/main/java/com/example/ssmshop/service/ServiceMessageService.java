package com.example.ssmshop.service;

import com.example.ssmshop.domain.ServiceMessage;
import com.example.ssmshop.mapper.ServiceMessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServiceMessageService {
    private final ServiceMessageMapper serviceMessageMapper;

    public ServiceMessageService(ServiceMessageMapper serviceMessageMapper) {
        this.serviceMessageMapper = serviceMessageMapper;
    }

    @Transactional
    public void create(Long userId, Long orderId, String messageText) {
        createUserMessage(userId, orderId, messageText);
    }

    @Transactional
    public void createUserMessage(Long userId, Long orderId, String messageText) {
        if (messageText == null || messageText.isBlank()) {
            throw new IllegalArgumentException("请填写要咨询的问题");
        }
        ServiceMessage message = new ServiceMessage();
        message.setUserId(userId);
        message.setOrderId(orderId);
        message.setMessage(messageText.trim());
        message.setStatus("OPEN");
        message.setSenderRole("USER");
        message.setUserUnread(false);
        message.setAdminUnread(true);
        serviceMessageMapper.insert(message);
    }

    @Transactional
    public void createAdminReply(Long userId, Long orderId, String messageText) {
        if (messageText == null || messageText.isBlank()) {
            throw new IllegalArgumentException("请填写回复内容");
        }
        ServiceMessage message = new ServiceMessage();
        message.setUserId(userId);
        message.setOrderId(orderId);
        message.setMessage(messageText.trim());
        message.setStatus("REPLIED");
        message.setSenderRole("ADMIN");
        message.setUserUnread(true);
        message.setAdminUnread(false);
        serviceMessageMapper.insert(message);
    }

    public List<ServiceMessage> findForUserOrder(Long orderId, Long userId) {
        return serviceMessageMapper.findByOrderIdAndUserId(orderId, userId);
    }

    public List<ServiceMessage> findForOrder(Long orderId) {
        return serviceMessageMapper.findByOrderId(orderId);
    }

    public List<ServiceMessage> findForUser(Long userId) {
        return serviceMessageMapper.findByUserId(userId);
    }

    public List<ServiceMessage> findAll() {
        return serviceMessageMapper.findAll();
    }

    public int countAdminUnread() {
        return serviceMessageMapper.countAdminUnread();
    }

    public int countUserUnread(Long userId) {
        return serviceMessageMapper.countUserUnread(userId);
    }

    public void markAdminRead(Long orderId) {
        serviceMessageMapper.markAdminRead(orderId);
    }

    public void markAllAdminRead() {
        serviceMessageMapper.markAllAdminRead();
    }

    public void markUserRead(Long userId, Long orderId) {
        serviceMessageMapper.markUserRead(userId, orderId);
    }

    public void markAllUserRead(Long userId) {
        serviceMessageMapper.markAllUserRead(userId);
    }
}
