package com.crimelist.crime.controller;

import com.crimelist.crime.AuthorizationApplication;
import com.crimelist.crime.config.AppProperties;
import com.crimelist.crime.model.AuthProvider;
import com.crimelist.crime.model.ERole;
import com.crimelist.crime.model.Role;
import com.crimelist.crime.payload.*;
import com.crimelist.crime.repository.RoleRepository;
import com.crimelist.crime.repository.UserRepository;
import com.crimelist.crime.exception.BadRequestException;
import com.crimelist.crime.model.User;
import com.crimelist.crime.security.CustomUserDetailsService;
import com.crimelist.crime.security.TokenProvider;
import com.crimelist.crime.security.UserPrincipal;
import com.google.api.client.util.ArrayMap;
import com.google.api.core.ApiFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private static String FROM_EMAIL = "contact@rewura.com";  // GMail user name (just the part before "@gmail.com")

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenProvider tokenProvider;

    @Value("${app.publicEndpoint}")
    private String publicEndpoint;

    @Value("${app.smtp.password}")
    private String smtpPassword;

    @Value("${app.smtp.userName}")
    private String smtpUserName;

    @Value("${app.name}")
    private String applicationName;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @GetMapping("/sso-token")
    public ResponseEntity<?> generateSSOToken(@RequestHeader(name = "id-token", required = true) String idToken) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        UserRecord fbUser = FirebaseAuth.getInstance().getUserByEmail(decodedToken.getEmail());

        //UserPrincipal principal = UserPrincipal.create(userRepository.findByEmail(decodedToken.getEmail()).get());
        UserPrincipal userDetails = null;
        try {
            userDetails = customUserDetailsService.loadUserByUsername(decodedToken.getEmail());
            if("local".equals(userDetails.getProvider())) {
                User toUpdate = userRepository.findByEmail(decodedToken.getEmail()).get();
                List<String> fbData = (List) ((ArrayMap) ((ArrayMap) decodedToken.getClaims().get("firebase")).get("identities")).get("facebook.com");
                if(fbData != null) {
                    toUpdate.setProvider(AuthProvider.valueOf(AuthProvider.facebook.name()));
                    toUpdate.setProviderId(fbData.get(0));
                }
                userRepository.save(toUpdate);
            }
        } catch(UsernameNotFoundException e) {
            User user = new User();
            user.setName(decodedToken.getName());
            user.setEmail(decodedToken.getEmail());
            user.setPassword(fbUser.getProviderId());
            user.setProvider(AuthProvider.local);
            user.setProviderId("");
            user.setEmailVerificationCode("");
            user.setEmailVerified(true);
            user.setPhone("");
            user.setImageUrl(decodedToken.getPicture());
            user.setPassword(null);
            Role userRoles = roleRepository.findByName(ERole.ROLE_USER);
            user.setRoles(Set.of(userRoles));
            user.setRegistrationDateTime(OffsetDateTime.now());
            List<String> fbData = (List) ((ArrayMap) ((ArrayMap) decodedToken.getClaims().get("firebase")).get("identities")).get("facebook.com");
            if(fbData != null) {
                user.setProvider(AuthProvider.valueOf(AuthProvider.facebook.name()));
                user.setProviderId(fbData.get(0));
            }

            List<String> googleData = (List) ((ArrayMap) ((ArrayMap) decodedToken.getClaims().get("firebase")).get("identities")).get("google.com");
            if(googleData != null) {
                user.setProvider(AuthProvider.valueOf(AuthProvider.google.name()));
                user.setProviderId(googleData.get(0));
            }

            User result = userRepository.save(user);
            userDetails = UserPrincipal.create(result);
            persistFirebaseUser(decodedToken.getUid(), user);
        }
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        responseHeaders.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        if(!userDetails.isEnabled()) {
            ErrorResponse responseBody = new ErrorResponse("AUT_001");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).headers(responseHeaders).body(responseBody);
        }
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        return ResponseEntity.ok().headers(responseHeaders).body(new AuthResponse(tokenProvider.createToken(authentication)));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        System.out.println("Login ... ");
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.createToken(authentication);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/signup")
    @Transactional
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) throws MessagingException, FirebaseAuthException {
        logger.info("New user");
        logger.info("select user0_.id as col_0_0_ from users user0_ where user0_.email='" + signUpRequest.getEmail() +"' limit 1");
        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new BadRequestException("Email address already in use.");
        }
        String verificationCode = UUID.randomUUID().toString();
        logger.info("New user");
        // Creating user's account
        User user = new User();
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(signUpRequest.getPassword());
        user.setProvider(AuthProvider.local);
        user.setEmailVerificationCode(verificationCode);
        user.setEmailVerified(false);
        user.setPhone(signUpRequest.getPhone());
        user.setRegistrationDateTime(OffsetDateTime.now());
        logger.info("New user");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role userRoles = roleRepository.findByName(ERole.ROLE_USER);
        user.setRoles(Set.of(userRoles));
        logger.info("New user");
        User result = userRepository.save(user);

        String[] to = { user.getEmail() }; // list of recipient email addresses
        String subject = applicationName + " - Email verification";

        String body = String.format("<h1>Welcome to %s</h1><br/>Please click on the link below to confirm your email address: <br/><a href=%s/api/auth/confirm/%s>Confirm your email address</a><br/><br/>Enjoy your Jurney<br/>%s team", applicationName, publicEndpoint, verificationCode, applicationName);
        sendMail(to, subject, body);

        logger.info("New user");
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/user/me")
                .buildAndExpand(result.getId()).toUri();
        logger.info("New user");
        registerFirebaseUser(result, signUpRequest.getPassword());
        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "User successfully registered. "));
    }

    private void registerFirebaseUser(User user, String password) throws FirebaseAuthException {
        UserRecord.CreateRequest ur = new UserRecord.CreateRequest();
        ur.setEmail(user.getEmail());
        ur.setEmailVerified(false);
        ur.setPassword(password);
        ur.setDisabled(false);
        ur.setDisplayName(user.getName());
        UserRecord response = FirebaseAuth.getInstance().createUser(ur);

        Map insert = new HashMap() {{
            put("name", response.getDisplayName());
            put("email", response.getEmail());
            put("username", response.getEmail());
            put("image", response.getPhotoUrl());
            put("followingCount", 0);
            put("followersCount", 0);
        }};
        persistFirebaseUser(response.getUid(), user);
        //AuthorizationApplication.fireStore.collection("users").document(response.getUid()).set(insert);
    }

    private void persistFirebaseUser(String uid, User user) {
        Map insert = new HashMap() {{
            put("name", user.getName());
            put("email", user.getEmail());
            put("username", user.getEmail());
            put("image", user.getImageUrl());
            put("followingCount", 0);
            put("followersCount", 0);
        }};
        AuthorizationApplication.fireStore.collection("users").document(uid).set(insert);
    }

    @SneakyThrows
    @GetMapping("/confirm/{emailVerificationCode}")
    public void confirmEmail(@PathVariable String emailVerificationCode, HttpServletResponse servletResponse) throws IOException {
        confirmEmail(emailVerificationCode);
        servletResponse.sendRedirect(publicEndpoint + "/#/auth/login/verified");
    }

    @SneakyThrows
    @GetMapping("/confirm-reset/{emailVerificationCode}")
    public void confirmResetEmail(@PathVariable String emailVerificationCode, HttpServletResponse servletResponse) throws IOException {
        confirmEmail(emailVerificationCode);
        servletResponse.sendRedirect(publicEndpoint + "/#/auth/new-password/" + emailVerificationCode);
    }

    private void confirmEmail(final String emailVerificationCode) {
        Optional<User> response = userRepository.findByEmailVerificationCode(emailVerificationCode);
        if(response.isPresent()) {
            User user = response.get();
            user.setEmailVerified(Boolean.TRUE);
            userRepository.save(user);
        }
    }

    @PostMapping("/password-reset")
    public ResponseEntity passwordReset(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse servletResponse) throws IOException, MessagingException {
        Optional<User> response = userRepository.findByEmail(loginRequest.getEmail());
        if(response.isPresent()) {
            User user = response.get();
            UUID verificationCode = UUID.randomUUID();
            user.setEmailVerified(Boolean.FALSE);
            user.setEmailVerificationCode(verificationCode.toString());
            userRepository.save(user);

            String[] to = { user.getEmail() }; // list of recipient email addresses
            String subject = applicationName + " - Password reset";
            String body = String.format("<h1>Password Reset</h1><br/>Please click on the link below to reset your password: <br/><a href=%s/api/auth/confirm-reset/%s>Reset your password</a><br/><br/>Enjoy your Jurney<br/>Booker team", publicEndpoint, verificationCode.toString());
            sendMail(to, subject, body);
        }
        //servletResponse.sendRedirect("http://localhost:3000/new-password");
        return ResponseEntity.ok().body(loginRequest);
    }

    @PostMapping("/update-passsword")
    public ResponseEntity updatePassword(@Valid @RequestBody PasswordResetRequest passwordResetRequest, HttpServletResponse servletResponse) throws IOException {
        Optional<User> response = userRepository.findByEmailVerificationCode(passwordResetRequest.getVerificationCode());
        if(response.isPresent()) {
            User user = response.get();
            user.setEmailVerificationCode(UUID.randomUUID().toString());
            user.setPassword(passwordEncoder.encode(passwordResetRequest.getPassword()));
            userRepository.save(user);
            System.out.println("Update password: " + passwordResetRequest.getPassword());
        }
        //servletResponse.sendRedirect("http://localhost:3000/login/password-updated");
        return ResponseEntity.ok().body(passwordResetRequest);
    }


    private void sendMail(String[] to, String subject, String body) throws MessagingException {
        Properties props = System.getProperties();
        String host = "smtp-relay.sendinblue.com";
//        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", smtpUserName);
        props.put("mail.smtp.password", smtpPassword);
        props.put("mail.smtp.port", 587);
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(FROM_EMAIL));
            InternetAddress[] toAddress = new InternetAddress[to.length];

            // To get the array of addresses
            for( int i = 0; i < to.length; i++ ) {
                toAddress[i] = new InternetAddress(to[i]);
            }

            for( int i = 0; i < toAddress.length; i++) {
                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
            }

            message.setSubject(subject);
            //message.setText(body);
            UUID.randomUUID();

            message.setContent(body, "text/html");
            Transport transport = session.getTransport("smtp");
            transport.connect(host, smtpUserName, smtpPassword);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }
        catch (AddressException ae) {
            ae.printStackTrace();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
    }
}
