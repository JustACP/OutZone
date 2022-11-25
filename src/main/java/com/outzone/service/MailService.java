package com.outzone.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.BufferedInputStream;
import java.io.IOException;

@Service
public class MailService {
    @Value("${spring.mail.username}")
    private String formMail;
    @Resource
    JavaMailSender mailSender;

    public JavaMailSender getMailSender() {
        return mailSender;
    }

    public void sendText(String txt){
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("robot@re1ife.top");
        mailMessage.setTo("justacp@163.com");
        mailMessage.setSubject("test");
        mailMessage.setText(txt);
        try{
            mailSender.send(mailMessage);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendTemplateMessage(String subject,String mailAddress,String code,String username) throws IOException, MessagingException {
        ClassPathResource classPathResource = new ClassPathResource("/template/mail-template.ftl");
        BufferedInputStream bufferedInputStream =  new BufferedInputStream(classPathResource.getInputStream());

        byte[] templateMail = new byte[bufferedInputStream.available()];
        String templateMailContent = "";

        int flag = 0;
        while((flag = bufferedInputStream.read(templateMail)) != -1){
            templateMailContent+=new String(templateMail);
        }

        templateMailContent = templateMailContent.replaceAll("#code",code);
        templateMailContent = templateMailContent.replaceAll("#product","OutZone");
        templateMailContent = templateMailContent.replaceAll("#name",username);
//        System.out.println(templateMailContent);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message,true);
        messageHelper.setSubject(subject);
        messageHelper.setTo(mailAddress);
        messageHelper.setFrom(formMail);
        messageHelper.setText(templateMailContent,true);
        mailSender.send(message);

    }
}
