package com.wiss.quizbackend.dto;

// this ones immutable
public class LoginResponseDTO {

    private final String token;
    private final String tokenType = "Bearer";
    private final Long userId;
    private final String username;
    private final String email;
    private final String role;
    private final long expiresIn;  // in Millisekunden

    /**
     * Constructor für erfolgreichen Login.
     *
     * @param token Der generierte JWT Token
     * @param userId Die User ID
     * @param username Der Username
     * @param email Die Email
     * @param role Die Rolle (ADMIN oder PLAYER)
     * @param expiresIn Token Gültigkeit in ms
    (normalerweise 86400000 = 24h)
     */
    public LoginResponseDTO(String token, Long userId, String username,
                            String email, String role, long expiresIn) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.expiresIn = expiresIn;
    }

    public String getToken() { return token; }

    public String getTokenType() { return tokenType; }

    public Long getUserId() { return userId; }

    public String getUsername() { return username; }

    public String getEmail() { return email; }

    public String getRole() { return role; }

    public long getExpiresIn() { return expiresIn; }

    @Override
    public String toString() {
        return "LoginResponseDTO{" +
                "token='[HIDDEN]'" +
                ", tokenType='" + tokenType + '\'' +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }
}

