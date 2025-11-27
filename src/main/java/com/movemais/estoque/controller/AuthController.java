package com.movemais.estoque.controller;

import com.movemais.estoque.dto.auth.LoginRequest;
import com.movemais.estoque.dto.auth.TokenResponse;
import com.movemais.estoque.service.JwtTokenService;
import com.movemais.estoque.service.UsuarioDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UsuarioDetailsService usuarioDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {

        log.info("Tentando autenticar username='{}'", request.getUsername());

        try {
            UserDetails userDetails = usuarioDetailsService.loadUserByUsername(request.getUsername());

            boolean matches = passwordEncoder.matches(request.getPassword(), userDetails.getPassword());
            log.info("Comparando senha. raw='{}', encoded='{}', matches={}",
                    request.getPassword(), userDetails.getPassword(), matches);

            if (!matches) {
                throw new BadCredentialsException("Usuário inexistente ou senha inválida");
            }

            String token = jwtTokenService.generateToken(userDetails.getUsername());
            return new TokenResponse(token, "Bearer");

        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário inexistente ou senha inválida");
        } catch (Exception ex) {
            log.error("Erro ao autenticar usuário", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao autenticar usuário");
        }
    }
}