package com.emailDemo.springemailclient.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.emailDemo.springemailclient.dto.UserDto;
import com.emailDemo.springemailclient.entity.User;
import com.emailDemo.springemailclient.repository.UserRepo;

@Service
@EnableScheduling
public class UserService {

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private User user;

	@Autowired
	private JavaMailSender mailSender;

	String Email;

	public User createUser(UserDto userDto) {
		User user = new User();
		// Generate verification code
		String verificationCode = UUID.randomUUID().toString();
		user.setVerificationcode(verificationCode);
		user.setVerified(false);
		user.setUsername(userDto.getUsername());
		user.setEmail(userDto.getEmail());
		user.setPassword(userDto.getPassword());
		// Save user to the database
//		sendUrl(userResponse.getEmail());
		User userResponse = userRepo.save(user);
		if (user.isVerified() == true) {
			sendVerificationCode(user.getVerificationcode());
			System.out.println("user response saved");
		} else {
			triggerMail();
			System.out.println("trigger mail");
		}
		return user;

	}


	@Scheduled(cron = "0 */1 * * * *")
	public void triggerMail() {
		List<User> users = userRepo.findByVerified(false); // Retrieve users with 'verified' set to false

		for (User user : users) {
			//String mail = user.getEmail();
			String vc = user.getVerificationcode();
			sendVerificationCode(vc);
		}
	}

	public void sendVerificationCode(String verificationCode) {
		User user = userRepo.findByVerificationcode(verificationCode);
		if (user != null) {
			String toemail = user.getEmail();
			Email=toemail;
			if (toemail != null) {
				SimpleMailMessage message = new SimpleMailMessage();
				message.setTo(toemail);
				message.setSubject("Verification Code");
				message.setText("Click this link for verification: http://localhost:8080/verifies/" + verificationCode);
				mailSender.send(message);
				System.out.println("Verification mail sent");
			}
		}
	}



	public String verifyUserByVerificationCodeAndEmail(String email, String verificationcode) {
		User user = userRepo.findByEmail(email);
//		sendUrl(email);
		if (user != null && user.getVerificationcode().equals(verificationcode)) {
			user.setVerified(true);
			userRepo.save(user);
			return "verified succesfully";
		}
		return "enter valid code";
	}

	public String verifyUsersByVerificationCode(String verificationcode) {
		User user=userRepo.findByEmail(Email);
		if (user != null && user.getVerificationcode().equals(verificationcode)) {
			user.setVerified(true);
			userRepo.save(user);
			return "verified succesfully";
		}
		return "enter valid code";
	}
}
